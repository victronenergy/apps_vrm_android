/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.models;

import java.io.Serializable;

import nl.victronenergy.util.Constants;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Attribute object
 *
 * @author Victron Energy
 */
public class Attribute implements Serializable {
	// Generated serial version Id
	private static final long serialVersionUID = 3947960759889933265L;

	@Expose
	@SerializedName("timestamp")
	private long mTimestamp;
	@Expose
	@SerializedName("idDataAttribute")
	public int attributeId;
	@Expose
	@SerializedName("code")
	public String attributeCode;
	@Expose
	@SerializedName("isValid")
	private boolean mIsValid;
	@Expose
	@SerializedName("valueFloat")
	private float mValueFloat;
	@Expose
	@SerializedName("valueString")
	private String mValueString;
	@Expose
	@SerializedName("valueEnum")
	private int mValueEnum;
	@Expose
	@SerializedName("nameEnum")
	private String mNameEnum;
	@Expose
	@SerializedName("formatValueOnly")
	private String mFormatValueOnly;
	@Expose
	@SerializedName("formatWithUnit")
	private String mFormatWithUnit;
	@Expose
	@SerializedName("dataType")
	private String mDataType;
	@Expose
	@SerializedName("customLabel")
	private String mCustomLabel;

	/**
	 * Returns the string value of this data attribute, can be null
	 *
	 * @return The string value of this data attribute, <b>can be null</b>
	 */
	public String getValue() {
		return mValueString;
	}

	/**
	 * Returns the float value of this data attribute
	 *
	 * @return The float value of this data attribute, 0.0f if it's not set
	 */
	public float getFloatValue() {
		return mValueFloat;
	}

	/**
	 * Returns the enum value
	 *
	 * @return
	 */
	public int getValueEnum() {
		return mValueEnum;
	}

	/**
	 * Returns the label that the user has given to this attribute
	 *
	 * @return The label that the user has given to this attribute
	 */
	public String getLabel() {
		return mCustomLabel;
	}

	/**
	 * <b>This function should only be used for IO Extender attributes</b><br/>
	 * Returns if this ioObject is open or closed
	 *
	 * @return Returns true if the status of this ioObject is open, false if it's closed
	 */
	public boolean isOpen() {
		// If the status is true, the status is open.
		// If the status is false, the status is closed.
		// For outputs this is reverse
		if (attributeCode.contains(Constants.OUTPUT_CODE_PREFIX)) {
			return !(mValueEnum == 1);
		} else {
			return (mValueEnum == 1);
		}
	}

	/**
	 * <b>This function should only be used for IO Extender attributes</b><br/>
	 * Set the status of the IO Attribute
	 *
	 * @param status
	 *        The new status of the IO Attribute
	 */
	public void setStatus(boolean status) {
		if (status) {
			mValueEnum = 1;
		} else {
			mValueEnum = 0;
		}
	}

	/**
	 * Set the float value of this data attribute
	 *
	 * @param pFloatValue
	 *        The float value to set
	 */
	public void setFloatValue(float pFloatValue) {
		mValueFloat = pFloatValue;
	}
}
