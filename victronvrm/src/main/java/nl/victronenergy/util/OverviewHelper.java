/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.util;

import nl.victronenergy.R;
import nl.victronenergy.models.Attribute;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Helper class that contains functions to setup the overview
 *
 * @author Victron Energy
 */
public class OverviewHelper {

	/**
	 * Set the ac visibility according to the VeBusState
	 *
	 * @param view
	 *        The view that contains the AC layouts that need to be (un)hidden
	 * @param vebusStateAttribute
	 *        The state of the vebus
	 */
	public static void setACVisibility(View view, Attribute vebusStateAttribute) {
		int veBusState = Constants.VEBUS_OFF;
		if (vebusStateAttribute != null) {
			veBusState = vebusStateAttribute.getValueEnum();
		}

		switch (veBusState) {
			case Constants.VEBUS_OFF:
				// Off: hide AC in and AC out
				view.findViewById(R.id.layout_ac_in).setVisibility(View.INVISIBLE);
				view.findViewById(R.id.layout_ac_system).setVisibility(View.INVISIBLE);
				view.findViewById(R.id.layout_arrow_multi_ac_in).setVisibility(View.INVISIBLE);
				view.findViewById(R.id.layout_arrow_multi_ac_system).setVisibility(View.INVISIBLE);
				break;
			case Constants.VEBUS_LOW_POWER:
				// low power mode: hide AC in
				view.findViewById(R.id.layout_ac_in).setVisibility(View.INVISIBLE);
				view.findViewById(R.id.layout_ac_system).setVisibility(View.VISIBLE);
				view.findViewById(R.id.layout_arrow_multi_ac_in).setVisibility(View.INVISIBLE);
				view.findViewById(R.id.layout_arrow_multi_ac_system).setVisibility(View.VISIBLE);
				break;
			case Constants.VEBUS_INVERTING:
				// inverting: hide AC in
				view.findViewById(R.id.layout_ac_in).setVisibility(View.INVISIBLE);
				view.findViewById(R.id.layout_ac_system).setVisibility(View.VISIBLE);
				view.findViewById(R.id.layout_arrow_multi_ac_in).setVisibility(View.INVISIBLE);
				view.findViewById(R.id.layout_arrow_multi_ac_system).setVisibility(View.VISIBLE);
				break;
			default:
				// no hiding
				view.findViewById(R.id.layout_ac_in).setVisibility(View.VISIBLE);
				view.findViewById(R.id.layout_ac_system).setVisibility(View.VISIBLE);
				view.findViewById(R.id.layout_arrow_multi_ac_in).setVisibility(View.VISIBLE);
				view.findViewById(R.id.layout_arrow_multi_ac_system).setVisibility(View.VISIBLE);
				break;
		}
	}

	/**
	 * Returns an arrow drawable for when the positive direction is Right
	 *
	 * @param context
	 *        The context needed to load the drawable
	 * @param value
	 *        The dataAttribute value to check the flow for
	 * @return Drawable of an arrow pointing in the direction of the flow
	 */
	public static Drawable getArrowDrawableForPositiveRight(Context context, float value) {
		if (value > 0.0) {
			return context.getResources().getDrawable(R.drawable.arrow_right);
		} else if (value < 0.0) {
			return context.getResources().getDrawable(R.drawable.arrow_left);
		} else {
			return context.getResources().getDrawable(R.drawable.arrow_dot);
		}
	}

	/**
	 * Returns an arrow drawable for when the positive direction is Left
	 *
	 * @param context
	 *        The context needed to load the drawable
	 * @param value
	 *        The dataAttribute value to check the flow for
	 * @return Drawable of an arrow pointing in the direction of the flow
	 */
	public static Drawable getArrowDrawableForPositiveLeft(Context context, float value) {
		if (value > 0.0) {
			return context.getResources().getDrawable(R.drawable.arrow_left);
		} else if (value < 0.0) {
			return context.getResources().getDrawable(R.drawable.arrow_right);
		} else {
			return context.getResources().getDrawable(R.drawable.arrow_dot);
		}
	}

	/**
	 * Returns an arrow drawable for when the positive direction is Up
	 *
	 * @param context
	 *        The context needed to load the drawable
	 * @param value
	 *        The dataAttribute value to check the flow for
	 * @return Drawable of an arrow pointing in the direction of the flow
	 */
	public static Drawable getArrowDrawableForPositiveUp(Context context, float value) {
		if (value > 0.0) {
			return context.getResources().getDrawable(R.drawable.arrow_up);
		} else if (value < 0.0) {
			return context.getResources().getDrawable(R.drawable.arrow_down);
		} else {
			return context.getResources().getDrawable(R.drawable.arrow_dot_up);
		}
	}

	/**
	 * Returns an arrow drawable for when the positive direction is Down
	 *
	 * @param context
	 *        The context needed to load the drawable
	 * @param value
	 *        The dataAttribute value to check the flow for
	 * @return Drawable of an arrow pointing in the direction of the flow
	 */
	public static Drawable getArrowDrawableForPositiveDown(Context context, float value) {
		if (value > 0.0) {
			return context.getResources().getDrawable(R.drawable.arrow_down);
		} else if (value < 0.0) {
			return context.getResources().getDrawable(R.drawable.arrow_up);
		} else {
			return context.getResources().getDrawable(R.drawable.arrow_dot_up);
		}
	}
}
