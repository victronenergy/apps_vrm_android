package nl.victronenergy.util.webservice;

/**
 * Class that contains info that for returned by a webservice call. This will be the status code and the actual data. If
 * everything went alright the data should be JSON
 * 
 * @author M2Mobi
 */
public class RestResponse {
	private String mData = null;
	private int mStatusCode = -1;

	public RestResponse() {
	}

	public RestResponse(String data, int code) {
		mData = data;
		mStatusCode = code;
	}

	/**
	 * Returns the data object
	 * 
	 * @return The data object
	 */
	public String getData() {
		return mData;
	}

	/**
	 * Returns the status code returned by the webservice
	 * 
	 * @return The status code that was returned by the webservice
	 */
	public int getStatusCode() {
		return mStatusCode;
	}
}
