/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.fragments.overviews;

import nl.victronenergy.R;
import nl.victronenergy.models.Site;
import nl.victronenergy.util.Constants.ATTRIBUTE;
import nl.victronenergy.util.OverviewHelper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment that shows the data for the BMV+Multi overview
 *
 * @author Victron Energy
 */
public class FragmentOverviewBmvMulti extends FragmentOverview {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_overview_bmv_multi, container, false);
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
	}
}
