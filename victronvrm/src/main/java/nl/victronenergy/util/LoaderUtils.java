/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.util;

import nl.victronenergy.util.Constants.LOADER_ID;

/**
 * Contains helper functions for the loader id
 *
 * @author Victron Energy
 */
public class LoaderUtils {

	/**
	 * Returns loaderID
	 *
	 * @param loaderType
	 *        The actual loader type
	 * @param siteId
	 *        The pageId of the viewpager used to create a unique loaderId
	 * @return A unique loaderId, created by combining the loaderType and viewPager pageIndex, usefull for loaders in
	 *         viewpagers
	 */
	public static int getUniqueLoaderId(int loaderType, int siteId) {
		return loaderType + siteId;
	}

	/**
	 * Returns the loaderId from the unique loader id, this undo's what the getUniqueLoaderId function does.
	 *
	 * @param loaderId
	 *        The loaderId returned by the webservice
	 * @return The original loaderId that was originaly used
	 */
	public static int getLoaderIdFromUniqueLoaderId(int loaderId) {
		return loaderId & LOADER_ID.FILTER;
	}
}
