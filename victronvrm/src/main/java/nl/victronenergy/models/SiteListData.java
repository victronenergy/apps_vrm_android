package nl.victronenergy.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.annotations.Expose;

/**
 * Array of sites returned by the webservice
 *
 * @author M2Mobi
 */
public class SiteListData implements Serializable {
	/**
	 * Generated serial version Id
	 */
	private static final long serialVersionUID = -667470541327539200L;

	@Expose
	public Site[] sites;

	/* Data is old when it's older than two weeks */
	public static final int TWO_WEEKS_MS = 1209600000;

	/**
	 * Group the sites according to the grouping rules: Sites with alarm, Sites ok, Sites not sending data
	 */
	public void orderSitesByStatus() {
		ArrayList<Site> sitesAlarm = new ArrayList<Site>();
		ArrayList<Site> sitesOk = new ArrayList<Site>();
		ArrayList<Site> sitesOld = new ArrayList<Site>();

		// Check the status of the site
		Date date = new Date();
		long timeStampTwoWeeksAgo = date.getTime() - TWO_WEEKS_MS;

		for (Site site : sites) {
			// If the site data is old put the site in the old data list
			if (site.getLastTimestampInMS() < timeStampTwoWeeksAgo) {
				sitesOld.add(site);
			} else {
				// If there are alarms put in the alarms list else put it in the ok list
				if (site.getActiveAlarms() > 0) {
					sitesAlarm.add(site);
				} else {
					sitesOk.add(site);
				}
			}
		}

		sites = new Site[sitesAlarm.size() + sitesOk.size() + sitesOld.size()];
		int count = 0;
		for (Site site : sitesAlarm) {
			sites[count] = site;
			count++;
		}

		for (Site site : sitesOk) {
			sites[count] = site;
			count++;
		}

		for (Site site : sitesOld) {
			sites[count] = site;
			count++;
		}
	}

	/**
	 * @param pSiteId
	 *        The id of the site you want the index for
	 * @return The index of the site if found, if not found returns -1
	 */
	public int getSiteIndexForSiteId(int pSiteId) {
		for (int i = 0; i < sites.length; i++) {
			if (sites[i].getIdSite() == pSiteId) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @param pSiteId
	 *        The id of the site you want the index for
	 * @return The site that has the site id, if not found returns null
	 */
	public Site getSiteById(int pSiteId) {
		for (int i = 0; i < sites.length; i++) {
			if (sites[i].getIdSite() == pSiteId) {
				return sites[i];
			}
		}
		return null;
	}
}
