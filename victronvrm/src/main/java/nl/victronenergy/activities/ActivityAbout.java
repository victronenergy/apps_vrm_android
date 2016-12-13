package nl.victronenergy.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import nl.victronenergy.BuildConfig;
import nl.victronenergy.R;

/**
 * About page that contains info about the app and links to the Victron social media pages
 * <p/>
 * Created by M2Mobi<br/>
 * 7/11/2014<br/>
 * 11:15<br/>
 */
public class ActivityAbout extends ActionBarActivity implements OnClickListener {

	/** Tag used for logging in this class */
	private static final String LOG_TAG = "ActivityAbout";

	/** Social media URLs */
	private static final String URL_FACEBOOK = "https://www.facebook.com/VictronEnergy.BV";
	private static final String URL_TWITTER = "https://twitter.com/Victron_Energy";
	private static final String URL_LINKEDIN = "http://www.linkedin.com/company/victron-energy";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		((TextView) findViewById(R.id.textview_about_version)).setText(getString(R.string.about_version, BuildConfig.VERSION_NAME));

		findViewById(R.id.about_imageview_fb).setOnClickListener(this);
		findViewById(R.id.about_imageview_twitter).setOnClickListener(this);
		findViewById(R.id.about_imageview_linkedin).setOnClickListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		String selectedUrl = null;
		switch (view.getId()) {
			case R.id.about_imageview_fb:
				selectedUrl = URL_FACEBOOK;
				break;
			case R.id.about_imageview_twitter:
				selectedUrl = URL_TWITTER;
				break;
			case R.id.about_imageview_linkedin:
				selectedUrl = URL_LINKEDIN;
				break;
		}

		// Open the selected webpage
		if (!TextUtils.isEmpty(selectedUrl)) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(selectedUrl));
			startActivity(browserIntent);
		}
	}
}
