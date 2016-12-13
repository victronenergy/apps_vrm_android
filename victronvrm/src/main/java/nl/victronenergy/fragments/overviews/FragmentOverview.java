package nl.victronenergy.fragments.overviews;

import nl.victronenergy.R;
import nl.victronenergy.models.Attribute;
import nl.victronenergy.models.Site;
import nl.victronenergy.util.AttributeUtils;
import nl.victronenergy.util.Constants.ATTRIBUTE;
import nl.victronenergy.util.Constants.AttributeUnit;
import nl.victronenergy.util.Constants.BUNDLE;
import nl.victronenergy.util.OverviewHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * FragmentOverview used as a base for the actual overviews.<br/>
 * Contains some abstract functions that will setup the overviews.
 *
 * @author M2Mobi
 */
public abstract class FragmentOverview extends Fragment {
	private static final String LOG_TAG = "FragmentOverview";
	private static String[] DATA_ATTRIBUTES_AC_IN_L1 = { ATTRIBUTE.GRID_L1, ATTRIBUTE.GENSET_L1 };
	private static String[] DATA_ATTRIBUTES_AC_IN_L2 = { ATTRIBUTE.GRID_L2, ATTRIBUTE.GENSET_L2 };
	private static String[] DATA_ATTRIBUTES_AC_IN_L3 = { ATTRIBUTE.GRID_L3, ATTRIBUTE.GENSET_L3 };
	private static String[] DATA_ATTRIBUTES_AC_IN_ALL = { ATTRIBUTE.GENSET_L1, ATTRIBUTE.GENSET_L2, ATTRIBUTE.GENSET_L3, ATTRIBUTE.GRID_L1,
			ATTRIBUTE.GRID_L2, ATTRIBUTE.GRID_L3 };
	private static String[] DATA_ATTRIBUTES_CONSUMPTION = { ATTRIBUTE.AC_CONSUMPTION_L1, ATTRIBUTE.AC_CONSUMPTION_L2, ATTRIBUTE.AC_CONSUMPTION_L3 };

	public Site mSite;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mSite = (Site) savedInstanceState.getSerializable(BUNDLE.SITE_OBJECT);
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

		Bundle args = getArguments();
		if (args != null) {
			mSite = (Site) args.getSerializable(BUNDLE.SITE_OBJECT);
		}

		initOverview(mSite);
		initValues();
		initArrowDirections();
	}

	/**
	 * Set the data in a textview
	 *
	 * @param textViewId
	 *        The id of the textview you want to show the data in
	 * @param dataAttributeCode
	 *        The id of the data attribute you want to show the data of
	 * @param hideIfNull
	 *        The type of data you want to show in this textview
	 */
	public void setDataInTextView(int textViewId, String dataAttributeCode, AttributeUnit pAttributeUnit, boolean hideIfNull) {
		View view = getView();
		if (view == null) {
			return;
		}

		TextView textView = (TextView) view.findViewById(textViewId);
		AttributeUtils.setDataInTextView(getActivity(), textView, mSite.getAttributeData().getAttribute(dataAttributeCode), pAttributeUnit,
				hideIfNull);
	}

	/**
	 * Set the data in a textview
	 *
	 * @param textViewId
	 *        The id of the textview you want to show the data in
	 * @param pAttributeUnit
	 *        Array of ids of the data attributes you want to show the data of
	 * @param hideIfNull
	 *        The type of data you want to show in this textview
	 */
	public void setDataInTextView(int textViewId, String[] dataAttributeCodes, AttributeUnit pAttributeUnit, boolean hideIfNull) {
		View view = getView();
		if (view == null) {
			return;
		}

		TextView textView = (TextView) view.findViewById(textViewId);
		if (textView != null) {

			boolean noAttributesAvailable = true;
			float totalValue = 0.0f;
			Attribute attribute;
			for (String dataAttributeCode : dataAttributeCodes) {
				attribute = mSite.getAttributeData().getAttribute(dataAttributeCode);
				if (attribute != null) {
					noAttributesAvailable = false;
					totalValue += attribute.getFloatValue();
				}
			}

			// If none of the requested attributes is available we should hide it or show that it's not available
			if (noAttributesAvailable) {
				if (hideIfNull) {
					textView.setVisibility(View.GONE);
				} else {
					textView.setText(AttributeUtils.getNotAvailableString(getActivity(), pAttributeUnit));
				}
			} else {
				textView.setText(AttributeUtils.getFormattedValue(getActivity(), totalValue, pAttributeUnit));
			}
			textView.setTextColor(getResources().getColor(AttributeUtils.getColorForAttributeUnit(pAttributeUnit)));
		}
	}

	/**
	 * Initialize the overview
	 *
	 * @param site
	 *        The site data used to check what should be hidden
	 */
	public abstract void initOverview(Site site);

	/**
	 * Initialize the values according to the values in the energy object
	 */
	public abstract void initValues();

	/**
	 * Set the arrow directions depending on the values in the energy object
	 */
	public abstract void initArrowDirections();

	/**
	 * Some common functions can be found here
	 */

	/**
	 * Initialize AC In
	 */
	public void initAcIn() {
		View view = getView();
		if (view == null) {
			return;
		}
		// Set the AC In Source
		if (mSite.getAttributeData().isAttributeSet(ATTRIBUTE.GENSET_L1)) {
			((TextView) view.findViewById(R.id.tv_ac_in_header)).setText(getString(R.string.site_detail_header_ac_in_generator));
		} else {
			((TextView) view.findViewById(R.id.tv_ac_in_header)).setText(getString(R.string.site_detail_header_ac_in_grid));
		}

		setDataInTextView(R.id.textview_ac_in_w_phase_1, DATA_ATTRIBUTES_AC_IN_L1, AttributeUnit.WATTS, false);
		setDataInTextView(R.id.textview_ac_in_w_phase_2, DATA_ATTRIBUTES_AC_IN_L2, AttributeUnit.WATTS, true);
		setDataInTextView(R.id.textview_ac_in_w_phase_3, DATA_ATTRIBUTES_AC_IN_L3, AttributeUnit.WATTS, true);
	}

	/**
	 * Initialize AC System
	 */
	public void initAcSystem() {
		setDataInTextView(R.id.textview_ac_system_w_phase_1, ATTRIBUTE.AC_CONSUMPTION_L1, AttributeUnit.WATTS, false);
		setDataInTextView(R.id.textview_ac_system_w_phase_2, ATTRIBUTE.AC_CONSUMPTION_L2, AttributeUnit.WATTS, true);
		setDataInTextView(R.id.textview_ac_system_w_phase_3, ATTRIBUTE.AC_CONSUMPTION_L3, AttributeUnit.WATTS, true);
	}

	/**
	 * Arrow between multi and AC In
	 */
	public void initAcInArrow() {
		ImageView arrowMultiAcIn = (ImageView) getView().findViewById(R.id.imageview_arrow_multi_ac_in);
		arrowMultiAcIn.setImageDrawable(OverviewHelper.getArrowDrawableForPositiveRight(getActivity(), mSite.getAttributeData()
				.getCombinedValueForAttributes(DATA_ATTRIBUTES_AC_IN_ALL)));
	}

	/**
	 * Arrow between multi and AC System
	 */
	public void initAcSystemArrow() {
		float value = mSite.getAttributeData().getCombinedValueForAttributes(DATA_ATTRIBUTES_CONSUMPTION);
		ImageView arrowMultiAcSystem = (ImageView) getView().findViewById(R.id.imageview_arrow_multi_ac_system);
		arrowMultiAcSystem.setImageDrawable(OverviewHelper.getArrowDrawableForPositiveRight(getActivity(), value));
	}

	/**
	 * Initialize common battery values
	 */
	public void initBatteryValues() {
		setDataInTextView(R.id.textview_battery_v, ATTRIBUTE.BATTERY_VOLTAGE, AttributeUnit.VOLT, false);
		setDataInTextView(R.id.textview_battery_a, ATTRIBUTE.CONSUMED_AMPHOURS, AttributeUnit.AMPHOUR, false);
		setDataInTextView(R.id.textview_battery_percentage, ATTRIBUTE.STATE_OF_CHARGE, AttributeUnit.PERCENTAGE, true);
		setDataInTextView(R.id.textview_battery_ttg, ATTRIBUTE.TIME_TO_GO, AttributeUnit.TIME, true);
	}

	/**
	 * Arrow value between multi and battery
	 */
	public void initBatteryCurrent() {
		setDataInTextView(R.id.textview_multi_battery_a, ATTRIBUTE.VEBUS_CHARGE_CURRENT, AttributeUnit.AMPS, false);
	}

	/**
	 * Arrow between multi and battery
	 */
	public void initBatteryCurrentArrow() {
		ImageView arrowMultiBattery = (ImageView) getView().findViewById(R.id.imageview_arrow_multi_battery);
		Attribute attribute = mSite.getAttributeData().getAttribute(ATTRIBUTE.VEBUS_CHARGE_CURRENT);
		if (attribute != null) {
			arrowMultiBattery.setImageDrawable(OverviewHelper.getArrowDrawableForPositiveDown(getActivity(), attribute.getFloatValue()));
		}
	}

	/**
	 * Initialize DC System value
	 */
	public void initDcSystem() {
		setDataInTextView(R.id.textview_dc_system_w, ATTRIBUTE.DC_SYSTEM, AttributeUnit.WATTS, false);
	}

	/**
	 * Initialize the DC System arrow
	 */
	public void initDcSystemArrow() {
		ImageView arrowDcSystem = (ImageView) getView().findViewById(R.id.imageview_arrow_battery_dc_system);
		Attribute attribute = mSite.getAttributeData().getAttribute(ATTRIBUTE.DC_SYSTEM);
		if (attribute != null) {
			arrowDcSystem.setImageDrawable(OverviewHelper.getArrowDrawableForPositiveRight(getActivity(), attribute.getFloatValue()));
		}
	}
}
