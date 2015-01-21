/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.models;

import java.io.Serializable;
import java.util.ArrayList;

import nl.victronenergy.util.Constants;
import nl.victronenergy.util.IoExtenderUtils;
import android.content.Context;
import android.text.TextUtils;

/**
 * IOExtender data object contains an array of IO objects that are connected to the IO Extender
 *
 * @author Victron Energy
 */
public class IoExtenderData implements Serializable {
	private ArrayList<Attribute> mIoAttributes = new ArrayList<Attribute>();

	/**
	 * Returns the Attribute IOExtender at a certain index
	 *
	 * @param index
	 *        The index of the Attribute IO Extender you want
	 * @return An Attribute object if it's found else NULL
	 */
	public Attribute getIoExtenderAtIndex(int index) {
		if (index < mIoAttributes.size()) {
			return mIoAttributes.get(index);
		}

		return null;
	}

	/**
	 * Returns the amount of connected IO Objects
	 *
	 * @return The amount of connected IO Objects
	 */
	public int getIoExtenderCount() {
		if (mIoAttributes != null) {
			return mIoAttributes.size();
		}

		return 0;
	}

	/**
	 * Return the label of an IO for an IO Code
	 *
	 * @param ioCode
	 *        The ioCode of the IO you are looking for
	 * @return The label of an IO if found, null if not found
	 */
	public String getIOLabelForCode(String ioCode) {
		for (Attribute ioAttribute : mIoAttributes) {
			if (ioAttribute.attributeCode.equals(ioCode)) {
				return ioAttribute.getLabel();
			}
		}

		return null;
	}

	/**
	 * Returns a list of outputs that are available on this IOExtender
	 *
	 * @return An ArrayList of outputs available on this IOExtender
	 */
	public ArrayList<Attribute> getOutputs() {
		ArrayList<Attribute> outputs = new ArrayList<Attribute>();

		for (Attribute ioAttribute : mIoAttributes) {
			if (ioAttribute.attributeCode.contains(Constants.OUTPUT_CODE_PREFIX)) {
				outputs.add(ioAttribute);
			}
		}

		return outputs;
	}

	/**
	 * Returns the ioObject where the generator is connected to
	 *
	 * @param pContext
	 *        the context needed to retrieve the generator settings
	 * @param pSiteId
	 *        The id of the site this ioExtender belongs to
	 * @return The ioExtenderObject the generator is connected to. <b>null</b> when there is no generator connected or
	 *         the settings are not set yet
	 */
	public Attribute getGeneratorOutputObject(Context pContext, int pSiteId) {
		String ioCode = IoExtenderUtils.getGeneratorIoCode(pContext, pSiteId);

		// If no ioCode is set, the settings are apparently not set yet so return null
		if (TextUtils.isEmpty(ioCode)) {
			return null;
		}

		for (Attribute ioAttribute : mIoAttributes) {
			if (ioAttribute.attributeCode.equals(ioCode)) {
				return ioAttribute;
			}
		}
		return null;
	}

	/**
	 * Add an IO Attribute to the attributes
	 *
	 * @param pIoAttribute
	 *        The attribute to add as an IO Attributes
	 */
	public void addIoAttribute(Attribute pIoAttribute) {
		mIoAttributes.add((pIoAttribute));
	}

	/**
	 * Clear all IoAttributes from this IO Extender
	 */
	public void clear() {
		mIoAttributes.clear();
	}
}
