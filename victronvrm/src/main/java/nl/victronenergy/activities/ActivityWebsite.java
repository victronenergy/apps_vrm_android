package nl.victronenergy.activities;

import nl.victronenergy.BuildConfig;
import nl.victronenergy.R;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.WEBAPP;
import nl.victronenergy.util.MyLog;
import nl.victronenergy.util.UserUtils;
import nl.victronenergy.util.webservice.RestResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;
import com.google.analytics.tracking.android.EasyTracker;

import java.net.URI;

import javax.net.ssl.SSLPeerUnverifiedException;

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
	private String smsToken = null;
	private boolean setTokenDone = false;

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
		setTokenDone = false;
		smsToken = null;
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
		wv.getSettings().setDomStorageEnabled(true);

		wv.loadUrl(mSiteUrl);

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

	public boolean removeToken()
	{
		MyLog.d(LOG_TAG,"Removing token...");
		wv.loadUrl("javascript:window.localStorage.removeItem('"+WEBAPP.TOKEN_KEY+"');");
		return true;
	}

	public boolean setToken(String token)
	{
		if (token != null && !token.equals(""))
		{
			UserUtils.saveToken(this, token);
			MyLog.d(LOG_TAG, "Setting token to: "+token);
			wv.loadUrl("javascript:window.localStorage.setItem('"+WEBAPP.TOKEN_KEY+"','"+token+"');");
			wv.loadUrl("javascript:window.location.href = '"+mSiteUrl+"';");
			this.setTokenDone = true;
		}

		return true;
	}

	public void getSmsToken() {
		smsToken = null;
		final EditText input = new EditText(this);
		final ActivityWebsite activity = this;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.sms_token));
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		builder.setView(input);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				smsToken = input.getText().toString();
				new retrieveAndSetTokenTask(activity).execute();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		builder.show();
	}

	private class retrieveAndSetTokenTask extends AsyncTask<Void, Void, String> {
		public ActivityWebsite activity;

		private retrieveAndSetTokenTask(ActivityWebsite a)
		{
			this.activity = a;
		}

		protected void onPostExecute(String result) {
			if (result != null && result.equals("verification_sent"))
			{
				this.activity.getSmsToken();
			}
			else if (result != null && !result.equals(""))
			{
				this.activity.setToken(result);
			}
		}
		protected String doInBackground(Void... parameters) {
			Throwable throwable = null;
			String userName = UserUtils.getUsername(this.activity);
			String password = UserUtils.getPassword(this.activity);
			// If there is no username, we are logged in as the demo user
			if (TextUtils.isEmpty(userName)) {
				userName = Constants.DEMO_EMAIL;
				password = Constants.DEMO_PASSWORD;
			}

			try {
				JSONObject json = new JSONObject();
				json.put("username", userName);
				json.put("password", password);

				if (smsToken != null)
				{
					json.put("sms_token", smsToken);
				}

				StringEntity params = new StringEntity(json.toString());
				HttpPost request = new HttpPost();
				request.setURI(new URI(WEBAPP.LOGIN_ENDPOINT));
				request.setHeader("Content-Type", "application/json");
				request.setHeader("Accept", "application/json");
				request.setEntity(params);

				// Setup the connection parameters
				HttpParams httpParameters = new BasicHttpParams();
				ConnManagerParams.setTimeout(httpParameters, Constants.TIMEOUT);
				HttpConnectionParams.setConnectionTimeout(httpParameters, Constants.TIMEOUT);
				HttpConnectionParams.setSoTimeout(httpParameters, Constants.TIMEOUT);
				HttpProtocolParams.setVersion(httpParameters, HttpVersion.HTTP_1_1);
				HttpProtocolParams.setContentCharset(httpParameters, HTTP.UTF_8);
				HttpClient httpClient = new DefaultHttpClient(httpParameters);
				HttpResponse response = httpClient.execute(request);
				HttpEntity responseEntity = response.getEntity();
				StatusLine responseStatus = response.getStatusLine();

				// Set the response data
				String responseData = null;
				if (responseEntity != null) {
					responseData = EntityUtils.toString(responseEntity);
				}

				if (responseData != null) {
					try {
						JSONObject tokenResult = new JSONObject(responseData);
						MyLog.d(LOG_TAG, "ResponseData: " + tokenResult);
						if (tokenResult.has("token") && !tokenResult.isNull("token") && tokenResult.getString("token") != null) {
							return tokenResult.getString("token");
						}
						else if (tokenResult.has("verification_sent") && tokenResult.getBoolean("verification_sent"))
						{
							// Ask user for confirmation
							return "verification_sent";
						}

					} catch (Exception e) {
						MyLog.e(LOG_TAG, "Exception in creating JSONObject, raw data :" + responseData);
					}
				}


			} catch (SSLPeerUnverifiedException e) {
				Toast.makeText(this.activity, getString(R.string.date_inaccurate), Toast.LENGTH_SHORT).show();
			} catch (Throwable t) {
				throwable = t;
			} finally {
				if (throwable != null) {
					MyLog.e(LOG_TAG, throwable.toString());
				}
			}
			return null;
		}
	}

	public void retrieveAndSetToken() {
		String currentToken = UserUtils.getSavedToken(this);

		if (!currentToken.equals(""))
		{
			MyLog.d(LOG_TAG, "Got saved token: "+currentToken);
			setToken(currentToken);
		}
		else
		{
			MyLog.d(LOG_TAG, "Should request new token");
			new retrieveAndSetTokenTask(this).execute();
		}

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
			MyLog.d(LOG_TAG, "Page loaded: "+url);
			if (!setTokenDone)
			{
				removeToken();
				retrieveAndSetToken();
				setTokenDone = true;
			}


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
