/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.models;

import com.google.gson.annotations.Expose;

/**
 * Contains an array of data attributes
 *
 * @author Victron Energy
 */
public class AttributesResponse extends BaseResponse {
    @Expose
	public AttributeData data;
}
