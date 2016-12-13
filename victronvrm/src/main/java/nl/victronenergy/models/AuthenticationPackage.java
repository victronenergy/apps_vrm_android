package nl.victronenergy.models;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * AuthenticationPackage contains the required keys to subscribe to pubnub
 * <p/>
 * User: M2Mobi<br/>
 * Date: 9-5-2014<br/>
 * Time: 13:35<br/>
 */
public class AuthenticationPackage implements Serializable {

	/** Generated serialVersionUID */
	private static final long serialVersionUID = 680735615597398959L;

	/** Channel used to receive data updates */
	@Expose
	@SerializedName("livefeed")
	private final PubnubChannel mChannelLiveFeed;

	/** Channel used to send data, in our case the keep alive */
	@Expose
	@SerializedName("reader")
	private final PubnubChannel mChannelReader;

	public AuthenticationPackage(final PubnubChannel pChannelLiveFeed, final PubnubChannel pChannelReader) {
		mChannelLiveFeed = pChannelLiveFeed;
		mChannelReader = pChannelReader;
	}

	public PubnubChannel getLiveFeedChannel() {
		return mChannelLiveFeed;
	}

	public PubnubChannel getReaderChannel() {
		return mChannelReader;
	}
}
