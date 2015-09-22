/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.fragments;

import java.util.Date;

import nl.victronenergy.BuildConfig;
import nl.victronenergy.R;
import nl.victronenergy.activities.ActivityGallery;
import nl.victronenergy.activities.ActivityWebsite;
import nl.victronenergy.activities.TakePictureActivity;
import nl.victronenergy.adapters.GalleryAdapter;
import nl.victronenergy.adapters.IoAdapter;
import nl.victronenergy.models.Attribute;
import nl.victronenergy.models.AttributeData;
import nl.victronenergy.models.AttributesResponse;
import nl.victronenergy.models.Site;
import nl.victronenergy.models.SiteListResponse;
import nl.victronenergy.util.AnalyticsConstants;
import nl.victronenergy.util.AttributeUtils;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.ATTRIBUTE;
import nl.victronenergy.util.Constants.BUNDLE;
import nl.victronenergy.util.Constants.DownloadStatus;
import nl.victronenergy.util.Constants.GENERATOR_STATE;
import nl.victronenergy.util.Constants.LOADER_ID;
import nl.victronenergy.util.Constants.Overview;
import nl.victronenergy.util.Constants.POST;
import nl.victronenergy.util.Constants.RESPONSE_CODE;
import nl.victronenergy.util.IoExtenderUtils;
import nl.victronenergy.util.LoaderUtils;
import nl.victronenergy.util.MyLog;
import nl.victronenergy.util.webservice.JsonParserHelper;
import nl.victronenergy.util.webservice.RestResponse;
import nl.victronenergy.util.webservice.WebserviceAsync;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

/**
 * Site summary can have 5 different scenarios according to documentation and each one show different data:
 * <ol>
 * <li>BMV (DC Current, DC Voltage, Consumed Energy, State of Charge, Time to go;.</li>
 * <li>Multi (AC In, AC Out, DC Current, DC Voltage, State of Charge);</li>
 * <li>BMV + Multi (AC In, AC Out, DC Current, DC Voltage, DC System, State of Charge, Time to go, Consumed Energy)</li>
 * <li>BMV + Multi + MPPT (AC In, AC Out, DC Current, DC Voltage, DC System, State of Charge, Time to go, Consumed
 * Energy, Solar Amps, Solar Watts)</li>
 * <li>No Data.</li>
 * </ol>
 *
 * @author Victron Energy
 */
public class FragmentSiteDetail extends VictronVRMFragment implements OnClickListener, LoaderCallbacks<RestResponse>, OnItemClickListener {

	/** String tag used for log messages */
	private static final String LOG_TAG = "FragmentSiteDetail";

	private SiteDetailCallBacks mSiteDetailCallBacks;

	private IoAdapter mIoAdapter;
	private View mViewFooterGenerator;
	private Button mButtonGenerator;
	private Button mButtonHistoricData;
	private TextView mTextViewSiteStatus;
	private ListView mListSummary;
	private Site mSite;
	private int mSiteIndex;

	/** Download status of the webservice calls */
	private DownloadStatus mSiteDownloadStatus;
	private DownloadStatus mAttributesDownloadStatus;
	private boolean mDownloadComplete = true;

	/** Grid view of the gallery */
	private GridView mGridViewGallery;

	/**
	 * Interface used to notify the activity of certain events on the site detail fragment
	 */
	public interface SiteDetailCallBacks {
		/**
		 * Notifies the activity that the historic data button has been clicked
		 *
		 * @param pSite
		 *        The site on which the button was clicked
		 */
		public void onHistoricDataClicked(Site pSite);

		/**
		 * Notifies the activity that the IO Settings button has been clicked
		 *
		 * @param pSite
		 *        The site on which the button was clicked
		 */
		public void onIOSettingsClicked(Site pSite);

		/**
		 * Notifies the activity that all data for the site details has finished loading
		 */
		public void onSiteDetailLoadingFinished();

		/**
		 * Notifies the activity that onDestroyOptionsMenu has been called
		 */
		public void onSiteDetailDestroyOptionsMenu();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().registerReceiver(toggleBroadcastReceiver, new IntentFilter(Constants.BROADCAST_TOGGLE_BUTTON));

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mSite = (Site) savedInstanceState.getSerializable(BUNDLE.SITE_OBJECT);
			mSiteIndex = savedInstanceState.getInt(BUNDLE.SITE_ARRAY_INDEX);
		} else {
			if (getArguments() != null) {
				mSite = (Site) getArguments().getSerializable(BUNDLE.SITE_OBJECT);
				mSiteIndex = getArguments().getInt(BUNDLE.SITE_ARRAY_INDEX);
			}
		}

		View view = inflater.inflate(R.layout.fragment_site_detail, container, false);
		initializeView(view);

		mIoAdapter = new IoAdapter(getActivity(), mSite, true);
		mListSummary.setAdapter(mIoAdapter);

		parseSiteData();

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mSiteDetailCallBacks = (SiteDetailCallBacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement SiteDetailCallBacks");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(BUNDLE.SITE_OBJECT, mSite);
		outState.putInt(BUNDLE.SITE_ARRAY_INDEX, mSiteIndex);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getActivity() != null) {
			EasyTracker.getInstance().setContext(getActivity());
			EasyTracker.getTracker().sendView(getString(R.string.ga_activity_site_detail));
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		loadData();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(toggleBroadcastReceiver);
	}

	/**
	 * Initialize this view
	 */
	private void initializeView(View view) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());

		// Setup the listview
		mListSummary = (ListView) view.findViewById(R.id.list_site_detail);

		// Add the header view, this view contains the actual site detail (site status and overview)
		ViewGroup header = (ViewGroup) inflater.inflate(R.layout.layout_site_detail, mListSummary, false);
		mListSummary.addHeaderView(header, null, false);

		View viewFooter = inflater.inflate(R.layout.layout_site_detail_footer, mListSummary, false);
		mListSummary.addFooterView(viewFooter, null, false);
		mViewFooterGenerator = viewFooter.findViewById(R.id.view_generator_button);
		mButtonGenerator = (Button) viewFooter.findViewById(R.id.button_generator);
		mButtonGenerator.setOnClickListener(this);

		mButtonHistoricData = (Button) view.findViewById(R.id.button_historical_data);
		mButtonHistoricData.setOnClickListener(this);
		view.findViewById(R.id.button_io_settings).setOnClickListener(this);

		mTextViewSiteStatus = (TextView) view.findViewById(R.id.textview_site_status);

		// Setup gallery
		mGridViewGallery = (GridView) viewFooter.findViewById(R.id.gridview_gallery);
		mGridViewGallery.setEmptyView(viewFooter.findViewById(R.id.gallery_textview_empty));
		mGridViewGallery.setAdapter(new GalleryAdapter(mSite));
		mGridViewGallery.setOnItemClickListener(this);
	}

	/**
	 * Returns the overview that's configured for this site
	 *
	 * @return The overview that is configured on the site
	 */
	private Overview getSiteOverview() {
		// If the attribute data is not set show a no connection screen
		if (mSite == null || !mSite.areAttributesLoaded() || mSite.getAttributeData() == null) {
			return Overview.NO_CONNECTION;
		}

		AttributeData attributeData = mSite.getAttributeData();
		boolean hasMulti = attributeData.isAttributeSet(ATTRIBUTE.AC_CONSUMPTION_L1);
		boolean hasSolarCharger = attributeData.isAttributeSet(ATTRIBUTE.PV_DC_COUPLED);
		boolean hasPvInverterOnOut = attributeData.isAttributeSet(ATTRIBUTE.PV_AC_COUPLED_OUTPUT_L1);

		// Checks which overview is the active overview for this site
		if (hasMulti && !hasSolarCharger && hasPvInverterOnOut) {
			return Overview.BMV_MULTI_PVINVERTER;
		} else if (hasMulti && hasSolarCharger) {
			return Overview.BMV_MULTI_MPPT;
		} else if (!hasMulti && hasSolarCharger) {
			return Overview.BMV_MPPT;
		} else if (hasMulti && !hasSolarCharger && !hasPvInverterOnOut) {
			return Overview.BMV_MULTI;
		} else {
			return Overview.BMV;
		}
	}

	/**
	 * Puts the proper overviewfragment in the overview frame depending on what kind of overview is configured on the
	 * site and passes the data to the fragment
	 */
	private void setOverviewFragment(Overview overview) {
		String title = overview.getTitle();
		Fragment fragmentOverview = Fragment.instantiate(getActivity(), overview.getClassOverviewFragment().getName(), null);

		// Show some debug data if this is a debug build
		if (BuildConfig.BUILD_TYPE.equals("debug")) {
			TextView tvOverviewType = (TextView) getView().findViewById(R.id.debug_info);
			tvOverviewType.setVisibility(View.VISIBLE);
			tvOverviewType.setText("[" + mSite.getIdSite() + "] " + title + "\nPUBNUB: " + mSite.hasPubnub());
		}

		Bundle bundle = new Bundle();
		bundle.putSerializable(BUNDLE.SITE_OBJECT, mSite);
		fragmentOverview.setArguments(bundle);
		// Only make the transaction when this fragment is visible
		if (isVisible()) {
			getChildFragmentManager().beginTransaction().replace(R.id.frame_overview, fragmentOverview).commit();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_site_detail, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.button_web:
				EasyTracker.getTracker().sendEvent(AnalyticsConstants.CAT_UI_ACTION, AnalyticsConstants.BUTTON_PRESS,
						AnalyticsConstants.SETTINGS_WEBSITE, null);
				Intent settingsIntent = new Intent(getActivity(), ActivityWebsite.class);
				settingsIntent.putExtra(Constants.INTENT_SITE_URL, Constants.WEBAPP.OPEN_SITE_URL + mSite.getIdSite());
				startActivity(settingsIntent);
				break;
			case R.id.button_refresh:
				EasyTracker.getTracker().sendEvent(AnalyticsConstants.CAT_UI_ACTION, AnalyticsConstants.BUTTON_PRESS,
						AnalyticsConstants.REFRESH_SITESUM, null);
				loadData();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroyOptionsMenu() {
		super.onDestroyOptionsMenu();
		mSiteDetailCallBacks.onSiteDetailDestroyOptionsMenu();
	}

	/**
	 * Method that makes the webservice calls to refresh data. But only if those calls aren't already in progress.
	 */
	private void loadData() {
		if (!mDownloadComplete) {
			MyLog.d(LOG_TAG, "[" + mSite.getIdSite() + "] Download already in progress");
			return;
		}

		MyLog.d(LOG_TAG, "[" + mSite.getIdSite() + "] Starting download");
		mDownloadComplete = false;

		// Call the site loader and attributes loader
		callSiteDataLoader();
		callAttributesLoader();
	}

	/**
	 * Call the loader to get the site data
	 */
	public void callSiteDataLoader() {
		MyLog.d(LOG_TAG, "[" + mSite.getIdSite() + "] Site data loader called");
		mSiteDownloadStatus = DownloadStatus.DOWNLOAD_IN_PROGRESS;

		Bundle params = new Bundle();
		params.putString(POST.URI, Constants.GET_SITE);
		params.putInt(POST.SITE_ID, mSite.getIdSite());

		int loaderID = LoaderUtils.getUniqueLoaderId(LOADER_ID.SITEDATA, mSite.getIdSite());
		if (getActivity().getSupportLoaderManager().getLoader(loaderID) == null) {
			getActivity().getSupportLoaderManager().initLoader(loaderID, params, this);
		} else {
			getActivity().getSupportLoaderManager().restartLoader(loaderID, params, this);
		}
	}

	/**
	 * Calls the attributes loader
	 */
	public void callAttributesLoader() {
		MyLog.d(LOG_TAG, "[" + mSite.getIdSite() + "] Attributes loader called");
		mAttributesDownloadStatus = DownloadStatus.DOWNLOAD_IN_PROGRESS;

		Bundle params = new Bundle();
		params.putString(POST.URI, Constants.GET_SITE_ATTRIBUTES);
		params.putInt(POST.SITE_ID, mSite.getIdSite());
		// Instance is always 0 for now
		params.putInt(POST.INSTANCE, 0);

		int loaderID = LoaderUtils.getUniqueLoaderId(LOADER_ID.SITE_ATTRIBUTES_DETAIL, mSite.getIdSite());
		if (getActivity().getSupportLoaderManager().getLoader(loaderID) == null) {
			getActivity().getSupportLoaderManager().initLoader(loaderID, params, this);
		} else {
			getActivity().getSupportLoaderManager().restartLoader(loaderID, params, this);
		}
	}

	/**
	 * Parse the site data
	 */
	public void parseSiteData() {
		View view = getView();
		if (view == null) {
			return;
		}

		((TextView) view.findViewById(R.id.textview_site_name)).setText(mSite.getName());

		// Display the generator button
		if (mSite.hasIOExtender()) {
			mViewFooterGenerator.setVisibility(View.VISIBLE);
			prepareGeneratorButton();
		} else {
			mViewFooterGenerator.setVisibility(View.GONE);
		}

		AttributeUtils.populateStatusTextView(getActivity(), mTextViewSiteStatus, mSite);

		mTextViewSiteStatus.setVisibility(View.VISIBLE);

		mGridViewGallery.setAdapter(new GalleryAdapter(mSite));
	}

	/**
	 * Set status of IO object (example: OUT2)
	 *
	 * @param ioCode
	 * @param command
	 */
	private void setSwitch(String ioCode, boolean command) {
		for (int i = 0; i < mIoAdapter.getCount(); i++) {
			if (mIoAdapter.getItem(i).attributeCode.equals(ioCode)) {
				mIoAdapter.getItem(i).setStatus(command);
				mIoAdapter.notifyDataSetChanged();
				return;
			}
		}
	}

	/**
	 * Prepare generator button When there is an IO Extender and the site has a generator show the generator start/stop
	 * button <br/>
	 * <br/>
	 * If there is a generator, check the output the generator is connected to and the setting that tells when the
	 * generator is stopped<br/>
	 * - Setting state stopped AND selected output closed = Start generator<br/>
	 * - Setting state stopped AND selected output open = Stop generator<br/>
	 * - Setting state running AND selected output closed = Stop generator<br/>
	 * - Setting state running AND selected output open = Start generator<br/>
	 */
	private void prepareGeneratorButton() {
		if (mSite.hasGenerator() && mSite.hasIOExtender() && mSite.getIoExtenderData() != null && mSite.getIoExtenderData().getIoExtenderCount() > 0) {
			mButtonGenerator.setVisibility(View.VISIBLE);

			int generatorStateClosed = IoExtenderUtils.getGeneratorStateClosed(getActivity(), mSite.getIdSite());
			Attribute generatorOutput = mSite.getIoExtenderData().getGeneratorOutputObject(getActivity(), mSite.getIdSite());
			if (generatorOutput == null) {
				mButtonGenerator.setText(getString(R.string.site_detail_generator_settings_not_set));
			} else {
				if (generatorOutput.isOpen()) {
					if (generatorStateClosed == GENERATOR_STATE.STOPPED) {
						mButtonGenerator.setText(getString(R.string.site_detail_button_start_generator));
					} else {
						mButtonGenerator.setText(getString(R.string.site_detail_button_stop_generator));
					}
				} else {
					if (generatorStateClosed == GENERATOR_STATE.STOPPED) {
						mButtonGenerator.setText(getString(R.string.site_detail_button_stop_generator));
					} else if (generatorStateClosed == GENERATOR_STATE.RUNNING) {
						mButtonGenerator.setText(getString(R.string.site_detail_button_start_generator));
					}
				}
			}

			// Enable or disable the button according to the rights of this user
			if (mSite.canEdit()) {
				mButtonGenerator.setEnabled(true);
			} else {
				mButtonGenerator.setEnabled(false);
			}
		} else {
			mButtonGenerator.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.button_generator:
				EasyTracker.getTracker().sendEvent(AnalyticsConstants.CAT_UI_ACTION, AnalyticsConstants.BUTTON_PRESS,
						AnalyticsConstants.GENERATOR_BUTTON, null);
				Attribute generatorOuput = mSite.getIoExtenderData().getGeneratorOutputObject(getActivity(), mSite.getIdSite());
				if (generatorOuput == null) {
					Toast.makeText(getActivity(), getString(R.string.site_detail_info_generator_set_settings), Toast.LENGTH_SHORT).show();
				} else {
					startSmsDialog(!generatorOuput.isOpen());
				}

				break;
			case R.id.button_io_settings:
				mSiteDetailCallBacks.onIOSettingsClicked(mSite);
				break;
			case R.id.button_historical_data:
				mSiteDetailCallBacks.onHistoricDataClicked(mSite);
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// If this is the last item, we clicked on the "add image" button else show the gallery
		if (position == mSite.getImages().size()) {
			Intent takePictureIntent = new Intent(getActivity(), TakePictureActivity.class);
			takePictureIntent.putExtra(Constants.INTENT_SITE_ID, mSite.getIdSite());
			startActivity(takePictureIntent);
		} else {
			Intent galleryIntent = new Intent(getActivity(), ActivityGallery.class);
			galleryIntent.putExtra(ActivityGallery.KEY_POSITION, position);
			galleryIntent.putExtra(BUNDLE.SITE_OBJECT, mSite);
			startActivity(galleryIntent);
		}
	}

	/**
	 * Set generator button state
	 *
	 * @param command
	 */
	public void setGeneratorButtonState(boolean command) {
		if (command) {
			mButtonGenerator.setText(getString(R.string.site_detail_button_start_generator));
		} else {
			mButtonGenerator.setText(getString(R.string.site_detail_button_stop_generator));
		}
	}

	/**
	 * Toggle generator button state
	 *
	 * @return
	 */
	public boolean toggleGeneratorButtonState() {
		if (mButtonGenerator == null) {
			return false;
		}

		if (mButtonGenerator.getText().equals(getActivity().getString(R.string.site_detail_button_start_generator))) {
			mButtonGenerator.setText(getActivity().getString(R.string.site_detail_button_stop_generator));
			return true;
		} else {
			mButtonGenerator.setText(getActivity().getString(R.string.site_detail_button_start_generator));
			return false;
		}
	}

	/**
	 * Setup and start a dialog showing the sms command ready to send
	 *
	 * @param state
	 */
	private void startSmsDialog(boolean state) {
		if (TextUtils.isEmpty(mSite.getPhonenumber())) {
			Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.error_no_phonenumber), Toast.LENGTH_SHORT).show();
			return;
		} else if (!mSite.canEdit()) {
			Toast.makeText(getActivity(), getActivity().getString(R.string.error_demo_user_message), Toast.LENGTH_SHORT).show();
			return;
		} else if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
			Toast.makeText(getActivity(), getActivity().getString(R.string.error_unable_to_send_text), Toast.LENGTH_SHORT).show();
			return;
		}

		final String smsPhoneNumber;
		if (BuildConfig.BUILD_TYPE.equals("debug")) {
			smsPhoneNumber = BuildConfig.DEBUG_PHONE_NUMER;
		} else {
			smsPhoneNumber = mSite.getPhonenumber();
		}
		final String smsCommand = IoExtenderUtils.prepareSmsCommand(mSite, IoExtenderUtils.getGeneratorIoCode(getActivity(), mSite.getIdSite()),
				state);
		String msgCommand = String.format(getString(R.string.sms_dialog_message), smsCommand, smsPhoneNumber);

		// Show sms dialog
		new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom)).setTitle(getString(R.string.sms_dialog_title))
				.setMessage(Html.fromHtml(msgCommand)).setCancelable(true).setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

						boolean originalState = toggleGeneratorButtonState();

						IoExtenderUtils.sendSMS(getActivity(), smsPhoneNumber, smsCommand, mSite.getIdSite(), mSiteIndex, null, true, originalState);

						// notify linked switch button (on/off)
						notifySwitchChanged(getActivity(), originalState);
					}
				}).setNegativeButton(R.string.cancel_dialog, null).show();
	}

	/**
	 * The generator button it's attached to one of the outputs (default: OUT1), so when the state changes in one of the
	 * sides the other gets affected too
	 *
	 * @param context
	 *        The context
	 * @param originalState
	 *        The original state of the switch, used to restore the original state in case anything goes wrong
	 */
	private void notifySwitchChanged(Context context, boolean originalState) {
		Intent i = new Intent(Constants.BROADCAST_TOGGLE_BUTTON);
		i.putExtra(Constants.INTENT_SITE_ID, mSite.getIdSite());
		i.putExtra(Constants.INTENT_IO_CODE, IoExtenderUtils.getGeneratorIoCode(context, mSite.getIdSite()));
		i.putExtra(Constants.INTENT_IS_GENERATOR_BTN, false);
		i.putExtra(Constants.INTENT_ORIGINAL_STATE, originalState);
		i.putExtra(Constants.INTENT_FIRED_BY_SMS_BROADCAST, false);
		context.sendBroadcast(i);
	}

	/**
	 * Check if all loaders succesfully finished, if the session expired make sure we relogin and recall the needed
	 * webservices
	 */
	private void checkLoadingFinished() {
		boolean areAllLoadersDone = true;
		boolean isReloginNeeded = false;
		boolean didAnyErrorsOccur = false;

		switch (mSiteDownloadStatus) {
			case DOWNLOAD_SESSION_EXPIRED:
				isReloginNeeded = true;
				break;
			case DOWNLOAD_IN_PROGRESS:
				MyLog.i(LOG_TAG, "[" + mSite.getIdSite() + "] Site is still downloading");
				areAllLoadersDone = false;
				break;
			case DOWNLOAD_ERROR:
				didAnyErrorsOccur = true;
				break;
			default:
				// We don't have to do anything when it's any other status
				break;
		}

		switch (mAttributesDownloadStatus) {
			case DOWNLOAD_SESSION_EXPIRED:
				isReloginNeeded = true;
				break;
			case DOWNLOAD_IN_PROGRESS:
				MyLog.i(LOG_TAG, "[" + mSite.getIdSite() + "] Attribute is still downloading");
				areAllLoadersDone = false;
				break;
			case DOWNLOAD_ERROR:
				didAnyErrorsOccur = true;
				break;
			default:
				// We don't have to do anything when it's any other status
				break;
		}

		// If all loaders are done we can check if we are done with loading
		if (areAllLoadersDone) {
			mDownloadComplete = true;
			MyLog.d(LOG_TAG, "[" + mSite.getIdSite() + "] All loaders are done");

			if (isReloginNeeded) {
				MyLog.d(LOG_TAG, "[" + mSite.getIdSite() + "] We need to relogin");
				callLoginLoader();
			} else {
				MyLog.d(LOG_TAG, "[" + mSite.getIdSite() + "] Download is complete");
				mSiteDetailCallBacks.onSiteDetailLoadingFinished();
				if (didAnyErrorsOccur) {
					MyLog.d(LOG_TAG, "[" + mSite.getIdSite() + "] Error occured in atleast one loader");
				}
				setViewsVisibility();
			}
		} else {
			MyLog.d(LOG_TAG, "[" + mSite.getIdSite() + "] Something is still downloading");
		}
	}

	/**
	 * Hides and unhides the views according to the result of the webservice calls
	 */
	private void setViewsVisibility() {
		View view = getView();
		if (view == null) {
			return;
		}

		Overview currentScenario = getSiteOverview();
		setOverviewFragment(currentScenario);
		view.findViewById(R.id.data_loading).setVisibility(View.GONE);
		view.findViewById(R.id.frame_overview).setVisibility(View.VISIBLE);

		// Only show the historic data if there is a valid overview
		if (currentScenario == Overview.NO_CONNECTION) {
			mButtonHistoricData.setVisibility(View.GONE);
		} else {
			mButtonHistoricData.setVisibility(View.VISIBLE);
		}

		// Set the IO Extender data
		if (mSite.getIoExtenderData() != null && mSite.getIoExtenderData().getIoExtenderCount() > 0) {
			mIoAdapter.setSiteObject(mSite);
			prepareGeneratorButton();
			view.findViewById(R.id.layout_io_header).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.layout_io_header).setVisibility(View.GONE);
		}
	}

	/**
	 * Parse the json that contains the list of sites
	 *
	 * @param pRestResponse
	 *        The response from the site webservice
	 */
	private void parseSiteResponse(RestResponse pRestResponse) {
		SiteListResponse siteResponse = JsonParserHelper.getInstance().parseJsonAndShowError(getActivity(), pRestResponse, SiteListResponse.class);
		if (siteResponse != null) {
			switch (siteResponse.status.code) {
				case RESPONSE_CODE.RESPONSE_OK:
					if (siteResponse.data.sites != null && siteResponse.data.sites.length > 0) {
						// Webservice always returns 1 site (the one we requested) so we always want the site at 0 index
						Site site = siteResponse.data.sites[0];
						// Copy old attribute data to new site object
						if (mSite.areAttributesLoaded() && mSite.getAttributeData() != null) {
							site.setAttributeData(getActivity(), mSite.getAttributeData());
						}
						mSite = site;
						parseSiteData();
					}
					mSiteDownloadStatus = DownloadStatus.DOWNLOAD_FINISHED;
					break;
				case RESPONSE_CODE.RESPONSE_SESSION_ID:
					mSiteDownloadStatus = DownloadStatus.DOWNLOAD_SESSION_EXPIRED;
					break;
				default:
					mSiteDownloadStatus = DownloadStatus.DOWNLOAD_ERROR;
					break;
			}
		} else {
			mSiteDownloadStatus = DownloadStatus.DOWNLOAD_ERROR;
		}
	}

	/**
	 * Parse the attribute response
	 *
	 * @param pRestResponse
	 *        The attribute response that we should parse
	 */
	private void parseAttributesResponse(RestResponse pRestResponse) {
		AttributesResponse attributesResponse = JsonParserHelper.getInstance().parseJsonAndShowError(getActivity(), pRestResponse,
				AttributesResponse.class);
		if (attributesResponse != null) {
			switch (attributesResponse.status.code) {
				case RESPONSE_CODE.RESPONSE_OK:
					if (attributesResponse.data != null) {
						mSite.setAttributeData(getActivity(), attributesResponse.data);
					}

					mAttributesDownloadStatus = DownloadStatus.DOWNLOAD_FINISHED;
					break;
				case RESPONSE_CODE.RESPONSE_SESSION_ID:
					mAttributesDownloadStatus = DownloadStatus.DOWNLOAD_SESSION_EXPIRED;
					break;
				default:
					mAttributesDownloadStatus = DownloadStatus.DOWNLOAD_ERROR;
					break;
			}
		} else {
			mAttributesDownloadStatus = DownloadStatus.DOWNLOAD_ERROR;
		}
	}

	@Override
	public Loader<RestResponse> onCreateLoader(int arg0, Bundle params) {
		WebserviceAsync loader = WebserviceAsync.newInstance(getActivity());
		loader.setParams(params);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<RestResponse> loader, RestResponse response) {
		MyLog.i(LOG_TAG, "[" + mSite.getIdSite() + "] Loader finished " + loader.getId());
		int loaderType = LoaderUtils.getLoaderIdFromUniqueLoaderId(loader.getId());
		switch (loaderType) {
			case LOADER_ID.SITE_ATTRIBUTES_DETAIL:
				MyLog.i(LOG_TAG, "[" + mSite.getIdSite() + "] Attributes finished");
				parseAttributesResponse(response);
				break;
			case LOADER_ID.SITEDATA:
				MyLog.i(LOG_TAG, "[" + mSite.getIdSite() + "] Site finished");
				parseSiteResponse(response);
				break;
			default:
				MyLog.e(LOG_TAG, "Unknown loader returned. LoaderID: " + loaderType);
				break;
		}
		checkLoadingFinished();
	}

	@Override
	public void onLoaderReset(Loader<RestResponse> pLoader) {
		// Do nothing
	}

	@Override
	public void onReloginSuccessful() {
		loadData();
	}

	@Override
	public void onReloginFailed() {
		// Unable to reload data
		// TODO: Do we want to inform the user about this?
	}

	/**
	 * Handle actions received through this 'onReceive' of the SMS broadcast receiver
	 */
	private BroadcastReceiver toggleBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Constants.BROADCAST_TOGGLE_BUTTON)) {

				// Generator Button
				int siteID = intent.getIntExtra(Constants.INTENT_SITE_ID, -1);
				if (intent.getBooleanExtra(Constants.INTENT_IS_GENERATOR_BTN, true)) {
					if (siteID == mSite.getIdSite()) {
						setGeneratorButtonState(intent.getBooleanExtra(Constants.INTENT_ORIGINAL_STATE, true));
						MyLog.i(LOG_TAG, "[Generator Toggled] SiteID: " + siteID);

						// notify linked switch button (on/off)
						if (intent.getBooleanExtra(Constants.INTENT_FIRED_BY_SMS_BROADCAST, false)) {
							// just to make sure!
							mIoAdapter.notifyDataSetChanged();
							setSwitch(IoExtenderUtils.getGeneratorIoCode(context, siteID),
									!intent.getBooleanExtra(Constants.INTENT_ORIGINAL_STATE, true));
							// just to make sure!
							mIoAdapter.notifyDataSetChanged();
						}
					}
					return;
				}

				// Toggle Button inside IoAdapter
				String ioCode = intent.getStringExtra(Constants.INTENT_IO_CODE);
				if (ioCode == null) {
					return;
				}
				if (siteID == mSite.getIdSite()) {
					if (ioCode.startsWith(Constants.OUTPUT_CODE_PREFIX)) {

						// just to make sure!
						mIoAdapter.notifyDataSetChanged();

						setSwitch(ioCode, intent.getBooleanExtra(Constants.INTENT_ORIGINAL_STATE, true));

						// just to make sure!
						mIoAdapter.notifyDataSetChanged();

						MyLog.i(LOG_TAG, "[IO Switch Toggled] IO Code: " + ioCode + "  (SiteID: " + siteID + ")");

						// notify linked generator button
						if (intent.getBooleanExtra(Constants.INTENT_FIRED_BY_SMS_BROADCAST, false)) {
							setGeneratorButtonState(!intent.getBooleanExtra(Constants.INTENT_ORIGINAL_STATE, true));
						}
					}
				}
			}
		}
	};
}
