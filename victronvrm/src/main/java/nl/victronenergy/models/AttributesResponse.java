package nl.victronenergy.models;

import com.google.gson.annotations.Expose;

/**
 * Contains an array of data attributes
 *
 * @author M2Mobi
 */
public class AttributesResponse extends BaseResponse {
    @Expose
	public AttributeData data;
}
