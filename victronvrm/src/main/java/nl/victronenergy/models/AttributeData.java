package nl.victronenergy.models;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Class that contains an array of attributes
 *
 * @author M2Mobi
 */
public class AttributeData implements Serializable {
	// Generated Serial version id
	private static final long serialVersionUID = 180973629201171140L;

	@Expose
	@SerializedName("attributes")
	private Attribute[] mAttributes;

	/**
	 * Checks if an attribute is set
	 *
	 * @param attributeCode
	 *        The attribute id you want to check
	 * @return True if the attribute is set, false if it's not set
	 */
	public boolean isAttributeSet(String attributeCode) {
		if (mAttributes == null) {
			return false;
		}

		for (Attribute attribute : mAttributes) {
			if (attribute.attributeCode.equals(attributeCode)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the attribute that has the requested id
	 *
	 * @param attributeCode
	 *        The id of the attribute you want
	 * @return The attribute if it's found, returns null if it can't be found
	 */
	public Attribute getAttribute(String attributeCode) {
		if (mAttributes == null) {
			return null;
		}

		for (Attribute attribute : mAttributes) {
			if (attribute.attributeCode.equals(attributeCode)) {
				return attribute;
			}
		}
		return null;
	}

	/**
	 * Returns the sum of multiple attributes requested in the array of attributeCodes
	 *
	 * @param attributeCodes
	 *        The attributeCodes you want the sum of
	 * @return Float value of the sum of attribute values for the requested ids
	 */
	public float getCombinedValueForAttributes(String[] attributeCodes) {
		float totalValue = 0.0f;

		Attribute attribute;
		for (String dataAttributeCode : attributeCodes) {
			attribute = getAttribute(dataAttributeCode);
			if (attribute != null) {
				totalValue += attribute.getFloatValue();
			}
		}
		return totalValue;
	}

	/**
	 * Returns an array of attributes
	 *
	 * @return An array of attributes
	 */
	public Attribute[] getAttributes() {
		return mAttributes;
	}

	/**
	 * Update the current attribute data to the received live pubnub data<br/>
	 * This is done by comparing the received dataAttributes with the current data attributes
	 *
	 * @param pPubnubStatusUpdate
	 *        Pubnub status update containing an array of up to date data attributes
	 */
	public void updateAttributesWithPubnubData(PubnubStatusUpdate pPubnubStatusUpdate) {
		for (PubnubDataUpdate dataUpdate : pPubnubStatusUpdate.mDataUpdate) {
			if (dataUpdate.isValueValid()) {
				Attribute attribute = getAttribute(dataUpdate.getCode());
				if (attribute != null) {
					attribute.setFloatValue(dataUpdate.getFloatValue());
				}
			}
		}
	}
}
