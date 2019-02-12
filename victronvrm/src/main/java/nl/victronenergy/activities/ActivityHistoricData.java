package nl.victronenergy.activities;

import nl.victronenergy.fragments.FragmentHistoricData;
import nl.victronenergy.R;
import nl.victronenergy.util.AnalyticsConstants;
import nl.victronenergy.util.Constants;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;


/**
 * Activity that shows the historic data fragment<br/>
 * Created by M2Mobi on 5-3-14.
 */
public class ActivityHistoricData extends ActionBarActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_historic_data);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Setup the fragment
		Fragment fragmentHistoricData = null;

		// Try to restore the fragment from the savedInstance
		if (savedInstanceState != null) {
			fragmentHistoricData = getSupportFragmentManager().getFragment(savedInstanceState, Constants.FRAGMENT_TAG.SITE_HISTORIC_DATA);
		}

		// Fragment is null, create a new fragment
		if (fragmentHistoricData == null) {
			fragmentHistoricData = new FragmentHistoricData();
			if (getIntent().hasExtra(Constants.BUNDLE.SITE_OBJECT)) {
				Bundle bundleArguments = new Bundle();
				bundleArguments.putSerializable(Constants.BUNDLE.SITE_OBJECT, getIntent().getSerializableExtra(Constants.BUNDLE.SITE_OBJECT));
				fragmentHistoricData.setArguments(bundleArguments);
			}
		}

		getSupportFragmentManager().beginTransaction().replace(R.id.frame_historic, fragmentHistoricData, Constants.FRAGMENT_TAG.SITE_HISTORIC_DATA)
				.commit();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the historic data fragment to the saved instance state
		Fragment fragmentHistoricData = getSupportFragmentManager().findFragmentByTag(Constants.FRAGMENT_TAG.SITE_HISTORIC_DATA);
		if (fragmentHistoricData != null) {
			getSupportFragmentManager().putFragment(outState, Constants.FRAGMENT_TAG.SITE_HISTORIC_DATA, fragmentHistoricData);
		}
	}

	/**
	 * Checks which action bar button is pressed
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
