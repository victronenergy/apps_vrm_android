/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.models;

import nl.victronenergy.util.MyLog;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a pubnub data update object. This contains the new value of an data attribute<br/>
 * <br/>
 * Created by Victron Energy on 9-4-14.
 */
public class PubnubDataUpdate {

	/** Tag used for logging in this class */
	private static final String LOG_TAG = "PubnubDataUpdate";

	@SerializedName("code")
	/** The data attribute code of this data update */
	private final String mCode;

	@SerializedName("instance")
	/** The battery monitor instance this attribute belongs to */
	private final String mInstance;

	@SerializedName("value")
	/** The value of the attribute */
	private final String mValue;

	public PubnubDataUpdate(final String pCode, final String pInstance, final String pValue) {
		mCode = pCode;
		mInstance = pInstance;
		mValue = pValue;
	}

	public String getCode() {
		return mCode;
	}

	public String getInstance() {
		return mInstance;
	}

	public String getStringValue() {
		return mValue;
	}

	/**
	 * Tries to parse the returned value as a float, if the value is not a float it returns 0.0f<br/>
	 *
	 * @return The value as a float or 0.0f when parsing failed
	 */
	public float getFloatValue() {
		try {
			return Float.valueOf(mValue);
		} catch (NumberFormatException nfe) {
			MyLog.e(LOG_TAG, "Unable to parse float: " + mValue);
		}
		return 0.0f;
	}

	/**
	 * Checks if the String value contains a valid float value
	 *
	 * @return True if the string contains a valid float value, false if it doesn't
	 */
	public boolean isValueValid() {
		if (!TextUtils.isEmpty(mValue)) {
			try {
				Float.valueOf(mValue);
				return true;
			} catch (NumberFormatException nfe) {
				MyLog.e(LOG_TAG, "Unable to parse float: " + mValue);
			}
		}
		return false;
	}
}
