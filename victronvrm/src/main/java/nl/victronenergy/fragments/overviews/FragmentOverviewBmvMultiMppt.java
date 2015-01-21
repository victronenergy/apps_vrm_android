/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.fragments.overviews;

import nl.victronenergy.R;
import nl.victronenergy.models.Attribute;
import nl.victronenergy.models.Site;
import nl.victronenergy.util.Constants.ATTRIBUTE;
import nl.victronenergy.util.Constants.AttributeUnit;
import nl.victronenergy.util.OverviewHelper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Fragment that shows the data for the BMV+Multi+MPPT overview
 *
 * @author Victron Energy
 */
public class FragmentOverviewBmvMultiMppt extends FragmentOverview {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_overview_bmv_multi_mppt, container, false);
	}

	@Override
	public void initOverview(Site site) {
		View view = getView();
		if (view == null) {
			return;
		}

		// Check if we need to hide AC In/AC System
		OverviewHelper.setACVisibility(view, mSite.getAttributeData().getAttribute(ATTRIBUTE.VEBUS_STATE));

		// Show/Hide DC System
		if (site.hasDcSystem()) {
			view.findViewById(R.id.layout_dc_system_out).setVisibility(View.VISIBLE);
			view.findViewById(R.id.layout_dc_system_arrow).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.layout_dc_system_out).setVisibility(View.INVISIBLE);
			view.findViewById(R.id.layout_dc_system_arrow).setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void initValues() {
		initAcIn();
		initAcSystem();
		initBatteryCurrent();
		initBatteryValues();
		initDcSystem();

		// Initialize MPPT values
		setDataInTextView(R.id.textview_mppt_w, ATTRIBUTE.PV_DC_COUPLED, AttributeUnit.WATTS, false);
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

		// Arrow between battery and MPPT
		ImageView arrowBatteryMPPT = (ImageView) getView().findViewById(R.id.imageview_arrow_battery_mppt);
		Attribute attribute = mSite.getAttributeData().getAttribute(ATTRIBUTE.PV_DC_COUPLED);
		if (attribute != null && attribute.getFloatValue() != 0.0) {
			arrowBatteryMPPT.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.arrow_right));
		}
	}
}
