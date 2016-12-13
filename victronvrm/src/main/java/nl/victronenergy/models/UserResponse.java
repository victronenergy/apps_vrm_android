package nl.victronenergy.models;

import com.google.gson.annotations.Expose;

/**
 * User response of the Victron VRM webservice
 *
 * @author M2Mobi
 */
public class UserResponse extends BaseResponse {
	@Expose
	public UserData data;
}
