/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.models;

import com.google.gson.annotations.Expose;

/**
 * User response of the Victron VRM webservice
 *
 * @author Victron Energy
 */
public class UserResponse extends BaseResponse {
	@Expose
	public UserData data;
}
