package nl.victronenergy.activities;

import nl.victronenergy.R;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.WEBAPP;
import nl.victronenergy.util.MyLog;
import nl.victronenergy.util.UserUtils;
import org.apache.http.util.EncodingUtils;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.google.analytics.tracking.android.EasyTracker;

/**
 * Login to the VRM and then show the VRM webapp<br/>
 * There are 3 different pages to show<br/>
 * - Default site overview<br/>
 * - Site settings<br/>
 * - Site alarm settings<br/>
 *
 * @author M2Mobi
 */
public class ActivityWebsite extends ActionBarActivity {
	private static final String LOG_TAG = "ActivityWebsite";
	private WebView wv;
	private String mSiteUrl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_website);

		mSiteUrl = getIntent().getStringExtra(Constants.INTENT_SITE_URL);

		initView();
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance().setContext(this);
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance().setContext(this);
		EasyTracker.getInstance().activityStop(this);
	}

	/** Initialize the view */
	private void initView() {
		// Hide actionbar title
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Clear cookies
		CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();

		// Login
		wv = (WebView) findViewById(R.id.wv_webapp);
		wv.setWebViewClient(webViewClient);

		// wv.setWebChromeClient(new WebChromeClient());
		wv.clearCache(false);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		wv.getSettings().setBuiltInZoomControls(true);
		wv.getSettings().setSupportZoom(true);

		String userName = UserUtils.getUsername(this);
		String password = UserUtils.getPassword(this);
		// If there is no username, we are logged in as the demo user
		if (TextUtils.isEmpty(userName)) {
			userName = Constants.DEMO_EMAIL;
			password = Constants.DEMO_PASSWORD;
		}
		String query = "username=" + userName + "&password=" + password;
		wv.postUrl(mSiteUrl, EncodingUtils.getBytes(query, "UTF-8"));
	}

	/** Checks which action bar button is pressed */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (wv.canGoBack()) {
				wv.goBack();
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Custom webview client that takes care of the automatic login and parsing the mailto url
	 */
	private final WebViewClient webViewClient = new WebViewClient() {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// Kept "mailto" and "http(s)" as a String for readability and they won't be used anywhere else
			if (url.startsWith("mailto:")) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				startActivity(intent);
			} else if (url.startsWith("http:") || url.startsWith("https:")) {
				view.loadUrl(url);
			}
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// Nothing to do here
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			MyLog.e(LOG_TAG, "Error code: " + errorCode);

			Toast.makeText(ActivityWebsite.this, getString(R.string.settings_webview_error), Toast.LENGTH_LONG).show();

			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			MyLog.e(LOG_TAG, "SSL Error " + error.toString());
			Toast.makeText(ActivityWebsite.this, getString(R.string.settings_webview_error), Toast.LENGTH_LONG).show();
			handler.cancel();
		}
	};
}
