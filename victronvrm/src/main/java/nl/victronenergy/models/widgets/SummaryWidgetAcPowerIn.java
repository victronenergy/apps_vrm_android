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
public class SummaryWidgetAcPowerIn extends SummaryWidget implements Serializable {
	private static final String[] ATTRIBUTE_CODES = new String[] { ATTRIBUTE.GRID_L1, ATTRIBUTE.GRID_L2, ATTRIBUTE.GRID_L3, ATTRIBUTE.GENSET_L1,
			ATTRIBUTE.GENSET_L2, ATTRIBUTE.GENSET_L3 };

	@Override
	protected void initIcon(Context pContext, AttributeData pAttributeData) {
		mIcon = R.drawable.ic_grid;
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
		// Check where the power comes from
		if (pAttributeData.isAttributeSet(ATTRIBUTE.GENSET_L1)) {
			mTitle = pContext.getString(R.string.widget_title_ac_in_genset);
		} else {
			mTitle = pContext.getString(R.string.widget_title_ac_in_grid);
		}

	}

	@Override
	public boolean areRequiredAttributesAvailable(AttributeData pAttributeData) {
		if (pAttributeData.isAttributeSet(ATTRIBUTE.GRID_L1) || pAttributeData.isAttributeSet(ATTRIBUTE.GENSET_L1)) {
			return true;
		}
		return false;
	}
}
