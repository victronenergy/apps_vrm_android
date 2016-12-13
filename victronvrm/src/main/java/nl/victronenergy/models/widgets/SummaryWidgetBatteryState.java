package nl.victronenergy.models.widgets;

import java.io.Serializable;

import nl.victronenergy.R;
import nl.victronenergy.models.AttributeData;
import nl.victronenergy.util.Constants.ATTRIBUTE;
import android.content.Context;

/**
 * Created by M2Mobi on 25-2-14.
 */
public class SummaryWidgetBatteryState extends SummaryWidget implements Serializable {

	@Override
	protected void initIcon(Context pContext, AttributeData pAttributeData) {
		int state = pAttributeData.getAttribute(ATTRIBUTE.BATTERY_STATE).getValueEnum();
		if (state == 0) {
			mIcon = R.drawable.ic_battery_idle;
		} else if (state == 1) {
			mIcon = R.drawable.ic_battery_charging;
		} else if (state == 2) {
			mIcon = R.drawable.ic_battery_discharging;
		} else {
			mIcon = R.drawable.ic_battery_idle;
		}
	}

	@Override
	protected void initText(Context pContext, AttributeData pAttributeData) {
		int state = pAttributeData.getAttribute(ATTRIBUTE.BATTERY_STATE).getValueEnum();
		if (state == 0) {
			mText = pContext.getString(R.string.widget_value_battery_state_idle);
		} else if (state == 1) {
			mText = pContext.getString(R.string.widget_value_battery_state_charging);
		} else if (state == 2) {
			mText = pContext.getString(R.string.widget_value_battery_state_discharging);
		} else {
			mText = "";
		}
	}

	@Override
	protected void initTitle(Context pContext, AttributeData pAttributeData) {
		mTitle = pContext.getString(R.string.widget_title_battery_state);
	}

	@Override
	public boolean areRequiredAttributesAvailable(AttributeData pAttributeData) {
		if (pAttributeData.isAttributeSet(ATTRIBUTE.BATTERY_STATE)) {
			return true;
		}
		return false;
	}
}
