/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.models.widgets;

import java.io.Serializable;

import nl.victronenergy.R;
import nl.victronenergy.models.AttributeData;
import nl.victronenergy.util.AttributeUtils;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.ATTRIBUTE;
import android.content.Context;

/**
 * Created by Victron Energy on 25-2-14.
 */
public class SummaryWidgetPvPower extends SummaryWidget implements Serializable {
	private static final String[] ATTRIBUTE_CODES = new String[] { ATTRIBUTE.PV_AC_COUPLED_OUTPUT_L1, ATTRIBUTE.PV_AC_COUPLED_OUTPUT_L2,
			ATTRIBUTE.PV_AC_COUPLED_OUTPUT_L3, ATTRIBUTE.PV_AC_COUPLED_INPUT_L1, ATTRIBUTE.PV_AC_COUPLED_INPUT_L2, ATTRIBUTE.PV_AC_COUPLED_INPUT_L3,
			ATTRIBUTE.PV_DC_COUPLED };

	@Override
	protected void initIcon(Context pContext, AttributeData pAttributeData) {
		mIcon = R.drawable.ic_weather_00;
	}

	@Override
	protected void initText(Context pContext, AttributeData pAttributeData) {
		float value = 0.0f;
		for (String dataAttributeCode : ATTRIBUTE_CODES) {
			if (pAttributeData.isAttributeSet(dataAttributeCode)) {
				value += pAttributeData.getAttribute(dataAttributeCode).getFloatValue();
			}
		}

		mText = AttributeUtils.getFormattedValue(pContext, value, Constants.AttributeUnit.WATTS);
	}

	@Override
	protected void initTitle(Context pContext, AttributeData pAttributeData) {
		mTitle = pContext.getString(R.string.widget_title_pv_power);
	}

	@Override
	public boolean areRequiredAttributesAvailable(AttributeData pAttributeData) {
		if (pAttributeData.isAttributeSet(ATTRIBUTE.PV_AC_COUPLED_OUTPUT_L1) || pAttributeData.isAttributeSet(ATTRIBUTE.PV_AC_COUPLED_INPUT_L1)
				|| pAttributeData.isAttributeSet(ATTRIBUTE.PV_DC_COUPLED)) {
			return true;
		}
		return false;
	}
}
