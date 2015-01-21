/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import nl.victronenergy.models.widgets.SummaryWidget;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.DownloadStatus;
import nl.victronenergy.util.Constants.Widget;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Site contains info about a site
 *
 * @author Victron Energy
 */
public class Site implements Serializable {
	private static final String LOG_TAG = "Site";

	/**
	 * Generated serial version Id
	 */
	private static final long serialVersionUID = 8220890193598136086L;

	@Expose
	@SerializedName("idSite")
	private int mIdSite;
	@Expose
	@SerializedName("name")
	private String mName;
	@Expose
	@SerializedName("lastTimestamp")
	private int mLastTimestamp;
	@Expose
	@SerializedName("alarmStarted")
	private int mAlarmStarted;
	@Expose
	@SerializedName("hasGenerator")
	private boolean mHasGenerator;
	@Expose
	@SerializedName("hasDCSystem")
	private boolean mHasDcSystem;
	@Expose
	@SerializedName("usesVEBusSOC")
	private boolean mUsesVEBusSOC;
	@Expose
	@SerializedName("hasIOExtender")
	private boolean mHasIOExtender;
	@Expose
	@SerializedName("phonenumber")
	private String mPhonenumber;
	@Expose
	@SerializedName("activeAlarms")
	private int mActiveAlarms;
	@Expose
	@SerializedName("canEdit")
	private boolean mCanEdit;
	@Expose
	@SerializedName("mainBatteryMonitorInstance")
	private int mMainBatteryMonitorInstance;
	@Expose
	@SerializedName("accessLevel")
	private String mAccessLevel;
	/** Authentication package containing the required authentication details for pubnub */
	@Expose
	@SerializedName("authenticationPackage")
	private AuthenticationPackage mAuthenticationPackage;
	@Expose
	@SerializedName("images")
	private ArrayList<String> mImages;

	private DownloadStatus mDownloadStatus = DownloadStatus.DOWNLOAD_NEEDED;
	private int mSiteStatus = -1;

	/** List holding all available widgets for this site */
	private ArrayList<SummaryWidget> mWidgets;

	private AttributeData mAttributeData;
	private IoExtenderData mIoExtenderData;
	private int mSelectedWidgetPage;

	public Site() {
		mIoExtenderData = new IoExtenderData();
		mWidgets = new ArrayList<SummaryWidget>();
	}

	/**
	 * The id of this site
	 *
	 * @return The id of this site
	 */
	public int getIdSite() {
		return mIdSite;
	}

	/**
	 * The name of this site
	 *
	 * @return The name of this site
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Returns the time when this site was last updated in seconds
	 *
	 * @return The time when this site was last updated in seconds
	 */
	public int getLastTimeStampInSeconds() {
		return mLastTimestamp;
	}

	/**
	 * Returns the time when this site was last updated in miliseconds
	 *
	 * @return The time when this site was last updated in miliseconds
	 */
	public long getLastTimestampInMS() {
		return ((long) mLastTimestamp) * 1000;
	}

	/**
	 * Returns the time the alarm started in miliseconds
	 *
	 * @return The timestamp when the alarm started in miliseconds
	 */
	public long getAlarmStartedTimestampInMS() {
		return ((long) mAlarmStarted) * 1000;
	}

	/**
	 * Returns if this site has a DC System or not
	 *
	 * @return True if this system has a DC System, false if it doesn't
	 */
	public boolean hasDcSystem() {
		return mHasDcSystem;
	}

	/**
	 * Returns if this site uses VEBusSOC as the State of charge
	 *
	 * @return True if this site uses the VEBus State of Charge, false if it doesn't
	 */
	public boolean usesVEBusSOC() {
		return mUsesVEBusSOC;
	}

	/**
	 * Return if this site has a generator or not
	 *
	 * @return True if the site has a generator, false if not
	 */
	public boolean hasGenerator() {
		return mHasGenerator;
	}

	/**
	 * Set if this site has a generator, <b>NOTE: </b> This is only changed locally, call the hasGenerator webservice to
	 * set the hasGenerator value on the server
	 *
	 * @param hasGenerator
	 *        True if this site has a generator, false if this site doesn't have a generator
	 */
	public void setHasGenerator(boolean hasGenerator) {
		mHasGenerator = hasGenerator;
	}

	/**
	 * Returns if this site has an IO Extender connected to it
	 *
	 * @return True if the site has an IO Extender connected, false if not
	 */
	public boolean hasIOExtender() {
		return mHasIOExtender;
	}

	/**
	 * Returns the phone number of this site
	 *
	 * @return Returns the phonenumber for this site
	 */
	public String getPhonenumber() {
		return mPhonenumber;
	}

	/**
	 * Returns the amount of active alarms for this site
	 *
	 * @return The amount of active alarms for this site
	 */
	public int getActiveAlarms() {
		return mActiveAlarms;
	}

	/**
	 * Can the user edit the site
	 *
	 * @return True if the user can edit the site, false if not
	 */
	public boolean canEdit() {
		return mCanEdit;
	}

	/**
	 * Returns if changes to this site are allowed or not
	 *
	 * @return True if the user is allowed to make changes to this site and the phone number is set, false if no changes
	 *         are allowed
	 */
	public boolean areChangesAllowed() {
		if (mCanEdit && !TextUtils.isEmpty(mPhonenumber)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the currently configured main monitor instance, default 0
	 *
	 * @return The currently configured main monitor instance
	 */
	public int getMainMonitorInstance() {
		if (mMainBatteryMonitorInstance != -1) {
			return mMainBatteryMonitorInstance;
		}
		return 0;
	}

	/**
	 * Checks if the attributes of this site should be loaded
	 *
	 * @return True if the attributes should be loaded, false if not
	 */
	public boolean shouldLoadAttributes() {
		if (mDownloadStatus == DownloadStatus.DOWNLOAD_NEEDED || mDownloadStatus == DownloadStatus.DOWNLOAD_ERROR) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the attributes are done loading, this can be in two states, finished properly or with an error
	 *
	 * @return True if the attributes are loaded, false if not
	 */
	public boolean areAttributesLoaded() {
		if (mDownloadStatus == DownloadStatus.DOWNLOAD_FINISHED) {
			return true;
		}
		return false;
	}

	/**
	 * Set the attribute download status
	 *
	 * @param pAttributeDownloadStatus
	 */
	public void setAttributeDownloadStatus(DownloadStatus pAttributeDownloadStatus) {
		mDownloadStatus = pAttributeDownloadStatus;
	}

	/**
	 * Returns the status of this site, ALARM, OK or OLD
	 *
	 * @return The status of this site
	 */
	public int getSiteStatus() {
		// If no status is set yet, define the status of the site first
		if (mSiteStatus == -1) {
			Date date = new Date();
			long timeStampTwoWeeksAgo = date.getTime() - SiteListData.TWO_WEEKS_MS;

			if (getLastTimestampInMS() < timeStampTwoWeeksAgo) {
				mSiteStatus = Constants.SITE_STATUS.OLD;
			} else {
				// If there are alarms put in the alarms list else put it in the ok list
				if (getActiveAlarms() > 0) {
					mSiteStatus = Constants.SITE_STATUS.ALARM;
				} else {
					mSiteStatus = Constants.SITE_STATUS.OK;
				}
			}
		}

		return mSiteStatus;
	}

	/**
	 * Define which values should be shown in the site summary, this depends on which values are available and the
	 * priority<br/>
	 * <ul>
	 * <li>state of charge [if there is no SOC, use DC Voltage instead]</li>
	 * <li>indication if battery is being charged or discharged.</li>
	 * <li>pv power</li>
	 * <li>ac power out</li>
	 * <li>ac power in</li>
	 * <li>dc power out</li>
	 * <li>dc power in</li>
	 * </ul>
	 */
	public void defineMicroWidgets(Context pContext, AttributeData pAttributeData) {

		// Boolean indicating if AC Out is used in the list of widgets
		boolean isACOutInUse = false;

		Widget[] widgets = Widget.values();
		SummaryWidget summaryWidget;
		for (Widget widget : widgets) {
			try {
				summaryWidget = (SummaryWidget) widget.getClassWidget().newInstance();
				if (summaryWidget.areRequiredAttributesAvailable(pAttributeData)) {
					summaryWidget.initValues(pContext, pAttributeData);

					// If this widget is AC Out indicate that AC out is in use
					if (widget.equals(Widget.AC_OUT)) {
						isACOutInUse = true;
					}

					// If this is AC In and AC Out is in use, make sure to insert AC In before AC Out
					if (widget.equals(Widget.AC_IN) && isACOutInUse) {
						mWidgets.add(mWidgets.size() - 1, summaryWidget);
					} else {
						// In any other case insert it in the default order
						mWidgets.add(summaryWidget);
					}
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the available widgets for this site
	 *
	 * @return An array of widgets that are available for this site
	 */
	public ArrayList<SummaryWidget> getWidgets() {
		return mWidgets;
	}

	/**
	 * Returns the attribute data
	 *
	 * @return The attribute data for this site
	 */
	public AttributeData getAttributeData() {
		return mAttributeData;
	}

	/**
	 * Returns the IO Extender data
	 *
	 * @return The ioExtender data
	 */
	public IoExtenderData getIoExtenderData() {
		return mIoExtenderData;
	}

	/**
	 * Set the attribute data for this site
	 *
	 * @param pAttributeData
	 */
	public void setAttributeData(Context pContext, AttributeData pAttributeData) {
		mAttributeData = pAttributeData;

		// First define the widgets for the sitelist
		defineMicroWidgets(pContext, pAttributeData);

		// Filter the IoExtender objects
		if (mHasIOExtender) {
			parseIoExtenderData(pAttributeData);
		}

		setAttributeDownloadStatus(DownloadStatus.DOWNLOAD_FINISHED);
	}

	/**
	 * Parses the attribute data to see if there are any IO's available
	 *
	 * @param pAttributeData
	 */
	private void parseIoExtenderData(AttributeData pAttributeData) {
		mIoExtenderData.clear();

		for (Attribute attribute : pAttributeData.getAttributes()) {
			if (attribute.attributeCode.contains(Constants.OUTPUT_CODE_PREFIX) || attribute.attributeCode.contains(Constants.INPUT_CODE_PREFIX)
					|| attribute.attributeCode.equals(Constants.ATTRIBUTE.IO_TEMPERATURE)) {
				mIoExtenderData.addIoAttribute(attribute);
			}
		}
	}

	/**
	 * Checks if this site supports pubnub by checking if the authenticationpackage is available
	 *
	 * @return True if this site supports pubnub, false if not
	 */
	public boolean hasPubnub() {
		if (mAuthenticationPackage == null) {
			return false;
		}
		return true;
	}

	/**
	 * Sets which widget page is currently selected
	 *
	 * @param pPosition
	 *        The index of the currently selected page
	 */
	public void setSelectedWidgetPage(int pPosition) {
		mSelectedWidgetPage = pPosition;
	}

	/**
	 * Returns the index of the currently selected widget page
	 *
	 * @return Index of the currently selected widget page
	 */
	public int getSelectedWidgetPage() {
		return mSelectedWidgetPage;
	}

	/**
	 * Returns The authenticationPackage for this site.
	 *
	 * @return The authenticationPackage for this site or null if no authenticationPackage is available
	 */
	public AuthenticationPackage getAuthenticationPackage() {
		return mAuthenticationPackage;
	}

	public ArrayList<String> getImages() {
		return mImages;
	}
}
