package nl.victronenergy.models.widgets;

import java.io.Serializable;

import nl.victronenergy.R;
import nl.victronenergy.models.AttributeData;
import nl.victronenergy.util.AttributeUtils;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.ATTRIBUTE;
import android.content.Context;

/**
 * Created by M2Mobi on 25-2-14.
 */
public class SummaryWidgetAcPowerOut extends SummaryWidget implements Serializable {
	private static final String[] ATTRIBUTE_CODES = new String[] { ATTRIBUTE.AC_CONSUMPTION_L1, ATTRIBUTE.AC_CONSUMPTION_L2,
			ATTRIBUTE.AC_CONSUMPTION_L3 };

	@Override
	protected void initIcon(Context pContext, AttributeData pAttributeData) {
		mIcon = R.drawable.ic_kwh_metre;
	}

	@Override
	protected void initText(Context pContext, AttributeData pAttributeData) {
		float value = 0.0f;
		for (String dataAttributeCodes : ATTRIBUTE_CODES) {
			if (pAttributeData.isAttributeSet(dataAttributeCodes)) {
				value += pAttributeData.getAttribute(dataAttributeCodes).getFloatValue();
			}
		}

		mText = AttributeUtils.getFormattedValue(pContext, value, Constants.AttributeUnit.WATTS);
	}

	@Override
	protected void initTitle(Context pContext, AttributeData pAttributeData) {
		mTitle = pContext.getString(R.string.widget_title_ac_out);
	}

	@Override
	public boolean areRequiredAttributesAvailable(AttributeData pAttributeData) {
		if (pAttributeData.isAttributeSet(ATTRIBUTE.AC_CONSUMPTION_L1)) {
			return true;
		}
		return false;
	}
}
