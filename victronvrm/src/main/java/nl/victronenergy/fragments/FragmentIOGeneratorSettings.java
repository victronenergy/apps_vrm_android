/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.fragments;

import nl.victronenergy.R;
import nl.victronenergy.activities.ActivityIOSettings;
import nl.victronenergy.adapters.IoAdapter;
import nl.victronenergy.adapters.OutputAdapter;
import nl.victronenergy.models.AttributesResponse;
import nl.victronenergy.models.BaseResponse;
import nl.victronenergy.models.Site;
import nl.victronenergy.util.AnalyticsConstants;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.BUNDLE;
import nl.victronenergy.util.Constants.LOADER_ID;
import nl.victronenergy.util.Constants.POST;
import nl.victronenergy.util.Constants.RESPONSE_CODE;
import nl.victronenergy.util.IoExtenderUtils;
import nl.victronenergy.util.MyLog;
import nl.victronenergy.util.webservice.JsonParserHelper;
import nl.victronenergy.util.webservice.RestResponse;
import nl.victronenergy.util.webservice.WebserviceAsync;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

/**
 * Activity that shows an overview of settings for a site Contains:
 * <p>
 * - Option to open settings in the webapp<br>
 * - Option to manage alarms in the webapp<br>
 * - Generator settings<br>
 * - IO Extender settings<br>
 *
 * @author Victron Energy
 */
public class FragmentIOGeneratorSettings extends VictronVRMFragment implements OnClickListener, LoaderCallbacks<RestResponse>, OnItemClickListener {
	private static final String LOG_TAG = "FragmentIOGeneratorSettings";

	private Site mSite;
	private IoAdapter mIoAdapter;
	private OutputAdapter mOutputAdapter;
	private Button mButtonSelectOutput;
	private Button mButtonStateStopped;
	private Button mButtonStateRunning;
	private int mReloginLoaderType;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_site_settings, container, false);

		if (savedInstanceState != null) {
			mSite = (Site) savedInstanceState.get(BUNDLE.SITE_OBJECT);
		} else {
			Bundle arguments = getArguments();
			if (arguments != null && arguments.containsKey(BUNDLE.SITE_OBJECT)) {
				mSite = (Site) arguments.get(BUNDLE.SITE_OBJECT);
			}
		}

		initView(view);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		callAttributeDataLoader();

		// Make sure the IO shows the new images
		if (mIoAdapter != null) {
			mIoAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(BUNDLE.SITE_OBJECT, mSite);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getActivity() != null) {
			EasyTracker.getInstance().setContext(getActivity());
			EasyTracker.getTracker().sendView(getString(R.string.ga_activity_io_generator_settings));
		}
	}

	/**
	 * Initialize the view
	 */
	private void initView(View view) {
		// Setup IO List
		ListView listView = (ListView) view.findViewById(R.id.list_site_settings);
		mIoAdapter = new IoAdapter(getActivity(), mSite, false);
		View viewGeneratorSettings = LayoutInflater.from(getActivity()).inflate(R.layout.layout_settings, listView, false);

		listView.addHeaderView(viewGeneratorSettings, null, false);
		listView.setAdapter(mIoAdapter);
		listView.setOnItemClickListener(this);

		// Initialize Generator views
		((TextView) view.findViewById(R.id.textview_site_name)).setText(mSite.getName());

		mButtonSelectOutput = (Button) view.findViewById(R.id.button_settings_select_output);
		mButtonSelectOutput.setOnClickListener(this);

		mButtonStateStopped = (Button) view.findViewById(R.id.button_settings_state_stopped);
		mButtonStateStopped.setOnClickListener(this);

		mButtonStateRunning = (Button) view.findViewById(R.id.button_settings_state_running);
		mButtonStateRunning.setOnClickListener(this);

		resetGeneratorSwitchToSiteStatus(view);
		view.findViewById(R.id.ib_settings_has_generator).setOnClickListener(this);

		updateSelectIOButton();
		updateGeneratorStateButtons();
	}

	/**
	 * Call the loader to get the IO data
	 */
	private void callAttributeDataLoader() {
		Bundle params = new Bundle();
		params.putString(POST.URI, Constants.GET_IO_DATA);
		params.putString(POST.SITE_ID, String.valueOf(mSite.getIdSite()));

		if (getActivity().getSupportLoaderManager().getLoader(LOADER_ID.ATTRIBUTES) == null) {
			getActivity().getSupportLoaderManager().initLoader(LOADER_ID.ATTRIBUTES, params, this);
		} else {
			getActivity().getSupportLoaderManager().restartLoader(LOADER_ID.ATTRIBUTES, params, this);
		}
	}

	/**
	 * Display to which output the generator is currently connected
	 */
	private void updateSelectIOButton() {
		mOutputAdapter = new OutputAdapter(getActivity(), mSite.getIoExtenderData().getOutputs());

		String selectedOutputCode = IoExtenderUtils.getGeneratorIoCode(getActivity(), mSite.getIdSite());
		if (TextUtils.isEmpty(selectedOutputCode)) {
			mButtonSelectOutput.setText(R.string.settings_select_output);
		} else {
			String ioLabel = mSite.getIoExtenderData().getIOLabelForCode(selectedOutputCode);
			if (!TextUtils.isEmpty(ioLabel)) {
				mButtonSelectOutput.setText(ioLabel);
			} else {
				mButtonSelectOutput.setText(R.string.settings_select_output);
			}
		}
	}

	/**
	 * Add the stopped/running state to the spinner
	 */
	private void updateGeneratorStateButtons() {
		int generatorState = IoExtenderUtils.getGeneratorStateClosed(getActivity(), mSite.getIdSite());
		if (generatorState == 0) {
			mButtonStateStopped.setEnabled(false);
			mButtonStateRunning.setEnabled(true);
		} else {
			mButtonStateStopped.setEnabled(true);
			mButtonStateRunning.setEnabled(false);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long itemID) {
		EasyTracker.getTracker()
				.sendEvent(AnalyticsConstants.CAT_UI_ACTION, AnalyticsConstants.LIST_PRESS, AnalyticsConstants.IO_EXTENDER_LIST, null);

		Intent ioIntent = new Intent(getActivity(), ActivityIOSettings.class);
		ioIntent.putExtra(BUNDLE.SITE_OBJECT, mSite);
		// Top settings (Generator/WebApp) is included as a header
		int headerCount = ((ListView) parent).getHeaderViewsCount();
		ioIntent.putExtra(Constants.INTENT_OBJECT_IO, mIoAdapter.getItem(position - headerCount));
		startActivity(ioIntent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ib_settings_has_generator:
				ImageButton generatorButton = (ImageButton) v;
				callSetHasGeneratorWebservice(!mSite.hasGenerator());
				if (!mSite.hasGenerator()) {
					generatorButton.setImageDrawable(getResources().getDrawable(R.drawable.switch_yes));
				} else {
					generatorButton.setImageDrawable(getResources().getDrawable(R.drawable.switch_no));
				}
				break;
			case R.id.button_settings_select_output:
				new AlertDialog.Builder(getActivity()).setTitle(R.string.settings_select_output)
						.setAdapter(mOutputAdapter, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int position) {
								IoExtenderUtils.saveGeneratorIoCode(getActivity(), mSite.getIdSite(), mOutputAdapter.getItem(position).attributeCode);
								updateSelectIOButton();
								dialog.dismiss();
							}
						}).create().show();
				break;
			case R.id.button_settings_state_running:
				IoExtenderUtils.saveGeneratorStateClosed(getActivity(), mSite.getIdSite(), Constants.GENERATOR_STATE.RUNNING);
				updateGeneratorStateButtons();
				break;
			case R.id.button_settings_state_stopped:
				IoExtenderUtils.saveGeneratorStateClosed(getActivity(), mSite.getIdSite(), Constants.GENERATOR_STATE.STOPPED);
				updateGeneratorStateButtons();
				break;
		}
	}

	/**
	 * Set this site to have a generator or not
	 *
	 * @param hasGenerator
	 */
	private void callSetHasGeneratorWebservice(boolean hasGenerator) {
		// Show the loading generator subsettings text
		setSubSettingsLoading(true);

		// Call the set generator webservice
		Bundle params = new Bundle();
		params.putString(POST.URI, Constants.SET_GENERATOR);
		params.putString(POST.SITE_ID, String.valueOf(mSite.getIdSite()));
		// hasGenerator webservice expects an int, but we have a boolean so convert it
		params.putInt(POST.POST_HAS_GENERATOR, hasGenerator ? 1 : 0);

		if (getActivity().getSupportLoaderManager().getLoader(LOADER_ID.LOADER_SET_GENERATOR) == null) {
			getActivity().getSupportLoaderManager().initLoader(LOADER_ID.LOADER_SET_GENERATOR, params, this);
		} else {
			getActivity().getSupportLoaderManager().restartLoader(LOADER_ID.LOADER_SET_GENERATOR, params, this);
		}
	}

	/**
	 * Set if the subsettings are loading or not, this will show a spinner until the webservice responds
	 *
	 * @param show
	 *        true if we should show a spinner
	 */
	private void setSubSettingsLoading(boolean show) {
		View view = getView();
		if (view == null) {
			return;
		}

		if (show) {
			view.findViewById(R.id.data_loading_subsettings).setVisibility(View.VISIBLE);
			view.findViewById(R.id.layout_subsettings_generator).setVisibility(View.GONE);
		} else {
			view.findViewById(R.id.data_loading_subsettings).setVisibility(View.GONE);
		}
	}

	/**
	 * Sets the has generator switch to the state that's in the sitedata
	 */
	private void resetGeneratorSwitchToSiteStatus(View view) {
		if (view == null) {
			return;
		}

		if (mSite.hasGenerator()) {
			((ImageButton) view.findViewById(R.id.ib_settings_has_generator)).setImageDrawable(getResources().getDrawable(R.drawable.switch_yes));
			view.findViewById(R.id.layout_subsettings_generator).setVisibility(View.VISIBLE);
		} else {
			((ImageButton) view.findViewById(R.id.ib_settings_has_generator)).setImageDrawable(getResources().getDrawable(R.drawable.switch_no));
			view.findViewById(R.id.layout_subsettings_generator).setVisibility(View.GONE);
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
					if (attributesResponse.data != null && attributesResponse.data.getAttributes() != null) {
						mSite.setAttributeData(getActivity(), attributesResponse.data);
						mIoAdapter.setSiteObject(mSite);
						updateSelectIOButton();
					}
					break;
				case RESPONSE_CODE.RESPONSE_SESSION_ID:
					mReloginLoaderType = LOADER_ID.ATTRIBUTES;
					callLoginLoader();
					break;
			}
		}
	}

	/**
	 * Parse the json that contains the IO Data
	 *
	 * @param pRestResponse
	 */
	private void parseGeneratorResponse(RestResponse pRestResponse) {
		BaseResponse generatorResponse = JsonParserHelper.getInstance().parseJsonAndShowError(getActivity(), pRestResponse, BaseResponse.class);
		if (generatorResponse != null) {
			switch (generatorResponse.status.code) {
				case RESPONSE_CODE.RESPONSE_OK:
					mSite.setHasGenerator(!mSite.hasGenerator());
					resetGeneratorSwitchToSiteStatus(getView());
					break;
				case RESPONSE_CODE.RESPONSE_SESSION_ID:
					mReloginLoaderType = LOADER_ID.LOADER_SET_GENERATOR;
					callLoginLoader();
					break;
				default:
					resetGeneratorSwitchToSiteStatus(getView());
					break;
			}
		} else {
			resetGeneratorSwitchToSiteStatus(getView());
		}

		setSubSettingsLoading(false);
	}

	@Override
	public Loader<RestResponse> onCreateLoader(int arg0, Bundle params) {
		WebserviceAsync loader = WebserviceAsync.newInstance(getActivity());
		loader.setParams(params);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<RestResponse> loader, RestResponse response) {
		MyLog.i(LOG_TAG, "Finished loading loader " + String.format("%x", loader.getId()));

		// Check which loaderType returned
		switch (loader.getId()) {
			case LOADER_ID.LOADER_SET_GENERATOR:
				MyLog.i(LOG_TAG, "Set generator response");
				parseGeneratorResponse(response);
				break;
			case LOADER_ID.ATTRIBUTES:
				parseAttributesResponse(response);
				break;
			default:
				MyLog.i(LOG_TAG, "Unknown loader id " + loader.getId());
		}
	}

	@Override
	public void onLoaderReset(Loader<RestResponse> loader) {
		// Do nothing
	}

	/**
	 * Recall the loader that caused a session timeout
	 */
	private void callLoaderAfterAutologin() {
		switch (mReloginLoaderType) {
			case LOADER_ID.LOADER_SET_GENERATOR:
				callSetHasGeneratorWebservice(!mSite.hasGenerator());
				break;
			case LOADER_ID.ATTRIBUTES:
				callAttributeDataLoader();
				break;
			default:
				MyLog.e(LOG_TAG, "Unknown loaderType after session time out: " + mReloginLoaderType);
				break;
		}
	}

	@Override
	public void onReloginSuccessful() {
		callLoaderAfterAutologin();
	}

	@Override
	public void onReloginFailed() {
		// TODO: Do we want to inform the user?
	}
}
