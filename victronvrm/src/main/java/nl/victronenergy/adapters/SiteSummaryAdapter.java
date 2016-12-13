package nl.victronenergy.adapters;

import java.util.ArrayList;
import java.util.Date;

import nl.victronenergy.R;
import nl.victronenergy.fragments.FragmentSiteSummary;
import nl.victronenergy.models.AttributesResponse;
import nl.victronenergy.models.Site;
import nl.victronenergy.models.SiteListData;
import nl.victronenergy.models.UserResponse;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.DownloadStatus;
import nl.victronenergy.util.Constants.LOADER_ID;
import nl.victronenergy.util.Constants.RESPONSE_CODE;
import nl.victronenergy.util.LoaderUtils;
import nl.victronenergy.util.UserUtils;
import nl.victronenergy.util.webservice.JsonParserHelper;
import nl.victronenergy.util.webservice.RestResponse;
import nl.victronenergy.util.webservice.WebserviceAsync;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Site adapter used to show a summary of all the sites grouped into 3 sections.
 * <ul>
 * <li>Sites with alarm</li>
 * <li>Sites without alarm</li>
 * <li>Sites with old data</li>
 * </ul>
 *
 * @author M2Mobi
 */
public class SiteSummaryAdapter extends BaseAdapter implements LoaderCallbacks<RestResponse> {

	/** TAG to identify log messages from this class */
	private static final String LOG_TAG = "SiteSummaryAdapter";

	/** The amount of sections we want to show in the list */
	private static final int SECTION_COUNT = 3;

	/** Used to see check if we should pause downloading or not */
	private boolean mIsScrolling = false;

	/** Used to get access to the loadermanager */
	private FragmentActivity mActivity;

	/** Used to pass a reference to the widgets to handle the onclick event of those widgets */
	private FragmentSiteSummary mFragmentSiteSummary;

	/** Full list of sites */
	private SiteListData mSiteListData;

	/** The id of the site that is currently selected, used to highlight the list item on tablets */
	private int mSelectedSiteId;

	/** A list of site indices that are currently visible */
	private ArrayList<Integer> mVisibleSiteIndices;

	/** Current amount of sites in each section, 0: In alarm, 1: Ok, 2: Old */
	private int[] mSectionSizes;

	/** Contains the string that we should use to filter the visible sites */
	private String mSearchFilter;

	/** Some things are done a little different on phones so we need to know what kind of device this is */
	private boolean mIsPhone;

	/** Used to restore the padding. A bug in setBackGroundResource removes the padding. */
	private final int mLayoutPadding;

	/** Drawables used by the pager indicator */
	private final Drawable mDrawableIndicatorSelected;
	private final Drawable mDrawableIndicatorUnselected;

	/**
	 * Constructor
	 *
	 * @param pFragmentSiteSummary
	 */
	public SiteSummaryAdapter(FragmentSiteSummary pFragmentSiteSummary) {
		mFragmentSiteSummary = pFragmentSiteSummary;
		mActivity = mFragmentSiteSummary.getActivity();
		mVisibleSiteIndices = new ArrayList<Integer>();
		mSectionSizes = new int[SECTION_COUNT];
		mSelectedSiteId = -1;
		mIsPhone = mActivity.getResources().getBoolean(R.bool.is_phone);
		mLayoutPadding = mActivity.getResources().getDimensionPixelSize(R.dimen.spacing_medium);

		mDrawableIndicatorSelected = mActivity.getResources().getDrawable(R.drawable.pagecontrol_blue);
		mDrawableIndicatorUnselected = mActivity.getResources().getDrawable(R.drawable.pagecontrol_lightgrey);

		resetSectionSizes();
	}

	@Override
	public int getCount() {
		return mVisibleSiteIndices.size();
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		ViewHolder viewHolder;

		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(R.layout.list_item_site, parent, false);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Site site = mSiteListData.sites[mVisibleSiteIndices.get(position)];

		// On tablets make the background of the selected site dark
		if (!mIsPhone) {
			if (site.getIdSite() == mSelectedSiteId) {
				viewHolder.viewSiteData.setBackgroundResource(R.drawable.background_site_summary_item_pressed);
			} else {
				viewHolder.viewSiteData.setBackgroundResource(R.drawable.background_site_summary_item);
			}
			// Restore the padding that got lost by settings the background
			viewHolder.viewSiteData.setPadding(mLayoutPadding, mLayoutPadding, mLayoutPadding, mLayoutPadding);
		}

		// Show the header if this is a header view else hide it
		if (isHeader(position)) {
			initSiteHeaderView(viewHolder, site.getSiteStatus());
		} else {
			viewHolder.textViewSummaryHeader.setVisibility(View.GONE);
		}

		initSiteItemView(viewHolder, site, site.getSiteStatus());

		// If the attributes are not loaded yet, try to load them
		if (site.getSiteStatus() != Constants.SITE_STATUS.OLD) {
			if (site.shouldLoadAttributes()) {
				// Only start loading when loading is not paused to improve performance when scrolling
				if (!mIsScrolling) {
					site.setAttributeDownloadStatus(DownloadStatus.DOWNLOAD_IN_PROGRESS);
					callAttributesLoader(site.getIdSite());
				}
			}
		}

		((ViewGroup) convertView).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

		return convertView;
	}

	/**
	 * Initialize the header view
	 *
	 * @param viewHolder
	 *        The view to setup
	 * @param pSectionIndex
	 *        The index of this section
	 * @return The initialized view
	 */
	private void initSiteHeaderView(ViewHolder viewHolder, int pSectionIndex) {
		viewHolder.textViewSummaryHeader.setVisibility(View.VISIBLE);

		switch (pSectionIndex) {
			case Constants.SITE_STATUS.ALARM:
				viewHolder.textViewSummaryHeader.setText(mActivity.getText(R.string.sitesummary_section_alarm));
				break;
			case Constants.SITE_STATUS.OK:
				viewHolder.textViewSummaryHeader.setText(mActivity.getText(R.string.sitesummary_section_ok));
				break;
			case Constants.SITE_STATUS.OLD:
				viewHolder.textViewSummaryHeader.setText(mActivity.getText(R.string.sitesummary_section_old_data));
				break;
		}
	}

	/**
	 * Initialize site item view
	 *
	 * @param viewHolder
	 *        The view to use and initialize
	 * @param pSite
	 *        The site to show the data of
	 * @param pSiteStatus
	 *        The status of this site
	 * @return Initialized view
	 */
	private void initSiteItemView(final ViewHolder viewHolder, final Site pSite, int pSiteStatus) {
		viewHolder.textViewSiteName.setText(pSite.getName());

		switch (pSiteStatus) {
			case Constants.SITE_STATUS.OK:
				viewHolder.textViewLastUpdate.setVisibility(View.GONE);
				break;
			case Constants.SITE_STATUS.ALARM:
				viewHolder.textViewLastUpdate.setVisibility(View.VISIBLE);

				// Create pretty timestamp of alarmStarted
				CharSequence alarmStarted = DateUtils.getRelativeTimeSpanString(pSite.getAlarmStartedTimestampInMS(), new Date().getTime(),
						DateUtils.SECOND_IN_MILLIS, 0);

				viewHolder.textViewLastUpdate.setText(String.format(mActivity.getString(R.string.sitesummary_last_update_alarm), alarmStarted));
				break;
			case Constants.SITE_STATUS.OLD:
				viewHolder.textViewLastUpdate.setVisibility(View.VISIBLE);

				// Create pretty timestamp of last update timestamp
				CharSequence lastUpdate = mActivity.getString(R.string.not_available);
				if (pSite.getLastTimeStampInSeconds() > 0) {
					lastUpdate = DateUtils.getRelativeTimeSpanString(pSite.getLastTimestampInMS(), new Date().getTime(), DateUtils.SECOND_IN_MILLIS,
							0);
				}

				viewHolder.textViewLastUpdate.setText(String.format(mActivity.getString(R.string.sitesummary_last_update_old), lastUpdate));
				break;
		}

		// Don't show widgets/loading for sites with old data
		if (pSiteStatus == Constants.SITE_STATUS.OLD) {
			viewHolder.progressBarLoading.setVisibility(View.GONE);
			viewHolder.viewPagerWidgets.setVisibility(View.GONE);
			viewHolder.layoutPagerIndicator.setVisibility(View.GONE);
		} else {
			// For other sites we show the widgets when we are not scrolling and not loading
			if (!mIsScrolling && pSite.areAttributesLoaded()) {
				viewHolder.viewPagerWidgets.setAdapter(new WidgetViewPagerAdapter(mFragmentSiteSummary, pSite.getWidgets(), pSite.getIdSite()));

				// We don't want the pages to change when this view is recreated so save the current page index
				viewHolder.viewPagerWidgets.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
					@Override
					public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
						// Do nothing
					}

					@Override
					public void onPageSelected(int position) {
						pSite.setSelectedWidgetPage(position);
						createWidgetPagerIndicator(viewHolder.layoutPagerIndicator, pSite.getSelectedWidgetPage(),
								viewHolder.viewPagerWidgets.getChildCount());
					}

					@Override
					public void onPageScrollStateChanged(int state) {
						// Do nothing
					}
				});

				viewHolder.viewPagerWidgets.setCurrentItem(pSite.getSelectedWidgetPage());
				createWidgetPagerIndicator(viewHolder.layoutPagerIndicator, pSite.getSelectedWidgetPage(),
						viewHolder.viewPagerWidgets.getChildCount());

				viewHolder.progressBarLoading.setVisibility(View.INVISIBLE);
				viewHolder.viewPagerWidgets.setVisibility(View.VISIBLE);
				// Only show the widget pager indicator if we have at least 2 pages of widgets
				if (viewHolder.viewPagerWidgets.getChildCount() > 1) {
					viewHolder.layoutPagerIndicator.setVisibility(View.VISIBLE);
				} else {
					viewHolder.layoutPagerIndicator.setVisibility(View.INVISIBLE);
				}
			} else {
				viewHolder.progressBarLoading.setVisibility(View.VISIBLE);
				viewHolder.viewPagerWidgets.setVisibility(View.INVISIBLE);
				viewHolder.layoutPagerIndicator.setVisibility(View.INVISIBLE);
			}
		}
	}

	@Override
	public Object getItem(int position) {
		if (mSiteListData.sites[mVisibleSiteIndices.get(position)] != null) {
			return mSiteListData.sites[mVisibleSiteIndices.get(position)];
		}
		return null;
	}

	/**
	 * Returns the original index of the site in the full site array
	 */
	@Override
	public long getItemId(int position) {
		for (int i = 0; i < mSiteListData.sites.length; i++) {
			if (mSiteListData.sites[i].getIdSite() == mSiteListData.sites[mVisibleSiteIndices.get(position)].getIdSite()) {
				return i;
			}
		}
		return Constants.DEFAULT_VALUE.SITE_INDEX;
	}

	/**
	 * Set the site data
	 *
	 * @param pSiteListData
	 */
	public void setSites(SiteListData pSiteListData) {
		mSiteListData = pSiteListData;

		// By default select the first site in the list
		if (mSelectedSiteId == -1 && mSiteListData != null && mSiteListData.sites != null && mSiteListData.sites.length > 0) {
			mSelectedSiteId = mSiteListData.sites[0].getIdSite();
		}

		filterSites();
		notifyDataSetChanged();
	}

	/**
	 * Checks if this list item is a header view or not
	 *
	 * @param position
	 *        The position of the item you would like to check
	 * @return True if this is a header view, false if it's a normal list item
	 */
	private boolean isHeader(int position) {
		// Iterate through the sections to see if the current position is in that section
		int positionInSection = position;
		for (int i = 0; i < SECTION_COUNT; i++) {
			if (getSectionSize(i) > 0) {

				// If the position is the first item in this section it's a header else it's not
				if (positionInSection == 0) {
					return true;
				} else if (positionInSection < getSectionSize(i)) {
					return false;
				}
			}

			// Decrease position by section size when the position was not in this section
			positionInSection -= getSectionSize(i);
		}

		return false;
	}

	/**
	 * Returns the size of a certain section
	 *
	 * @param pSectionIndex
	 *        The index of the section you want to know the size of
	 * @return The size of the section
	 */
	public int getSectionSize(int pSectionIndex) {
		return mSectionSizes[pSectionIndex];
	}

	/**
	 * Set the search filter and make sure sites are filtered
	 *
	 * @param pSearchFilter
	 *        The string you want to filter on
	 */
	public void setSearchFilter(String pSearchFilter) {
		mSearchFilter = pSearchFilter;
		filterSites();
		notifyDataSetChanged();
	}

	/**
	 * Clear the search filter
	 */
	public void clearSearchFilter() {
		mSearchFilter = null;
		filterSites();
		notifyDataSetChanged();
	}

	/**
	 * Filter sites that should be shown according to the search filter
	 */
	private void filterSites() {
		mVisibleSiteIndices.clear();
		resetSectionSizes();

		if (mSiteListData == null) {
			return;
		}

		for (int i = 0; i < mSiteListData.sites.length; i++) {
			if (mSearchFilter == null || mSiteListData.sites[i].getName().toLowerCase().contains(mSearchFilter.toLowerCase())) {
				mVisibleSiteIndices.add(i);
				mSectionSizes[mSiteListData.sites[i].getSiteStatus()]++;
			}
		}
	}

	/**
	 * Reset the section sizes to zero
	 */
	private void resetSectionSizes() {
		for (int i = 0; i < mSectionSizes.length; i++) {
			mSectionSizes[i] = 0;
		}
	}

	/**
	 * Set a boolean indicating that the user is scrolling. This is used to prevent loading data for views that are
	 * scrolled passed. If we don't do this scrolling is a bit slow.<br/>
	 *
	 * @param pIsScrolling
	 *        True if the user is scrolling, false if not
	 */
	public void setIsScrolling(boolean pIsScrolling) {
		mIsScrolling = pIsScrolling;

		// If we stopped scrolling call notifyDataSetChanged so the list gets redrawn
		if (!mIsScrolling) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Sets which site is selected
	 *
	 * @param position
	 *        The position of the site in the full site list
	 */
	public void setSelectedSitePosition(int position) {
		if (mSiteListData != null && position < mSiteListData.sites.length) {
			mSelectedSiteId = mSiteListData.sites[position].getIdSite();
		}
	}

	/**
	 * Returns the id of the selected site
	 *
	 * @return The id of the selected site
	 */
	public int getSelectedSiteId() {
		return mSelectedSiteId;
	}

	/**
	 * Set the selected site id
	 *
	 * @param pSiteId
	 *        The id of the site that is selected
	 */
	public void setSelectedSiteId(int pSiteId) {
		mSelectedSiteId = pSiteId;
	}

	/**
	 * Sets up the pager indicator
	 *
	 * @param pLayoutPagerIndicator
	 *        The layout where the pager indicator should be added
	 * @param pCurrentPageWidget
	 *        The index of the currently selected page
	 * @param pTotalWidgetPages
	 *        The amount of total pages
	 */
	private void createWidgetPagerIndicator(LinearLayout pLayoutPagerIndicator, int pCurrentPageWidget, int pTotalWidgetPages) {
		pLayoutPagerIndicator.removeAllViews();
		for (int i = 0; i < pTotalWidgetPages; i++) {
			ImageView tempView = new ImageView(mActivity);
			if (i == pCurrentPageWidget) {
				tempView.setImageDrawable(mDrawableIndicatorSelected);
			} else {
				tempView.setImageDrawable(mDrawableIndicatorUnselected);
			}
			pLayoutPagerIndicator.addView(tempView);
		}
	}

	/**
	 * Calls the attributes loader
	 */
	public void callAttributesLoader(int pSiteId) {
		Bundle params = new Bundle();
		params.putString(Constants.POST.URI, Constants.GET_SITE_ATTRIBUTES);
		params.putInt(Constants.POST.SITE_ID, pSiteId);
		params.putInt(Constants.POST.INSTANCE, 0);

		int loaderID = LoaderUtils.getUniqueLoaderId(LOADER_ID.SITE_ATTRIBUTES_SUMMARY, pSiteId);
		if (mActivity.getSupportLoaderManager().getLoader(loaderID) == null) {
			mActivity.getSupportLoaderManager().initLoader(loaderID, params, this);
		} else {
			mActivity.getSupportLoaderManager().restartLoader(loaderID, params, this);
		}
	}

	/**
	 * Parse the login response
	 *
	 * @param pRestResponse
	 *        The login response that we should parse
	 */
	private void parseLoginResponse(RestResponse pRestResponse) {
		UserResponse userResponse = JsonParserHelper.getInstance().parseJson(pRestResponse, UserResponse.class);
		if (userResponse != null) {
			switch (userResponse.status.code) {
				case RESPONSE_CODE.RESPONSE_OK:
					UserUtils.saveSessionID(mActivity, userResponse.data.user.sessionId);
					break;
			}
		}
	}

	/**
	 * Parse the attribute response
	 *
	 * @param pRestResponse
	 *        The attribute response that we should parse
	 */
	private void parseAttributesResponse(Site pSite, RestResponse pRestResponse) {
		AttributesResponse attributesResponse = JsonParserHelper.getInstance().parseJson(pRestResponse, AttributesResponse.class);
		if (attributesResponse != null) {
			switch (attributesResponse.status.code) {
				case RESPONSE_CODE.RESPONSE_OK:
					pSite.defineMicroWidgets(mActivity, attributesResponse.data);
					pSite.setAttributeDownloadStatus(DownloadStatus.DOWNLOAD_FINISHED);
					notifyDataSetChanged();
					break;
				case RESPONSE_CODE.RESPONSE_SESSION_ID:
					callLoginLoader();
					break;
				default:
					// If something went wrong just try to load them again
					pSite.setAttributeDownloadStatus(DownloadStatus.DOWNLOAD_NEEDED);
					break;
			}
		} else {
			// If something went wrong just try to load them again
			pSite.setAttributeDownloadStatus(DownloadStatus.DOWNLOAD_NEEDED);
		}
	}

	@Override
	public Loader<RestResponse> onCreateLoader(int loaderId, Bundle params) {
		WebserviceAsync loader = WebserviceAsync.newInstance(mActivity);
		loader.setParams(params);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<RestResponse> loader, RestResponse response) {
		int loaderType = LoaderUtils.getLoaderIdFromUniqueLoaderId(loader.getId());
		int siteId = (loader.getId() & 0x0FFFFFFF);

		switch (loaderType) {
			case LOADER_ID.LOGIN:
				parseLoginResponse(response);
				break;
			case LOADER_ID.SITE_ATTRIBUTES_SUMMARY:
				parseAttributesResponse(mSiteListData.getSiteById(siteId), response);
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<RestResponse> arg0) {
		// Do nothing
	}

	// =============================================================
	// Auto relogin after time out
	// =============================================================

	/**
	 * Call the loader to try login (get's called when the session expired on the server)
	 */
	private void callLoginLoader() {
		String username = UserUtils.getUsername(mActivity);
		String password = UserUtils.getPassword(mActivity);

		Bundle params = new Bundle();
		params.putString(Constants.POST.URI, Constants.WEBSERVICE_USER_LOGIN);
		params.putString(Constants.POST.EMAIL, username);
		params.putString(Constants.POST.PASSWORD, password);

		if (mActivity.getSupportLoaderManager().getLoader(LOADER_ID.LOGIN) == null) {
			mActivity.getSupportLoaderManager().initLoader(LOADER_ID.LOGIN, params, this);
		} else {
			mActivity.getSupportLoaderManager().restartLoader(LOADER_ID.LOGIN, params, this);
		}
	}

	/**
	 * Viewholder class that holds the site list item view
	 */
	private static class ViewHolder {
		TextView textViewSummaryHeader;
		View viewSiteData;
		TextView textViewSiteName;
		TextView textViewLastUpdate;
		ProgressBar progressBarLoading;
		ViewPager viewPagerWidgets;
		LinearLayout layoutPagerIndicator;

		public ViewHolder(View view) {
			progressBarLoading = (ProgressBar) view.findViewById(R.id.progressbar_summary_loading);
			textViewLastUpdate = (TextView) view.findViewById(R.id.textview_last_update);
			textViewSiteName = (TextView) view.findViewById(R.id.textview_site_name);
			textViewSummaryHeader = (TextView) view.findViewById(R.id.tv_summary_header);
			viewSiteData = view.findViewById(R.id.layout_site_data);
			viewPagerWidgets = (ViewPager) view.findViewById(R.id.viewpager_widgets);
			layoutPagerIndicator = (LinearLayout) view.findViewById(R.id.layout_widgets_pager_indicator);
		}
	}
}
