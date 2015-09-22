/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.adapters;

import nl.victronenergy.fragments.FragmentSiteDetail;
import nl.victronenergy.models.Site;
import nl.victronenergy.models.SiteListData;
import nl.victronenergy.util.Constants.BUNDLE;

import android.os.Bundle;
import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * Viewpager that contains fragments for all the sites
 *
 * @author Victron Energy
 */
public class SiteFragmentStatePagerAdapter extends FixedFragmentStatePagerAdapter {
	private Site[] mSites;

	public SiteFragmentStatePagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		FragmentSiteDetail fragment = new FragmentSiteDetail();
		Bundle b = new Bundle();
		b.putSerializable(BUNDLE.SITE_OBJECT, mSites[position]);
		b.putInt(BUNDLE.SITE_ARRAY_INDEX, position);
		fragment.setArguments(b);

		return fragment;
	}

	@Override
	public int getCount() {
		if (mSites != null) {
			return mSites.length;
		}
		return 0;
	}

	/**
	 * Set the sites and notify that the data set has changed
	 *
	 * @param pSiteData
	 *        The new site data
	 */
	public void setSiteData(SiteListData pSiteData) {
		if (pSiteData != null) {
			mSites = pSiteData.sites;
			notifyDataSetChanged();
		}
	}
}
