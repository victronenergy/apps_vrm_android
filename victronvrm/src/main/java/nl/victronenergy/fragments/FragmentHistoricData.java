/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.fragments;

import nl.victronenergy.R;
import nl.victronenergy.models.AttributeData;
import nl.victronenergy.models.AttributesResponse;
import nl.victronenergy.models.Site;
import nl.victronenergy.util.AttributeUtils;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.ATTRIBUTE;
import nl.victronenergy.util.Constants.AttributeUnit;
import nl.victronenergy.util.Constants.BUNDLE;
import nl.victronenergy.util.Constants.LOADER_ID;
import nl.victronenergy.util.Constants.POST;
import nl.victronenergy.util.Constants.RESPONSE_CODE;
import nl.victronenergy.util.MyLog;
import nl.victronenergy.util.webservice.JsonParserHelper;
import nl.victronenergy.util.webservice.RestResponse;
import nl.victronenergy.util.webservice.WebserviceAsync;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

/**
 * Shows a list of 16 historic values
 *
 * @author Victron Energy
 */
public class FragmentHistoricData extends VictronVRMFragment implements LoaderCallbacks<RestResponse> {
	private static final String LOG_TAG = "FragmentHistoricData";
	private static final String HISTORIC_ATTRIBUTE_IDS = "[" + ATTRIBUTE.DEEPEST_DISCHARGE // --
			+ "," + ATTRIBUTE.LAST_DISCHARGE // --
			+ "," + ATTRIBUTE.AVERAGE_DISCHARGE // --
			+ "," + ATTRIBUTE.CHARGE_CYCLES  // --
			+ "," + ATTRIBUTE.FULL_DISCHARGE_CYCLES  // --
			+ "," + ATTRIBUTE.TOTAL_AH_DRAWN // --
			+ "," + ATTRIBUTE.MINIMUM_VOLTAGE // --
			+ "," + ATTRIBUTE.MAXIMUM_VOLTAGE // --
			+ "," + ATTRIBUTE.TIME_SINCE_LAST_FULL_CHARGE // --
			+ "," + ATTRIBUTE.AUTOMATIC_SYNCS // --
			+ "," + ATTRIBUTE.LOW_VOLTAGE_ALARMS // --
			+ "," + ATTRIBUTE.HIGH_VOLTAGE_ALARMS // --
			+ "," + ATTRIBUTE.LOW_STARTER_VOLTAGE_ALARMS // --
			+ "," + ATTRIBUTE.HIGH_STARTER_VOLTAGE_ALARMS // --
			+ "," + ATTRIBUTE.MINIMUM_STARTER_VOLTAGE // --
			+ "," + ATTRIBUTE.MAXIMUM_STARTER_VOLTAGE + "]";

	private Site mSite;
	private boolean mIsHistoricDataDoneLoading = false;
	private AttributeData mAttributeData;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_historic_data, null);

		if (savedInstanceState != null) {
			mSite = (Site) savedInstanceState.getSerializable(BUNDLE.SITE_OBJECT);
		} else {
			if (getArguments() != null) {
				mSite = (Site) getArguments().getSerializable(BUNDLE.SITE_OBJECT);
			}
		}

		((TextView) view.findViewById(R.id.textview_site_name)).setText(mSite.getName());

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		// Only make the call when there is a valid site object
		if (mSite != null) {
			callHistoricDataLoader();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getActivity() != null) {
			EasyTracker.getInstance().setContext(getActivity());
			EasyTracker.getTracker().sendView(getString(R.string.ga_activity_historic_data));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(BUNDLE.SITE_OBJECT, mSite);
	}

	/**
	 * Init the historic data textfields with the historic data
	 */
	private void initHistoricData() {
		View view = getView();
		if (mAttributeData == null || view == null) {
			return;
		}

		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_deepest_discharge),
				mAttributeData.getAttribute(ATTRIBUTE.DEEPEST_DISCHARGE), AttributeUnit.AMPHOUR, false);
		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_last_discharge),
				mAttributeData.getAttribute(ATTRIBUTE.LAST_DISCHARGE), AttributeUnit.AMPHOUR, false);
		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_average_discharge),
				mAttributeData.getAttribute(ATTRIBUTE.AVERAGE_DISCHARGE), AttributeUnit.AMPHOUR, false);
		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_charge_cycles),
				mAttributeData.getAttribute(ATTRIBUTE.CHARGE_CYCLES), AttributeUnit.COUNT, false);
		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_full_discharge_cycles),
				mAttributeData.getAttribute(ATTRIBUTE.FULL_DISCHARGE_CYCLES), AttributeUnit.COUNT, false);
		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_total_ah_drawn),
				mAttributeData.getAttribute(ATTRIBUTE.TOTAL_AH_DRAWN), AttributeUnit.AMPHOUR, false);
		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_minimum_voltage),
				mAttributeData.getAttribute(ATTRIBUTE.MINIMUM_VOLTAGE), AttributeUnit.VOLT, false);
		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_maximum_voltage),
				mAttributeData.getAttribute(ATTRIBUTE.MAXIMUM_VOLTAGE), AttributeUnit.VOLT, false);
		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_time_since_last_full_charge),
				mAttributeData.getAttribute(ATTRIBUTE.TIME_SINCE_LAST_FULL_CHARGE), AttributeUnit.TIME, false);
		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_automatic_syncs),
				mAttributeData.getAttribute(ATTRIBUTE.AUTOMATIC_SYNCS), AttributeUnit.COUNT, false);
		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_low_voltage_alarms),
				mAttributeData.getAttribute(ATTRIBUTE.LOW_VOLTAGE_ALARMS), AttributeUnit.COUNT, false);
		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_high_voltage_alarms),
				mAttributeData.getAttribute(ATTRIBUTE.HIGH_VOLTAGE_ALARMS), AttributeUnit.COUNT, false);
		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_low_starter_voltage_alarms),
				mAttributeData.getAttribute(ATTRIBUTE.LOW_STARTER_VOLTAGE_ALARMS), AttributeUnit.COUNT, false);
		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_high_starter_voltage_alarms),
				mAttributeData.getAttribute(ATTRIBUTE.HIGH_STARTER_VOLTAGE_ALARMS), AttributeUnit.COUNT, false);
		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_minimum_starter_voltage),
				mAttributeData.getAttribute(ATTRIBUTE.MINIMUM_STARTER_VOLTAGE), AttributeUnit.VOLT, false);
		AttributeUtils.setDataInTextView(getActivity(), (TextView) view.findViewById(R.id.tv_historic_maximum_starter_voltage),
				mAttributeData.getAttribute(ATTRIBUTE.MAXIMUM_STARTER_VOLTAGE), AttributeUnit.VOLT, false);
	}

	/**
	 * Call loader to get historic data
	 */
	private void callHistoricDataLoader() {
		Bundle params = new Bundle();
		params.putString(POST.URI, Constants.GET_ATTRIBUTES);
		params.putString(POST.ATTRIBUTE_IDS, HISTORIC_ATTRIBUTE_IDS);
		params.putInt(POST.INSTANCE, mSite.getMainMonitorInstance());
		params.putInt(POST.SITE_ID, mSite.getIdSite());

		if (getActivity().getSupportLoaderManager().getLoader(LOADER_ID.ATTRIBUTES) == null) {
			getActivity().getSupportLoaderManager().initLoader(LOADER_ID.ATTRIBUTES, params, this);
		} else {
			getActivity().getSupportLoaderManager().restartLoader(LOADER_ID.ATTRIBUTES, params, this);
		}
	}

	/**
	 * Check if all the data is loaded and show it
	 */
	private void checkLoadingFinished() {
		View view = getView();
		if (view == null) {
			MyLog.e(LOG_TAG, "View is null on checkLoadingFinished()");
			return;
		}

		if (mIsHistoricDataDoneLoading) {
			view.findViewById(R.id.data_loading).setVisibility(View.GONE);
			view.findViewById(R.id.sv_historic_data).setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Parse the historic data json
	 *
	 * @param pRestResponse
	 *        The response returned by the webservice
	 */
	private void parseAttributesResponse(RestResponse pRestResponse) {
		AttributesResponse attributesResponse = JsonParserHelper.getInstance().parseJsonAndShowError(getActivity(), pRestResponse,
				AttributesResponse.class);
		if (attributesResponse != null) {
			switch (attributesResponse.status.code) {
				case RESPONSE_CODE.RESPONSE_OK:
					mAttributeData = attributesResponse.data;
					initHistoricData();
					mIsHistoricDataDoneLoading = true;
					break;
				case RESPONSE_CODE.RESPONSE_SESSION_ID:
					callLoginLoader();
					break;
				default:
					mIsHistoricDataDoneLoading = true;
					break;
			}
		} else {
			mIsHistoricDataDoneLoading = true;
		}
	}

	@Override
	public Loader<RestResponse> onCreateLoader(int arg0, Bundle params) {
		WebserviceAsync loader = WebserviceAsync.newInstance(getActivity());
		loader.setParams(params);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<RestResponse> loader, RestResponse pRestResponse) {
		switch (loader.getId()) {
			case LOADER_ID.ATTRIBUTES:
				parseAttributesResponse(pRestResponse);
				break;
		}
		checkLoadingFinished();
	}

	@Override
	public void onLoaderReset(Loader<RestResponse> arg0) {
		// Do nothing
	}

	@Override
	public void onReloginSuccessful() {
		callHistoricDataLoader();
	}

	@Override
	public void onReloginFailed() {
		Toast.makeText(getActivity(), "Relogin failed", Toast.LENGTH_LONG).show();
		// TODO: Show error message that we really can't load data
	}
}
