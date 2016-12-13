package nl.victronenergy.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HistoricDataResponse extends BaseResponse {
	@Expose
	@SerializedName("hd")
	public HistoricData historicData;
}
