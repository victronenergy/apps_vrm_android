/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.activities;

import nl.victronenergy.R;
import nl.victronenergy.fragments.FragmentHistoricData;
import nl.victronenergy.fragments.FragmentIOGeneratorSettings;
import nl.victronenergy.fragments.FragmentSiteDetail.SiteDetailCallBacks;
import nl.victronenergy.fragments.FragmentSiteSummary;
import nl.victronenergy.fragments.FragmentSiteSummary.OnSiteSelectedListener;
import nl.victronenergy.fragments.FragmentSiteViewPager;
import nl.victronenergy.fragments.FragmentSiteViewPager.SiteViewPagerCallBacks;
import nl.victronenergy.models.Site;
import nl.victronenergy.models.SiteListData;
import nl.victronenergy.models.SiteListResponse;
import nl.victronenergy.models.UserResponse;
import nl.victronenergy.util.AnalyticsConstants;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.BUNDLE;
import nl.victronenergy.util.Constants.FRAGMENT_TAG;
import nl.victronenergy.util.SmsDelivered;
import nl.victronenergy.util.SmsSent;
import nl.victronenergy.util.UserUtils;
import nl.victronenergy.util.webservice.JsonParserHelper;
import nl.victronenergy.util.webservice.RestResponse;
import nl.victronenergy.util.webservice.WebserviceAsync;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.analytics.tracking.android.EasyTracker;

/**
 * The Main Activity. On tablets shows the sitesummary in a sliding menu and the sitedetails on the main screen. On
 * phones the sitesummary and sitedetails are shown separately.<br/>
 * <br/>
 * Created by Victron Energy on 27-2-14.
 */
public class ActivitySiteSummary extends ActionBarActivity implements LoaderManager.LoaderCallbacks<RestResponse>, AnimationListener,
		OnSiteSelectedListener, SiteDetailCallBacks, SiteViewPagerCallBacks {

	/** Tag used for logging */
	private static final String LOG_TAG = "ActivitySiteSummary";

	/** Used to show refresh animation in the actionbar */
	private ImageView mImageViewRefresh;
	private MenuItem mMenuItemRefresh;
	private Animation mAnimationRotate;

	/** Used to indicate if all loading tasks are finished */
	private boolean mAllTasksFinishedLoading = false;
	private boolean mSiteDetailLoadingFinished = false;
	private boolean mSiteSummaryLoadingFinished = false;

	/** The data of all the sites */
	private SiteListData mSiteData;

	/** SMS Objects */
	private SmsSent mSmsSent = null;
	private SmsDelivered mSmsDelivered = null;

	/** Indicates if the current device is a phone or a tablet */
	private boolean mIsPhone;

	/** Drawer menu layout */
	private DrawerLayout mDrawerLayout;

	/** View used as the left Drawer */
	private View mLeftDrawer;

	/** Toggle for the ActionBar drawer */
	private ActionBarDrawerToggle mDrawerToggle;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Check if the current device is a phone or tablet
		mIsPhone = getResources().getBoolean(R.bool.is_phone);

		// Find and setup the DrawerLayout
		View viewDrawerLayout = findViewById(R.id.drawer_layout);
		if (viewDrawerLayout != null) {
			mDrawerLayout = (DrawerLayout) viewDrawerLayout;
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.menu_open, R.string.menu_close);
			mLeftDrawer = findViewById(R.id.frame_site_summary);

			// Set the drawer toggle as the DrawerListener
			mDrawerLayout.setDrawerListener(mDrawerToggle);

			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}

		// Create sms objects
		mSmsSent = new SmsSent();
		mSmsDelivered = new SmsDelivered();

		initView(savedInstanceState);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		unregisterReceiver(mSmsSent);
		unregisterReceiver(mSmsDelivered);
	}

	@Override
	protected void onResume() {
		super.onResume();

		registerReceiver(mSmsSent, new IntentFilter(Constants.SMS_COMMAND_SENT));
		registerReceiver(mSmsDelivered, new IntentFilter(Constants.SMS_COMMAND_DELIVERED));

		updateActionbarTitle();
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance().setContext(this);
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance().setContext(this);
		EasyTracker.getInstance().activityStop(this);
	}

	/**
	 * Initialize the view and load required data
	 */
	private void initView(Bundle savedInstanceState) {

		if (savedInstanceState == null) {
			// Show the list of site summaries if the summary frame
			if (findViewById(R.id.frame_site_summary) != null) {
				getSupportFragmentManager().beginTransaction().replace(R.id.frame_site_summary, new FragmentSiteSummary(), FRAGMENT_TAG.SITE_SUMMARY)
						.commit();
			}

			// Show the site details in the content frame (tablet only)
			if (findViewById(R.id.frame_content) != null) {
				getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, new FragmentSiteViewPager(), FRAGMENT_TAG.SITE_VIEWPAGER)
						.commit();
			}
		}

		// Prepare for refresh
		LayoutInflater inflater = LayoutInflater.from(this);
		mImageViewRefresh = (ImageView) inflater.inflate(R.drawable.refresh_action_item, null);

		// Load the animation
		mAnimationRotate = AnimationUtils.loadAnimation(this, R.anim.anim_refresh);
		mAnimationRotate.setAnimationListener(this);

		callSiteListLoader();
	}

	/**
	 * Update the actionbar title according to the amount of sites in the list
	 */
	private void updateActionbarTitle() {
		if (mSiteData != null && mSiteData.sites != null) {
			getSupportActionBar().setTitle(getResources().getQuantityString(R.plurals.actionbar_title_site_summary_plural, mSiteData.sites.length));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		// Refresh data, should not return true as the detail screen should listen for the menu press too
		if (item.getItemId() == R.id.button_refresh) {
			EasyTracker.getTracker().sendEvent(AnalyticsConstants.CAT_UI_ACTION, AnalyticsConstants.BUTTON_PRESS,
					AnalyticsConstants.REFRESH_SITELIST, null);
			refreshData(item);
		}

		// Make sure that the list of site summary is opened when search is clicked
		if (item.getItemId() == R.id.button_search) {
			if (mDrawerLayout != null) {
				mDrawerLayout.openDrawer(mLeftDrawer);
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Refreshed the data and starts refresh animation
	 */
	private void refreshData(MenuItem menuItem) {
		mMenuItemRefresh = menuItem;

		// Apply the animation to our View
		mImageViewRefresh.startAnimation(mAnimationRotate);

		// Apply the View to our MenuItem
		MenuItemCompat.setActionView(menuItem, mImageViewRefresh);

		callSiteListLoader();
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// Do nothing
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// Do nothing
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (mAllTasksFinishedLoading) {
			// Reset loading finished flags
			mSiteDetailLoadingFinished = false;
			mAllTasksFinishedLoading = false;
			mSiteSummaryLoadingFinished = false;

			// Clear animation
			if (mImageViewRefresh != null) {
				mImageViewRefresh.clearAnimation();
				MenuItemCompat.setActionView(mMenuItemRefresh, null);
			}
		} else {
			if (mImageViewRefresh != null) {
				mImageViewRefresh.startAnimation(mAnimationRotate);
			}
		}
	}

	@Override
	public void onSiteSelected(final int position) {
		if (mIsPhone) {
			// On phones we start an activity with the sitesummary
			Intent intentSiteSummary = new Intent(this, ActivitySiteDetail.class);
			intentSiteSummary.putExtra(BUNDLE.SITE_LIST_DATA, mSiteData);
			intentSiteSummary.putExtra(BUNDLE.SITE_ARRAY_INDEX, position);
			startActivity(intentSiteSummary);
		} else {
			// On tablets just swipe the detail fragment to the correct site
			final FragmentSiteViewPager fragmentSiteViewPager = (FragmentSiteViewPager) getSupportFragmentManager().findFragmentByTag(
					FRAGMENT_TAG.SITE_VIEWPAGER);

			if (fragmentSiteViewPager != null) {
				// If this fragment is not visible pop the backstack to the main overview
				if (!fragmentSiteViewPager.isVisible()) {
					getSupportFragmentManager().popBackStack();
				}

				// Move the fragment to the selected site, this has to be delayed or else it won't move
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						fragmentSiteViewPager.setSelectedSiteIndex(position);
					}
				}, 100);

				// Close the drawer if there is one
				if (mDrawerLayout != null) {
					mDrawerLayout.closeDrawers();
				}
			}
		}
	}

	@Override
	public void onHistoricDataClicked(Site pSite) {
		// Setup the arguments
		Bundle arguments = new Bundle();
		arguments.putSerializable(BUNDLE.SITE_OBJECT, pSite);

		// Create the historic data fragment
		FragmentHistoricData fragmentHistoric = new FragmentHistoricData();
		fragmentHistoric.setArguments(arguments);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.frame_content, fragmentHistoric, FRAGMENT_TAG.SITE_HISTORIC_DATA);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onIOSettingsClicked(Site pSite) {
		// Setup the arguments
		Bundle arguments = new Bundle();
		arguments.putSerializable(BUNDLE.SITE_OBJECT, pSite);

		// Create the IO Settings fragment
		FragmentIOGeneratorSettings fragmentIOGeneratorSettings = new FragmentIOGeneratorSettings();
		fragmentIOGeneratorSettings.setArguments(arguments);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.frame_content, fragmentIOGeneratorSettings, FRAGMENT_TAG.SITE_IO_SETTINGS);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onSiteDetailLoadingFinished() {
		mSiteDetailLoadingFinished = true;

		// Check if both tasks are finished loading
		if (mSiteDetailLoadingFinished && mSiteSummaryLoadingFinished) {
			mAllTasksFinishedLoading = true;
		}
	}

	@Override
	public void onSiteDetailDestroyOptionsMenu() {
		// Make sure the refresh icon doesn't keep spinning or gets duplicated
		if (mImageViewRefresh != null) {
			mImageViewRefresh.clearAnimation();
		}
		if (mMenuItemRefresh != null) {
			MenuItemCompat.setActionView(mMenuItemRefresh, null);
		}
	}

	@Override
	public void onPageChange(int position) {
		// Pass data to the sitelist fragment
		FragmentSiteSummary fragmentSiteSummary = (FragmentSiteSummary) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG.SITE_SUMMARY);
		if (fragmentSiteSummary != null) {
			fragmentSiteSummary.setSelectedSitePosition(position);
		}
	}

	/**
	 * Call loader to get sitelist
	 */
	private void callSiteListLoader() {
		Bundle params = new Bundle();
		params.putString(Constants.POST.URI, Constants.GET_SITES);

		if (getSupportLoaderManager().getLoader(Constants.LOADER_ID.SITELIST) == null) {
			getSupportLoaderManager().initLoader(Constants.LOADER_ID.SITELIST, params, this);
		} else {
			getSupportLoaderManager().restartLoader(Constants.LOADER_ID.SITELIST, params, this);
		}
	}

	/**
	 * Parses the sitelist response returned from the webservice
	 *
	 * @param pRestResponse
	 *        The response returned by the webservice
	 */
	private void parseSiteListResponse(RestResponse pRestResponse) {
		SiteListResponse siteListResponse = JsonParserHelper.getInstance().parseJsonAndShowError(this, pRestResponse, SiteListResponse.class);
		if (siteListResponse != null) {
			switch (siteListResponse.status.code) {
				case Constants.RESPONSE_CODE.RESPONSE_OK:
					mSiteData = siteListResponse.data;
					mSiteData.orderSitesByStatus();

					// Pass data to the sitelist fragment
					FragmentSiteSummary fragmentMenu = (FragmentSiteSummary) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG.SITE_SUMMARY);
					if (fragmentMenu != null) {
						fragmentMenu.setSiteData(mSiteData);
					}

					// Pass data to the site detail fragment
					FragmentSiteViewPager fragmentSiteDetail = (FragmentSiteViewPager) getSupportFragmentManager().findFragmentByTag(
							FRAGMENT_TAG.SITE_VIEWPAGER);
					if (fragmentSiteDetail != null) {
						fragmentSiteDetail.setSiteData(mSiteData);
					}

					updateActionbarTitle();

					mSiteSummaryLoadingFinished = true;
					break;
				case Constants.RESPONSE_CODE.RESPONSE_SESSION_ID:
					callLoginLoader();
					break;
				default:
					mSiteSummaryLoadingFinished = true;
					break;
			}
		} else {
			mSiteSummaryLoadingFinished = true;
		}

		// Check if both tasks are finished loading
		if ((mIsPhone || mSiteDetailLoadingFinished) && mSiteSummaryLoadingFinished) {
			mAllTasksFinishedLoading = true;
		}
	}

	/**
	 * Parse the login response, used when the session got timed out
	 *
	 * @param pRestResponse
	 *        The response returned by the login webservice
	 */
	private void parseLoginResponse(RestResponse pRestResponse) {
		UserResponse userResponse = JsonParserHelper.getInstance().parseJsonAndShowError(this, pRestResponse, UserResponse.class);
		if (userResponse != null) {
			if (userResponse.status.code == Constants.RESPONSE_CODE.RESPONSE_OK) {
				UserUtils.saveSessionID(this, userResponse.data.user.sessionId);
				callSiteListLoader();
			}
		} else {
			mAllTasksFinishedLoading = true;
		}
	}

	@Override
	public Loader<RestResponse> onCreateLoader(int loaderId, Bundle params) {
		WebserviceAsync loader = WebserviceAsync.newInstance(this);
		loader.setParams(params);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<RestResponse> loader, RestResponse response) {
		switch (loader.getId()) {
			case Constants.LOADER_ID.SITELIST:
				parseSiteListResponse(response);
				break;
			case Constants.LOADER_ID.LOGIN:
				parseLoginResponse(response);
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<RestResponse> loader) {
		// Do nothing
	}

	// =============================================================
	// Auto relogin after time out
	// =============================================================

	/**
	 * Call the loader to try login
	 */
	private void callLoginLoader() {
		String username = UserUtils.getUsername(this);
		String password = UserUtils.getPassword(this);

		Bundle params = new Bundle();
		params.putString(Constants.POST.URI, Constants.WEBSERVICE_USER_LOGIN);
		params.putString(Constants.POST.EMAIL, username);
		params.putString(Constants.POST.PASSWORD, password);

		if (getSupportLoaderManager().getLoader(Constants.LOADER_ID.LOGIN) == null) {
			getSupportLoaderManager().initLoader(Constants.LOADER_ID.LOGIN, params, this);
		} else {
			getSupportLoaderManager().restartLoader(Constants.LOADER_ID.LOGIN, params, this);
		}
	}
}
