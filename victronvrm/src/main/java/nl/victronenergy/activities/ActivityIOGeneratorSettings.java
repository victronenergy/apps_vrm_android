package nl.victronenergy.activities;

import nl.victronenergy.R;
import nl.victronenergy.fragments.FragmentIOGeneratorSettings;
import nl.victronenergy.util.AnalyticsConstants;
import nl.victronenergy.util.Constants;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

/**
 * Activity that shows the generator settings fragment<br/>
 * Created by M2Mobi on 5-3-14.
 */
public class ActivityIOGeneratorSettings extends ActionBarActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_historic_data);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Setup the fragment
		Fragment fragmentGeneratorSettings = null;

		// Try to restore the fragment from the savedInstance
		if (savedInstanceState != null) {
			fragmentGeneratorSettings = getSupportFragmentManager()
					.getFragment(savedInstanceState, Constants.FRAGMENT_TAG.SITE_IO_GENERATOR_SETTINGS);
		}

		// Fragment is null, create a new fragment
		if (fragmentGeneratorSettings == null) {
			fragmentGeneratorSettings = new FragmentIOGeneratorSettings();
			fragmentGeneratorSettings.setArguments(getIntent().getExtras());
		}

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame_historic, fragmentGeneratorSettings, Constants.FRAGMENT_TAG.SITE_IO_GENERATOR_SETTINGS).commit();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the generator settings fragment to the saved instance state
		Fragment fragmentGeneratorSettings = getSupportFragmentManager().findFragmentByTag(Constants.FRAGMENT_TAG.SITE_IO_GENERATOR_SETTINGS);
		if (fragmentGeneratorSettings != null) {
			getSupportFragmentManager().putFragment(outState, Constants.FRAGMENT_TAG.SITE_IO_GENERATOR_SETTINGS, fragmentGeneratorSettings);
		}
	}
}
