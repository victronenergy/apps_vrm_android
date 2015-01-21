/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.models;

import com.google.gson.annotations.Expose;

public class SiteListResponse extends BaseResponse {
	@Expose
	public SiteListData data;
}
