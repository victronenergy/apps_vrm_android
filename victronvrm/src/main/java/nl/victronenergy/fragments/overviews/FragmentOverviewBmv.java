/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.fragments.overviews;

import nl.victronenergy.R;
import nl.victronenergy.models.Site;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment that shows the data for the BMV overview
 *
 * @author Victron Energy
 */
public class FragmentOverviewBmv extends FragmentOverview {
	private final String LOG_TAG = "FragmentOverviewBmv";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_overview_bmv, container, false);
	}

	/**
	 * Initialize the overview
	 *
	 * @param site
	 *        The site data used to check what should be hidden
	 */
	@Override
	public void initOverview(Site site) {
		View view = getView();
		if (view == null) {
			return;
		}

		// Show/Hide DC System
		if (site.hasDcSystem()) {
			view.findViewById(R.id.layout_dc_system_out).setVisibility(View.VISIBLE);
			view.findViewById(R.id.layout_dc_system_arrow).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.layout_dc_system_out).setVisibility(View.INVISIBLE);
			view.findViewById(R.id.layout_dc_system_arrow).setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * Initialize the values according to the values in the energy object
	 */
	@Override
	public void initValues() {
		initBatteryValues();
		initDcSystem();
	}

	/**
	 * Set the arrow directions depending on the values in the energy object
	 */
	@Override
	public void initArrowDirections() {
		if (getView() == null) {
			return;
		}

		initDcSystemArrow();
	}
}
