/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.models.widgets;

import java.io.Serializable;

import nl.victronenergy.R;
import nl.victronenergy.models.Attribute;
import nl.victronenergy.models.AttributeData;
import nl.victronenergy.util.AttributeUtils;
import nl.victronenergy.util.Constants.ATTRIBUTE;
import nl.victronenergy.util.Constants.AttributeUnit;

import android.content.Context;

/**
 * Created by Victron Energy on 25-2-14.
 */
public class SummaryWidgetDcPowerSystem extends SummaryWidget implements Serializable {

	@Override
	protected void initIcon(Context pContext, AttributeData pAttributeData) {
		mIcon = R.drawable.ic_kwh_metre;
	}

	@Override
	protected void initText(Context pContext, AttributeData pAttributeData) {
		Attribute stateOfCharge = pAttributeData.getAttribute(ATTRIBUTE.DC_SYSTEM);
		mText = AttributeUtils.getFormattedValue(pContext, stateOfCharge.getFloatValue(), AttributeUnit.WATTS);
	}

	@Override
	protected void initTitle(Context pContext, AttributeData pAttributeData) {
		mTitle = pContext.getString(R.string.widget_title_dc_power_system);
	}

	@Override
	public boolean areRequiredAttributesAvailable(AttributeData pAttributeData) {
		if (pAttributeData.isAttributeSet(ATTRIBUTE.DC_SYSTEM)) {
			return true;
		}
		return false;
	}
}
