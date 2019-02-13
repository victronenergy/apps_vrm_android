package nl.victronenergy.fragments;

import nl.victronenergy.R;
import nl.victronenergy.activities.ActivityAbout;
import nl.victronenergy.activities.ActivityDetailWebsite;
import nl.victronenergy.activities.ActivityLogin;
import nl.victronenergy.activities.ActivityWebsite;
import nl.victronenergy.adapters.SiteSummaryAdapter;
import nl.victronenergy.models.SiteListData;
import nl.victronenergy.util.AnalyticsConstants;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.BUNDLE;
import nl.victronenergy.util.Constants.DEFAULT_VALUE;
import nl.victronenergy.util.UserUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Shows a list of sites for the logged in user
 *
 * @author M2Mobi
 */
public class FragmentSiteSummary extends Fragment implements OnItemClickListener, OnScrollListener {
	private static final String LOG_TAG = "FragmentSiteSummary";
	private OnSiteSelectedListener mOnSiteSelectedListener;

	private ListView mListViewSites;
	private SiteSummaryAdapter mSiteSummaryAdapter;
	private SiteListData mSiteData;

	private EditText mSearchActionBar;
	private MenuItem mSearchMenuItem;

	/**
	 * Interface used to tell the activity that a site has been selected in the sitelist
	 */
	public interface OnSiteSelectedListener {
		public void onSiteSelected(int position);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		View view = inflater.inflate(R.layout.fragment_site_summary, null);
		initView(view);

		if (savedInstanceState != null) {
			mSiteData = (SiteListData) savedInstanceState.getSerializable(BUNDLE.SITE_LIST_DATA);
			mSiteSummaryAdapter.setSites(mSiteData);

			// Restore selected site
			int siteId = savedInstanceState.getInt(BUNDLE.SELECTED_SITE_ID, DEFAULT_VALUE.SITE_INDEX);
			if (siteId != DEFAULT_VALUE.SITE_INDEX) {
				if (mSiteData != null) {
					setSelectedSitePosition(mSiteData.getSiteIndexForSiteId(siteId));
				}
			}
			setLoadingFinished(view);
		}
		return view;
	}

	/**
	 * Initialize the view
	 */
	private void initView(View view) {
		mSiteSummaryAdapter = new SiteSummaryAdapter(this);
		mListViewSites = (ListView) view.findViewById(R.id.list_sites);

		// TODO: Remove once we drop support for < API 11
		View viewFooter = LayoutInflater.from(getActivity()).inflate(R.layout.footer_site_summary, mListViewSites, false);
		mListViewSites.addFooterView(viewFooter, null, false);

		mListViewSites.setEmptyView(view.findViewById(R.id.layout_empty_view_site_summary));
		mListViewSites.setAdapter(mSiteSummaryAdapter);
		mListViewSites.setOnItemClickListener(this);
		mListViewSites.setOnScrollListener(this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mOnSiteSelectedListener = (OnSiteSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnSiteSelectedListener");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(BUNDLE.SITE_LIST_DATA, mSiteData);
		outState.putInt(BUNDLE.SELECTED_SITE_ID, mSiteSummaryAdapter.getSelectedSiteId());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		super.onCreateOptionsMenu(menu, menuInflater);
		menuInflater.inflate(R.menu.menu_sitesummary, menu);

		// Add the username to the logout button
		String userName = UserUtils.getUsername(getActivity());
		if (TextUtils.isEmpty(userName)) {
			menu.findItem(R.id.menu_button_logout).setTitle(getString(R.string.menu_logout_email, getString(R.string.demo)));
		} else {
			menu.findItem(R.id.menu_button_logout).setTitle(getString(R.string.menu_logout_email, userName));
		}

		// Add a expand/collapse listener to the search
		mSearchMenuItem = menu.findItem(R.id.button_search);
		MenuItemCompat.setOnActionExpandListener(mSearchMenuItem, new MenuItemCompat.OnActionExpandListener() {

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				// Return true to expand action view
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				if (mSearchActionBar != null && !mSearchActionBar.equals("")) {
					mSearchActionBar.setText("");
				}
				hideKeyboard();

				// Return true to collapse action view
				return true;
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long itemId) {
		if (mSearchActionBar != null && MenuItemCompat.isActionViewExpanded(mSearchMenuItem)) {
			MenuItemCompat.collapseActionView(mSearchMenuItem);
		}

		// Notify the activity that a site has been selected
		mOnSiteSelectedListener.onSiteSelected((int) itemId);
	}

	/**
	 * Hacky way to retrieve the OnClickListener of the viewpager in the site list
	 *
	 * @param pSiteId
	 *        The id of the site that has been clicked
	 */
	public void onWidgetOfSiteItemClicked(int pSiteId) {
		// Notify the activity that a site has been selected
		mOnSiteSelectedListener.onSiteSelected(mSiteData.getSiteIndexForSiteId(pSiteId));
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE:
				mSiteSummaryAdapter.setIsScrolling(false);
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				// No need to pause when scrolling
				break;
			case OnScrollListener.SCROLL_STATE_FLING:
				mSiteSummaryAdapter.setIsScrolling(true);
				break;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// Nothing to do
	}

	/**
	 * Checks which action bar button is pressed
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.button_search:
				performSearch(item);
				break;
			case R.id.menu_button_logout:
				logout();
				break;
			case R.id.menu_button_website:
				openVrmPortal();
				break;
			case R.id.menu_button_about:
				startActivity(new Intent(getActivity(), ActivityAbout.class));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Log the user out and return to the login screen
	 */
	private void logout() {
		UserUtils.deleteUserData(getActivity());

		// Go to login screen
		Intent loginIntent = new Intent(getActivity(), ActivityLogin.class);
		loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(loginIntent);
	}

	/**
	 * Open the VRM Portal website
	 */
	private void openVrmPortal() {
		Intent websiteIntent = new Intent(getActivity(), ActivityDetailWebsite.class);
		websiteIntent.putExtra(Constants.INTENT_SITE_URL, Constants.WEBAPP.BASE_URL);
		startActivity(websiteIntent);
	}

	/**
	 * Performs the search
	 *
	 * @param item
	 *        The menuitem to get the search from
	 */
	private void performSearch(MenuItem item) {
		if (item != null) {
			mSearchMenuItem = item;
			mSearchActionBar = (EditText) MenuItemCompat.getActionView(item).findViewById(R.id.et_search);
			mSearchActionBar.addTextChangedListener(searchTextWatcher);
			mSearchActionBar.requestFocus();

			mSiteSummaryAdapter.setSearchFilter(mSearchActionBar.getText().toString());

			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		}
	}

	/**
	 * Hides the keyboard<br/>
	 * The flag provided in hideSoftInputFromWindow() hides the keyboard, the flag constant (RESULT_UNCHANGED_SHOWN)
	 * provided does not seem to match what effect is has, therefore the 0 (zero); even though they do the same.
	 */
	private void hideKeyboard() {
		if (((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).isActive() && mSearchActionBar != null) {
			((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
					mSearchActionBar.getWindowToken(), 0);
		}
	}

	/**
	 * Watches the searchbox text changes
	 */
	private final TextWatcher searchTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
			// Do nothing
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// Do nothing
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			mSiteSummaryAdapter.setSearchFilter(String.valueOf(s));
		}
	};

	/**
	 * Set the site data
	 *
	 * @param siteData
	 *        The site data to set
	 */
	public void setSiteData(SiteListData siteData) {
		mSiteData = siteData;
		mSiteSummaryAdapter.setSites(siteData);
		setLoadingFinished(getView());
	}

	/**
	 * Sets the loading finished flag and shows the listview/hides the loading view
	 *
	 * @param view
	 *        The view needed to hide the data loading view
	 */
	private void setLoadingFinished(View view) {
		if (view == null) {
			return;
		}

		view.findViewById(R.id.data_loading).setVisibility(View.GONE);
		mListViewSites.setVisibility(View.VISIBLE);
	}

	/**
	 * Scrolls the list to the given position
	 *
	 * @param position
	 *        The position you want to show at the top of the list
	 */
	public void setSelectedSitePosition(int position) {
		mListViewSites.smoothScrollToPosition(position);
		mSiteSummaryAdapter.setSelectedSitePosition(position);
		mSiteSummaryAdapter.notifyDataSetChanged();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
}
