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
 * Fragment that is shown when there is no valid configuration
 *
 * @author Victron Energy
 */
public class FragmentOverviewNoConnection extends FragmentOverview {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_overview_no_connection, container, false);
	}

	@Override
	public void initOverview(Site site) {
		// Nothing to do here
	}

	@Override
	public void initValues() {
		// No values to show in this overview
	}

	@Override
	public void initArrowDirections() {
		// No arrows in this overview
	}
}
