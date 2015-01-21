/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HistoricData {
	@Expose
	@SerializedName("ts")
	public int timestamp;
    @Expose
	@SerializedName("dd")
	public String deepestDischarge;
	@Expose
	@SerializedName("ld")
	public String lastDischarge;
	@Expose
	@SerializedName("ad")
	public String averageDischarge;
	@Expose
	@SerializedName("cc")
	public String chargeCycles;
	@Expose
	@SerializedName("fd")
	public String fullDischarge;
	@Expose
	@SerializedName("tad")
	public String totalAhDrawn;
	@Expose
	@SerializedName("minv")
	public String minimumVoltage;
	@Expose
	@SerializedName("maxv")
	public String maximumVoltage;
	@Expose
	@SerializedName("tslc")
	public String timeSinceLastFullCharge;
	@Expose
	@SerializedName("as")
	public String automaticSyncs;
	@Expose
	@SerializedName("lva")
	public String lowVoltageAlarms;
	@Expose
	@SerializedName("hva")
	public String highVoltageAlarms;
	@Expose
	@SerializedName("lsva")
	public String lowStarterVoltageAlarms;
	@Expose
	@SerializedName("hsva")
	public String highStarterVoltageAlarms;
	@Expose
	@SerializedName("minsv")
	public String minimumStarterVoltage;
	@Expose
	@SerializedName("maxsv")
	public String maximumStarterVoltage;
}
