package nl.victronenergy.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Data object with user info
 *
 * @author M2Mobi
 */
public class UserData {
	@Expose
	public User user;

	/**
	 * User class contains the session id and an UserInfo object
	 *
	 * @author M2Mobi
	 */
	public class User {
		@Expose
		@SerializedName("sessionid")
		public String sessionId;
		@Expose
		@SerializedName("user_info")
		public UserInfo userInfo;
	}

	/**
	 * UserInfo class contains info about the user
	 *
	 * @author M2Mobi
	 */
	public class UserInfo {
		@Expose
		public int idUser;
		@Expose
		public String email;
		@Expose
		@SerializedName("firstname")
		public String firstName;
		@Expose
		@SerializedName("lastname")
		public String lastName;
		@Expose
		public String company;
		@Expose
		public String city;
		@Expose
		public String country;
		@Expose
		public String phone;
		@Expose
		@SerializedName("mobile_phone")
		public String mobilePhone;
		@Expose
		public String dealer;
	}
}
