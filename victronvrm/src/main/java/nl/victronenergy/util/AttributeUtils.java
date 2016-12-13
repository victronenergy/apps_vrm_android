package nl.victronenergy.util;

import nl.victronenergy.R;
import nl.victronenergy.models.Attribute;
import nl.victronenergy.util.Constants.AttributeUnit;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

/**
 * Contains a couple of functions to help formatting the data attributes
 *
 * @author M2Mobi
 */
public final class AttributeUtils {
	private static final String LOG_TAG = "AttributeUtils";

	/**
	 * Set the data attribute value in a textview
	 *
	 * @param pContext
	 *        The context needed to load the String resources
	 * @param pTextView
	 *        The textview to show the value in
	 * @param pAttribute
	 *        The attribute we want to show the value of
	 * @param pAttributeUnit
	 *        The unit we would like to show the value in
	 * @param hideIfAttributeNull
	 *        Hide the textview if the attribute is null
	 */
	public static void setDataInTextView(Context pContext,
			TextView pTextView,
			Attribute pAttribute,
			AttributeUnit pAttributeUnit,
			boolean hideIfAttributeNull) {
		if (pTextView != null) {
			if (pAttribute == null) {
				if (hideIfAttributeNull) {
					pTextView.setVisibility(View.GONE);
				} else {
					pTextView.setText(getNotAvailableString(pContext, pAttributeUnit));
				}
			} else {
				pTextView.setText(getFormattedValue(pContext, pAttribute.getFloatValue(), pAttributeUnit));
			}

			pTextView.setTextColor(pContext.getResources().getColor(getColorForAttributeUnit(pAttributeUnit)));
		}
	}

	/**
	 * Returns the color the value should be displayed in
	 *
	 * @param pAttributeUnit
	 *        The unit of this attribute
	 * @return The color of the attribute
	 */
	public static int getColorForAttributeUnit(AttributeUnit pAttributeUnit) {
		switch (pAttributeUnit) {
			case AMPHOUR:
				return R.color.orange;
			case VOLT:
				return R.color.blue;
			case AMPS:
				return R.color.red;
			default:
				return R.color.black;
		}
	}

	/**
	 * Returns the not available string for the requested type
	 *
	 * @param pContext
	 *        The context needed to load the String resource
	 * @param pAttributeUnit
	 *        The type we want the string to be in
	 * @return The not available string for the requested type
	 */
	public static String getNotAvailableString(Context pContext, AttributeUnit pAttributeUnit) {
		switch (pAttributeUnit) {
			case AMPHOUR:
				return pContext.getString(R.string.not_available_ah);
			case AMPS:
				return pContext.getString(R.string.not_available_a);
			case PERCENTAGE:
				return pContext.getString(R.string.not_available_percentage);
			case TIME:
				return pContext.getString(R.string.not_available_time);
			case VOLT:
				return pContext.getString(R.string.not_available_v);
			case WATTS:
				return pContext.getString(R.string.not_available_w);
			case COUNT:
				return pContext.getString(R.string.not_available);
			default:
				return pContext.getString(R.string.not_available);
		}
	}

	/**
	 * Returns the formatted string of a float
	 *
	 * @param pContext
	 *        The context needed to load the format String resource
	 * @param pValue
	 *        The value to format
	 * @param pAttributeUnit
	 *        The unit to format the value in
	 * @return Formatted value
	 */
	public static String getFormattedValue(Context pContext, float pValue, AttributeUnit pAttributeUnit) {
		switch (pAttributeUnit) {
			case WATTS:
				return formatWatts(pContext, pValue);
			case VOLT:
				return formatVolt(pContext, pValue);
			case TIME:
				return formatTime(pContext, pValue);
			case AMPS:
				return formatAmps(pContext, pValue);
			case AMPHOUR:
				return formatAmpHour(pContext, pValue);
			case PERCENTAGE:
				return formatPercentage(pContext, pValue);
			case COUNT:
				return formatCount(pValue);
			default:
				return pContext.getString(R.string.not_available);
		}
	}

	/**
	 * Formats a count value
	 *
	 * @param pValue
	 *        The attribute we want formatted
	 * @return Value formatted as a count
	 */
	private static String formatCount(float pValue) {
		return String.valueOf((int) pValue);
	}

	/**
	 * Formats an amps value
	 *
	 * @param pContext
	 *        Context needed to retrieve the amps format string
	 * @param pValue
	 *        The attribute we want formatted
	 * @return Value formatted as amps
	 */
	private static String formatAmps(Context pContext, float pValue) {
		return pContext.getString(R.string.formatted_value_a, Math.abs(pValue));
	}

	/**
	 * Formats a AmpHour value
	 *
	 * @param pContext
	 *        Context needed to retrieve the AmpHour format string
	 * @param pValue
	 *        The attribute we want formatted
	 * @return Value formatted as AmpHour
	 */
	private static String formatAmpHour(Context pContext, float pValue) {
		return pContext.getString(R.string.formatted_value_ah, pValue);
	}

	/**
	 * Formats a percentage value
	 *
	 * @param pContext
	 *        Context needed to retrieve the percentage format string
	 * @param pValue
	 *        The attribute we want formatted
	 * @return Value formatted as percentage
	 */
	private static String formatPercentage(Context pContext, float pValue) {
		if (pValue == 0.0f) {
			return pContext.getString(R.string.not_available_percentage);
		}
		return pContext.getString(R.string.formatted_value_percentage, pValue);
	}

	/**
	 * Formats a Watts value
	 *
	 * @param pContext
	 *        Context needed to retrieve the Watts/kWatts format string
	 * @param pValue
	 *        The attribute we want formatted
	 * @return Value formatted as Watts or kWatts depending on the value
	 */
	private static String formatWatts(Context pContext, float pValue) {
		float value = Math.abs(pValue);
		if (value < 10000) {
			return pContext.getString(R.string.formatted_value_w, value);
		} else {
			// Divide by 1000 to get kW
			value /= 1000;
			return pContext.getString(R.string.formatted_value_kw, value);
		}
	}

	/**
	 * Formats a volt value
	 *
	 * @param pContext
	 *        Context needed to retrieve the volt format string
	 * @param pValue
	 *        The attribute we want formatted
	 * @return Value formatted as volt
	 */
	private static String formatVolt(Context pContext, float pValue) {
		return pContext.getString(R.string.formatted_value_v, pValue);
	}

	/**
	 * The webservice returns a float (for example 23.041) where 23 would be the amount of hours and 041 the amount of
	 * seconds. In order to display it according to business rules, it needs to be formatted as hours to go and minutes
	 * to go (as xxh xxm). If no time is remaining it should display --h --m.
	 *
	 * @param pContext
	 *        The context needed to load string resources
	 * @return The time to go formatted as xxh xxm
	 */
	private static String formatTime(Context pContext, float pValue) {
		// Amount of hours
		int hours = (int) Math.floor(pValue);

		// Amount of seconds multiplied by 60 to get the amount of minutes
		int minutes = Math.round((pValue - hours) * 60);

		// Don't show the time if there is no time left
		if (hours == 0 && minutes == 0) {
			return pContext.getString(R.string.not_available_time);
		}
		return pContext.getString(R.string.formatted_value_time, hours, minutes);
	}
}
