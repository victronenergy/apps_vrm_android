package nl.victronenergy.fragments;

import nl.victronenergy.R;
import nl.victronenergy.adapters.SiteFragmentStatePagerAdapter;
import nl.victronenergy.models.SiteListData;
import nl.victronenergy.util.AnalyticsConstants;
import nl.victronenergy.util.Constants.BUNDLE;
import nl.victronenergy.util.Constants.DEFAULT_VALUE;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

/**
 * Shows an overview of the sites owned by this user in a pagerAdapter
 *
 * @author M2Mobi
 */
public class FragmentSiteViewPager extends Fragment implements OnPageChangeListener {
	private static final String LOG_TAG = "FragmentSiteViewPager";

	private SiteViewPagerCallBacks mSiteViewPagerCallBacks;

	private int mSelectedSiteIndex = DEFAULT_VALUE.SITE_INDEX;
	private SiteFragmentStatePagerAdapter mPagerAdapter;
	private ViewPager mViewPager;
	private SiteListData mSiteData;

	/**
	 * Interface used to notify the activity of certain events in the SiteViewPager fragment
	 */
	public interface SiteViewPagerCallBacks {
		/**
		 * Notifies the Activity that the user swiped between the sites
		 *
		 * @param position
		 *        The position where the user swiped to
		 */
		public void onPageChange(int position);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mSelectedSiteIndex = savedInstanceState.getInt(BUNDLE.SITE_ARRAY_INDEX, DEFAULT_VALUE.SITE_INDEX);
			mSiteData = (SiteListData) savedInstanceState.getSerializable(BUNDLE.SITE_LIST_DATA);
		} else {
			if (getArguments() != null) {
				mSelectedSiteIndex = getArguments().getInt(BUNDLE.SITE_ARRAY_INDEX, DEFAULT_VALUE.SITE_INDEX);
				mSiteData = (SiteListData) getArguments().getSerializable(BUNDLE.SITE_LIST_DATA);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_site_viewpager, null);
		initializeView(view);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mSiteViewPagerCallBacks = (SiteViewPagerCallBacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement SiteDetailCallBacks");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(BUNDLE.SITE_ARRAY_INDEX, mSelectedSiteIndex);
		outState.putSerializable(BUNDLE.SITE_LIST_DATA, mSiteData);
	}

	/**
	 * Initialize this view
	 *
	 * @param view
	 *        The view that needs to be initialized
	 */
	private void initializeView(View view) {

		// Setup the viewpager
		mViewPager = (ViewPager) view.findViewById(R.id.viewpager_site_detail);
		mPagerAdapter = new SiteFragmentStatePagerAdapter(getChildFragmentManager());
		mPagerAdapter.setSiteData(mSiteData);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setCurrentItem(mSelectedSiteIndex);
		mViewPager.setOnPageChangeListener(this);
	}

	@Override
	public void onPageScrollStateChanged(int position) {
		// Auto-generated method stub
	}

	@Override
	public void onPageScrolled(int position, float arg1, int arg2) {
		// Auto-generated method stub
	}

	@Override
	public void onPageSelected(int position) {
		EasyTracker.getTracker().sendEvent(AnalyticsConstants.CAT_GESTURE_ACTION, AnalyticsConstants.SWIPE, AnalyticsConstants.SITE_SWIPE, null);
		mSelectedSiteIndex = position;
		mSiteViewPagerCallBacks.onPageChange(position);
	}

	/**
	 * Set the site data
	 *
	 * @param pSiteData
	 *        The site data to set
	 */
	public void setSiteData(SiteListData pSiteData) {
		mSiteData = pSiteData;
		if (mPagerAdapter != null) {
			mPagerAdapter.setSiteData(pSiteData);
		}
	}

	/**
	 * Sets the currently selected site
	 *
	 * @param pSiteIndex
	 */
	public void setSelectedSiteIndex(int pSiteIndex) {
		mViewPager.setCurrentItem(pSiteIndex);
		mSelectedSiteIndex = pSiteIndex;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Toast.makeText(getActivity(), "VIEWPAGER", Toast.LENGTH_SHORT).show();
		super.onActivityResult(requestCode, resultCode, data);
	}
}
