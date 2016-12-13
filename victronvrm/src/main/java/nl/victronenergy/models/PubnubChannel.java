package nl.victronenergy.models;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Contains the cipher key required to encrypt data on this channel and the name of the channel
 * <p/>
 * User: M2Mobi<br/>
 * Date: 9-5-2014<br/>
 * Time: 13:37<br/>
 */
public class PubnubChannel implements Serializable {

	/** Generated serialVersionUID */
	private static final long serialVersionUID = 1118463675085104483L;

	/** The cipher key used for AES encryption on the channel */
	@Expose
	@SerializedName("cipherkey")
	private final String mCipherKey;

	/** The name of the channel */
	@Expose
	@SerializedName("channel")
	private final String mChannel;

	public PubnubChannel(final String pCipherKey, final String pChannel) {
		mCipherKey = pCipherKey;
		mChannel = pChannel;
	}

	public String getCipherKey() {
		return mCipherKey;
	}

	public String getChannelName() {
		return mChannel;
	}
}
