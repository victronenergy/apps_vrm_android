package nl.victronenergy.models;

import com.google.gson.annotations.SerializedName;

/**
 * Pubnub status update containing all updated data attributes for a site<br/>
 * <br/>
 * Created by M2Mobi on 9-4-14.
 */
public class PubnubStatusUpdate {

	@SerializedName("pID")
	/** The id of the channel that published this data update */
	public String mPublishId;

	@SerializedName("t")
	/** Timestamp of the data update */
	public long mTimestamp;

	@SerializedName("announceMessage")
	/** Announcement message might be used to send messages, but unused for now */
	public String mAnnounceMessage;

	@SerializedName("dataUpdate")
	/** Array of data update objects. Containing the attribute data */
	public PubnubDataUpdate[] mDataUpdate;
}
