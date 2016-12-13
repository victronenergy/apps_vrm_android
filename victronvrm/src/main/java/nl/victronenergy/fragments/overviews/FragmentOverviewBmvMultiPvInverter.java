package nl.victronenergy.fragments.overviews;

import nl.victronenergy.R;
import nl.victronenergy.models.Site;
import nl.victronenergy.util.Constants.ATTRIBUTE;
import nl.victronenergy.util.Constants.AttributeUnit;
import nl.victronenergy.util.OverviewHelper;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Fragment that shows the data for the BMV overview
 *
 * @author M2Mobi
 */
public class FragmentOverviewBmvMultiPvInverter extends FragmentOverview {
	private final String LOG_TAG = "FragmentOverviewBmvMultiPvInverter";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_overview_bmv_multi_pvinverter, container, false);
	}

	@Override
	public void initOverview(Site site) {
		View view = getView();
		if (view == null) {
			return;
		}

		// Check if we need to hide AC In/AC System
		OverviewHelper.setACVisibility(getView(), mSite.getAttributeData().getAttribute(ATTRIBUTE.VEBUS_STATE));

		// Hide TTG and W if the site has no VEBus
		boolean hasMulti = mSite.getAttributeData().isAttributeSet(ATTRIBUTE.AC_CONSUMPTION_L1);
		if (hasMulti) {
			getView().findViewById(R.id.textview_battery_ttg).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.textview_dc_system_w).setVisibility(View.VISIBLE);
		} else {
			getView().findViewById(R.id.textview_battery_ttg).setVisibility(View.GONE);
			getView().findViewById(R.id.textview_dc_system_w).setVisibility(View.GONE);
		}

		// Show/Hide DC System
		if (site.hasDcSystem()) {
			getView().findViewById(R.id.layout_dc_system_out).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.layout_dc_system_arrow).setVisibility(View.VISIBLE);
		} else {
			getView().findViewById(R.id.layout_dc_system_out).setVisibility(View.INVISIBLE);
			getView().findViewById(R.id.layout_dc_system_arrow).setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void initValues() {
		initAcIn();
		initAcSystem();
		initBatteryCurrent();
		initBatteryValues();
		initDcSystem();

		// Value at arrow between Multi and PV Inverter
		setDataInTextView(R.id.textview_multi_pv_inverter_w_phase_1, ATTRIBUTE.PV_AC_COUPLED_OUTPUT_L1, AttributeUnit.WATTS, false);
		setDataInTextView(R.id.textview_multi_pv_inverter_w_phase_2, ATTRIBUTE.PV_AC_COUPLED_OUTPUT_L2, AttributeUnit.WATTS, true);
		setDataInTextView(R.id.textview_multi_pv_inverter_w_phase_3, ATTRIBUTE.PV_AC_COUPLED_OUTPUT_L3, AttributeUnit.WATTS, true);
	}

	@Override
	public void initArrowDirections() {
		if (getView() == null) {
			return;
		}

		initAcInArrow();
		initAcSystemArrow();
		initBatteryCurrentArrow();
		initDcSystemArrow();

		// Arrow between multi and pv inverter
		String[] attributeIds = { ATTRIBUTE.PV_AC_COUPLED_OUTPUT_L1, ATTRIBUTE.PV_AC_COUPLED_OUTPUT_L2, ATTRIBUTE.PV_AC_COUPLED_OUTPUT_L3 };

		ImageView arrowMultiPvInverter = (ImageView) getView().findViewById(R.id.imageview_arrow_multi_pv_inverter);
		Drawable drawableMultiPvInverter;
		if (mSite.getAttributeData().getCombinedValueForAttributes(attributeIds) != 0.0) {
			drawableMultiPvInverter = getActivity().getResources().getDrawable(R.drawable.arrow_left);
		} else {
			drawableMultiPvInverter = getActivity().getResources().getDrawable(R.drawable.arrow_dot);
		}
		arrowMultiPvInverter.setImageDrawable(drawableMultiPvInverter);
	}
}
