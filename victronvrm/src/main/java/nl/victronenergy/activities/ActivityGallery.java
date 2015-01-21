/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import nl.victronenergy.R;
import nl.victronenergy.adapters.GalleryPagerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import nl.victronenergy.models.BaseResponse;
import nl.victronenergy.models.Site;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.BUNDLE;
import nl.victronenergy.util.Constants.LOADER_ID;
import nl.victronenergy.util.Constants.POST;
import nl.victronenergy.util.Constants.RESPONSE_CODE;
import nl.victronenergy.util.MyLog;
import nl.victronenergy.util.webservice.JsonParserHelper;
import nl.victronenergy.util.webservice.RestResponse;
import nl.victronenergy.util.webservice.WebserviceAsync;

/**
 * Activity that lets a user swipe between the pictures of his site
 * <p/>
 * User: Victron Energy<br/>
 * Date: 12-5-2014<br/>
 * Time: 15:21<br/>
 */
public class ActivityGallery extends ActionBarActivity implements LoaderCallbacks<RestResponse>, OnPageChangeListener, OnClickListener {

	/** Tag used for logging in this class */
	private static final String LOG_TAG = "ActivityGallery";

	/** Key for (re)storing the currently visible gallery item */
	public static final String KEY_POSITION = "KEY_POSITION";

	/** Viewpager filled with images/pictures of the site */
	private ViewPager mViewPagerGallery;

	/** The id of the site we want to show the images for */
	private Site mSite;

	/** Pager adapter for the gallery */
	private GalleryPagerAdapter mGalleryPagerAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mSite = (Site) getIntent().getSerializableExtra(BUNDLE.SITE_OBJECT);
		int pagerPosition;
		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(KEY_POSITION);
		} else {
			pagerPosition = getIntent().getIntExtra(KEY_POSITION, 0);
		}

		// PagerPosition is 0 based so + 1
		setActionBarTitle(pagerPosition + 1, mSite.getImages().size());

		mViewPagerGallery = (ViewPager) findViewById(R.id.viewpager_gallery);
		mGalleryPagerAdapter = new GalleryPagerAdapter(this, mSite);
		mViewPagerGallery.setAdapter(mGalleryPagerAdapter);
		mViewPagerGallery.setCurrentItem(pagerPosition);
		mViewPagerGallery.setOnPageChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Only show the menu with the delete option when a user is able to delete images
		if (mSite.canEdit()) {
			getMenuInflater().inflate(R.menu.menu_gallery, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_delete:
				showDeleteImageDialog();
				break;
			case android.R.id.home:
				onBackPressed();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(KEY_POSITION, mViewPagerGallery.getCurrentItem());
	}

	/**
	 * Shows confirmation dialog to delete the image
	 */
	private void showDeleteImageDialog() {
		new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom)).setTitle(getString(R.string.app_name_complete))
				.setMessage(R.string.gallery_question_delete_image).setIcon(R.drawable.ic_launcher_base)
				.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int which) {
						callDeleteImageLoader(mSite.getImages().get(mViewPagerGallery.getCurrentItem()));
						dialogInterface.dismiss();
					}
				}).setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int which) {
						dialogInterface.dismiss();
					}
				}).show();
	}

	/**
	 * Update the actionbar title to show the currently selected image and the amount of total images
	 *
	 * @param pCurrentPage
	 *        The current page we are on
	 * @param pTotalPages
	 *        Total amount of pages
	 */
	private void setActionBarTitle(int pCurrentPage, int pTotalPages) {
		getSupportActionBar().setTitle(getString(R.string.gallery_title, pCurrentPage, pTotalPages));
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// Do nothing
	}

	@Override
	public void onPageSelected(int position) {
		// + 1 because position is 0 based
		setActionBarTitle(position + 1, mViewPagerGallery.getAdapter().getCount());
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// Do nothing
	}

	/**
	 * Call the loader to delete a certain image
	 *
	 * @param pImageName
	 *        The name of the image that should be deleted
	 */
	private void callDeleteImageLoader(String pImageName) {
		Bundle params = new Bundle();
		params.putString(POST.URI, Constants.DELETE_IMAGE);
		params.putInt(POST.SITE_ID, mSite.getIdSite());
		params.putString(POST.IMAGE_NAME, pImageName);

		if (getSupportLoaderManager().getLoader(LOADER_ID.DELETE_IMAGE) == null) {
			getSupportLoaderManager().initLoader(LOADER_ID.DELETE_IMAGE, params, this);
		} else {
			getSupportLoaderManager().restartLoader(LOADER_ID.DELETE_IMAGE, params, this);
		}
	}

	/**
	 * Parse the IO Name response
	 *
	 * @param pRestResponse
	 *        The response returned by the IO Name webservice
	 */
	private void parseDeleteImageResponse(RestResponse pRestResponse) {
		BaseResponse baseResponse = JsonParserHelper.getInstance().parseJsonAndShowError(this, pRestResponse, BaseResponse.class);
		if (baseResponse != null && baseResponse.status.code == RESPONSE_CODE.RESPONSE_OK) {
			mSite.getImages().remove(mViewPagerGallery.getCurrentItem());
			mGalleryPagerAdapter.setSite(mSite);
			setActionBarTitle(mViewPagerGallery.getCurrentItem() + 1, mSite.getImages().size());

			// If there are no images left in the gallery, go back to the previous activity
			if (mSite.getImages().size() == 0) {
				finish();
			}
		}
	}

	@Override
	public Loader<RestResponse> onCreateLoader(int pLoaderId, Bundle pParams) {
		WebserviceAsync loader = WebserviceAsync.newInstance(this);
		loader.setParams(pParams);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<RestResponse> loader, RestResponse response) {
		if (loader.getId() == LOADER_ID.DELETE_IMAGE) {
			parseDeleteImageResponse(response);
		} else {
			MyLog.e(LOG_TAG, "Unknown loader finished: " + loader.getId());
		}
	}

	@Override
	public void onLoaderReset(Loader<RestResponse> loader) {
		// Do nothing
	}

	@Override
	public void onClick(View view) {
		ValueAnimator backGroundValueAnimator;
		if (getSupportActionBar().isShowing()) {
			getSupportActionBar().hide();
			backGroundValueAnimator = ObjectAnimator.ofInt(mViewPagerGallery, "backgroundColor", getResources().getColor(R.color.background),
					getResources().getColor(android.R.color.black));
		} else {
			getSupportActionBar().show();
			backGroundValueAnimator = ObjectAnimator.ofInt(mViewPagerGallery, "backgroundColor", getResources().getColor(android.R.color.black),
					getResources().getColor(R.color.background));
		}
		backGroundValueAnimator.setEvaluator(new ArgbEvaluator());
		backGroundValueAnimator.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
		backGroundValueAnimator.start();
	}
}
