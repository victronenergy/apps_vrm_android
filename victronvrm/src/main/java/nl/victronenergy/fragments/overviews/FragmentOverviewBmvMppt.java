package nl.victronenergy.fragments.overviews;

import nl.victronenergy.R;
import nl.victronenergy.models.Attribute;
import nl.victronenergy.models.Site;
import nl.victronenergy.util.Constants.ATTRIBUTE;
import nl.victronenergy.util.Constants.AttributeUnit;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Fragment that shows the data for the BMV+Multi+MPPT overview
 *
 * @author M2Mobi
 */
public class FragmentOverviewBmvMppt extends FragmentOverview {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_overview_bmv_mppt, container, false);
	}

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

	@Override
	public void initValues() {
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

		initDcSystemArrow();

		// Arrow between battery and MPPT
		ImageView arrowBatteryMPPT = (ImageView) getView().findViewById(R.id.imageview_arrow_battery_mppt);
		Attribute attribute = mSite.getAttributeData().getAttribute(ATTRIBUTE.PV_DC_COUPLED);
		if (attribute != null && attribute.getFloatValue() != 0.0) {
			arrowBatteryMPPT.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.arrow_right));
		}
	}
}
