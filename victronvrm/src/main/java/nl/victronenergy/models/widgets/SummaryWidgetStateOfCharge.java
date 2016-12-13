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
 * A widget that shows the state of charge of a battery, this shows a percentage and an icon.<br/>
 * The icon can be:<br/>
 * <ul>
 * <li>FULLY CHARGED</li>
 * <li>THREE QUARTER CHARGED</li>
 * <li>HALF CHARGED</li>
 * <li>QUARTER CHARGED</li>
 * <li>EMPTY</li>
 * </ul>
 * Created by M2Mobi on 25-2-14.
 */
public class SummaryWidgetStateOfCharge extends SummaryWidget implements Serializable {
	/* Percentages of how much the battery is charged */
	private static final float BATTERY_FULLY_CHARGED = 100.0f;
	private static final float BATTERY_THREE_QUARTER_CHARGED = 75.0f;
	private static final float BATTERY_HALF_CHARGED = 50.0f;
	private static final float BATTERY_QUARTER_CHARGED = 25.0f;

	@Override
	protected void initIcon(Context pContext, AttributeData pAttributeData) {
		if (pAttributeData.isAttributeSet(ATTRIBUTE.STATE_OF_CHARGE)) {
			Attribute attribute = pAttributeData.getAttribute(ATTRIBUTE.STATE_OF_CHARGE);

			// Check which icon we should use
			float stateOfCharge = attribute.getFloatValue();
			if (stateOfCharge >= BATTERY_FULLY_CHARGED) {
				mIcon = R.drawable.ic_battery_4;
			} else if (stateOfCharge >= BATTERY_THREE_QUARTER_CHARGED) {
				mIcon = R.drawable.ic_battery_3;
			} else if (stateOfCharge >= BATTERY_HALF_CHARGED) {
				mIcon = R.drawable.ic_battery_2;
			} else if (stateOfCharge >= BATTERY_QUARTER_CHARGED) {
				mIcon = R.drawable.ic_battery_1;
			} else {
				mIcon = R.drawable.ic_battery_0;
			}
		} else if (pAttributeData.isAttributeSet(ATTRIBUTE.BATTERY_VOLTAGE)) {
			mIcon = R.drawable.ic_battery_voltage;
		}
	}

	@Override
	protected void initText(Context pContext, AttributeData pAttributeData) {
		String value = "";
		if (pAttributeData.isAttributeSet(ATTRIBUTE.STATE_OF_CHARGE)) {
			value = AttributeUtils.getFormattedValue(pContext, pAttributeData.getAttribute(ATTRIBUTE.STATE_OF_CHARGE).getFloatValue(),
					AttributeUnit.PERCENTAGE);
		} else if (pAttributeData.isAttributeSet(ATTRIBUTE.BATTERY_VOLTAGE)) {
			value = AttributeUtils.getFormattedValue(pContext, pAttributeData.getAttribute(ATTRIBUTE.BATTERY_VOLTAGE).getFloatValue(),
					AttributeUnit.VOLT);
		}

		mText = value;
	}

	@Override
	protected void initTitle(Context pContext, AttributeData pAttributeData) {
		mTitle = pContext.getString(R.string.widget_title_state_of_charge);
	}

	@Override
	public boolean areRequiredAttributesAvailable(AttributeData pAttributeData) {
		if (pAttributeData.isAttributeSet(ATTRIBUTE.STATE_OF_CHARGE) || pAttributeData.isAttributeSet(ATTRIBUTE.BATTERY_VOLTAGE)) {
			return true;
		}
		return false;
	}
}
