package nl.victronenergy.activities;

import nl.victronenergy.R;
import nl.victronenergy.fragments.FragmentSiteDetail.SiteDetailCallBacks;
import nl.victronenergy.fragments.FragmentSiteViewPager;
import nl.victronenergy.fragments.FragmentSiteViewPager.SiteViewPagerCallBacks;
import nl.victronenergy.models.Site;
import nl.victronenergy.util.AnalyticsConstants;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.SmsDelivered;
import nl.victronenergy.util.SmsSent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;


/**
 * Shows a viewpager with detailed info of the site status.<br/>
 * Created by M2Mobi on 20-3-14.
 */
public class ActivitySiteDetail extends ActionBarActivity implements SiteDetailCallBacks, AnimationListener, SiteViewPagerCallBacks {
	private static final String LOG_TAG = "ActivitySiteDetail";

	/* Used to show refresh animation in the actionbar */
	private ImageView mImageViewRefresh;
	private MenuItem mMenuItemRefresh;
	private Animation mAnimationRotate;
	private boolean mIsLoadingFinished;

	/* SMS Objects */
	private SmsSent mSmsSent = null;
	private SmsDelivered mSmsDelivered = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_site_detail);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Setup the fragment
		Fragment fragmentSiteDetailViewPager = null;

		// Try to restore the fragment from the savedInstance
		if (savedInstanceState != null) {
			fragmentSiteDetailViewPager = getSupportFragmentManager().getFragment(savedInstanceState, Constants.FRAGMENT_TAG.SITE_VIEWPAGER);
		}

		// Fragment is null, create a new fragment
		if (fragmentSiteDetailViewPager == null) {
			fragmentSiteDetailViewPager = new FragmentSiteViewPager();
			if (getIntent().hasExtra(Constants.BUNDLE.SITE_LIST_DATA)) {
				Bundle bundleArguments = new Bundle();
				bundleArguments.putSerializable(Constants.BUNDLE.SITE_LIST_DATA, getIntent().getSerializableExtra(Constants.BUNDLE.SITE_LIST_DATA));
				bundleArguments.putInt(Constants.BUNDLE.SITE_ARRAY_INDEX,
						getIntent().getIntExtra(Constants.BUNDLE.SITE_ARRAY_INDEX, Constants.DEFAULT_VALUE.SITE_INDEX));
				fragmentSiteDetailViewPager.setArguments(bundleArguments);
			}
		}

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame_site_detail, fragmentSiteDetailViewPager, Constants.FRAGMENT_TAG.SITE_VIEWPAGER).commit();

		// Prepare for refresh
		LayoutInflater inflater = LayoutInflater.from(this);
		mImageViewRefresh = (ImageView) inflater.inflate(R.layout.refresh_action_item, null);

		// Load the animation
		mAnimationRotate = AnimationUtils.loadAnimation(this, R.anim.anim_refresh);
		mAnimationRotate.setAnimationListener(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the historic data fragment to the saved instance state
		Fragment fragmentSiteDetailViewPager = getSupportFragmentManager().findFragmentByTag(Constants.FRAGMENT_TAG.SITE_VIEWPAGER);
		if (fragmentSiteDetailViewPager != null) {
			getSupportFragmentManager().putFragment(outState, Constants.FRAGMENT_TAG.SITE_VIEWPAGER, fragmentSiteDetailViewPager);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (mSmsSent != null && mSmsDelivered != null) {
			unregisterReceiver(mSmsSent);
			unregisterReceiver(mSmsDelivered);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		registerReceiver(mSmsSent, new IntentFilter(Constants.SMS_COMMAND_SENT));
		registerReceiver(mSmsDelivered, new IntentFilter(Constants.SMS_COMMAND_DELIVERED));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Checks which action bar button is pressed
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.button_refresh) {
			startRefreshAnimation(item);
		} else if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onHistoricDataClicked(Site pSite) {
		Intent intentHistoricData = new Intent(this, ActivityHistoricData.class);
		intentHistoricData.putExtra(Constants.BUNDLE.SITE_OBJECT, pSite);
		startActivity(intentHistoricData);
	}

	@Override
	public void onIOSettingsClicked(Site pSite) {
		Intent intentIOSettings = new Intent(this, ActivityIOGeneratorSettings.class);
		intentIOSettings.putExtra(Constants.BUNDLE.SITE_OBJECT, pSite);
		startActivity(intentIOSettings);
	}

	@Override
	public void onSiteDetailLoadingFinished() {
		mIsLoadingFinished = true;
	}

	@Override
	public void onSiteDetailDestroyOptionsMenu() {
		if (mImageViewRefresh != null) {
			mImageViewRefresh.clearAnimation();
		}
		if (mMenuItemRefresh != null) {
			MenuItemCompat.setActionView(mMenuItemRefresh, null);
		}
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
		if (mIsLoadingFinished) {
			mIsLoadingFinished = false;

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

	/**
	 * Starts the refresh animation in the actionbar
	 *
	 * @param menuItem
	 *        The menu item that needs to be replaced with the refresh animation
	 */
	private void startRefreshAnimation(MenuItem menuItem) {
		mMenuItemRefresh = menuItem;

		// Apply the animation to our View
		mImageViewRefresh.startAnimation(mAnimationRotate);

		// Apply the View to our MenuItem
		MenuItemCompat.setActionView(menuItem, mImageViewRefresh);
	}

	@Override
	public void onPageChange(int position) {
		// We have to implement the callback, but we don't have to do anything with it on phones
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Toast.makeText(this, "LA", Toast.LENGTH_SHORT).show();
		super.onActivityResult(requestCode, resultCode, data);
	}
}
