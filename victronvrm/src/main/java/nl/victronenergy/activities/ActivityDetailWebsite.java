package nl.victronenergy.activities;

import nl.victronenergy.R;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.WEBAPP;
import nl.victronenergy.util.MyLog;
import nl.victronenergy.util.UserUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.analytics.tracking.android.EasyTracker;

import java.net.URI;

import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * Login to the VRM and then show the VRM webapp<br/>
 *
 * @author El Niño
 */
public class ActivityDetailWebsite extends ActionBarActivity {
    private static TokenCallbackInterface mInterface;

    private static final String TAG = "ActivityWebsite";
    private WebView wv;
    private String mSiteUrl;
    private String smsToken = null;
    private boolean setTokenDone = false;

    private ProgressBar mLoading;
    private int mSiteId;
    private String mToken;
    private String mGeneratedToken;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_website);

        mSiteUrl = getIntent().getStringExtra(Constants.INTENT_SITE_URL);
        mSiteId = getIntent().getIntExtra("siteId", -1);
        mLoading = (ProgressBar) findViewById(R.id.loading_spinner);

        initView();

        if (getIntent().hasExtra("userToken")) {
            mToken = getIntent().getStringExtra("userToken");
        }

        if (mToken != null) {
            new GenerateToken(this).execute();
        } else {
            new GetUserToken(this).execute();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    private void showSite() {
        //Retrieve the token if none is set
        if (mToken == null || mToken.equals("")) {
            //There is no saved token
            new GetUserToken(this).execute();
        } else {
            //Token found, lets generate a authentication token
            new GenerateToken(this).execute();
        }
    }

    private void loadUrl() {
        wv.loadUrl("https://vrm.victronenergy.com/login?token=" + mGeneratedToken + "&redirect=/installation/" + mSiteId + "/dashboard");
    }

    /**
     * Show an alert dialog via which the user can enter the SMS verification code
     */
    public void getSmsToken() {
        smsToken = null;
        final EditText input = new EditText(this);
        final ActivityDetailWebsite activity = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.sms_token));
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                smsToken = input.getText().toString();

                Log.d(TAG, "onClick: SMS code received: " + smsToken);
                new GetUserToken(ActivityDetailWebsite.this).execute();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                loadUrl();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    loadUrl();
                }
            });
        }

        builder.show();
    }

    /**
     * Generate a new sign in token
     */
    private class GenerateToken extends AsyncTask<Void, Void, String> {
        public ActivityDetailWebsite activity;

        private GenerateToken(ActivityDetailWebsite a)
        {
            this.activity = a;
        }

        protected void onPostExecute(String result) {
            Log.d("Generate Token", "onPostExecute: post execute: " + result);

            if (result != null && result.equals("verification_sent"))
            {
                Log.d(TAG, "onPostExecute: SMS verification sent!");
                this.activity.getSmsToken();
            }
            else if (result != null && !result.equals(""))
            {
                Log.d(TAG, "onPostExecute: Generated a new Token: " + result);
                mGeneratedToken = result;


                if (mInterface != null) {
                    Log.d(TAG, "onPostExecute: Callback to save mGeneratedToken token");
                    mInterface.setUserToken(mGeneratedToken);
                } else {
                    Log.d(TAG, "onPostExecute: Interface is null, mGeneratedToken token");
                }

                //Try to show the site again using the new token
                loadUrl();

            } else {
                //Get a new user token and try again
                Log.d(TAG, "onPostExecute: Get New User Token!");
                new GetUserToken(ActivityDetailWebsite.this).execute();
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
                Log.d("DO IN BACKGROUND", "doInBackground: Token: " + mToken);

//                StringEntity params = new StringEntity(json.toString());
                HttpGet request = new HttpGet();
                request.setURI(new URI("https://vrmapi.victronenergy.com/v2/auth/generatetoken"));
                request.setHeader("X-Authorization", "Bearer " + mToken);
//                request.setEntity(params);

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
                    Log.d("Generate Token", "doInBackground: " + responseData);
                    try {
                        JSONObject tokenResult = new JSONObject(responseData);
                        MyLog.d(TAG, "ResponseData: " + tokenResult);
                        if (tokenResult.has("token") && !tokenResult.isNull("token") && tokenResult.getString("token") != null) {
                            mGeneratedToken = tokenResult.getString("token");
                            return tokenResult.getString("token");
                        }
                        else if (tokenResult.has("verification_sent") && tokenResult.getBoolean("verification_sent"))
                        {
                            // Ask user for confirmation
                            return "verification_sent";
                        } else if (tokenResult.isNull("token")) {
                            return "error";
                        }

                    } catch (Exception e) {
                        MyLog.e(TAG, "Exception in creating JSONObject, raw data :" + responseData);
                    }
                }
            } catch (SSLPeerUnverifiedException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, getString(R.string.date_inaccurate), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Throwable t) {
                throwable = t;
            } finally {
                if (throwable != null) {
                    MyLog.e(TAG, throwable.toString());
                }
            }
            return null;
        }
    }

    /**
     * Get a token for the user which can be used to generate a auth token to allow for signing in without showing the
     * sign in screen
     */
    private class GetUserToken extends AsyncTask<Void, Void, String> {
        public final ActivityDetailWebsite activity;

        private GetUserToken(ActivityDetailWebsite a)
        {
            this.activity = a;
        }

        protected void onPostExecute(String result) {
            if (result != null && result.equals("verification_sent"))
            {
                Log.d(TAG, "onPostExecute: SMS verification sent!");
                this.activity.getSmsToken();
            }
            else if (result != null && !result.equals("") && !result.equals("smserror") && !result.equals("crederror"))
            {
                Log.d(TAG, "onPostExecute: Saving a new Token: " + result);
                UserUtils.saveToken(ActivityDetailWebsite.this, result);
                mToken = result;

                if (mInterface != null) {
                    Log.d(TAG, "onPostExecute: Callback to save user token");
                    mInterface.setUserToken(mToken);
                } else {
                    Log.d(TAG, "onPostExecute: Interface is null, user token");
                }

                //Try to show the site again using the new token
                showSite();
            } else if (result != null && !result.equals("") && result.equals("smserror")) {
                Log.d(TAG, "onPostExecute: SMS ERROR");
                Toast.makeText(ActivityDetailWebsite.this, getString(R.string.error_sms_invalid), Toast.LENGTH_LONG).show();
                getSmsToken();
            } else {
                Log.d(TAG, "onPostExecute: OTHER ERROR");
                loadUrl();
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
                        MyLog.d(TAG, "ResponseData: " + tokenResult + " --- " + responseStatus);
                        if (tokenResult.has("token") && !tokenResult.isNull("token") && tokenResult.getString("token") != null) {
                            return tokenResult.getString("token");
                        }
                        else if (tokenResult.has("verification_sent") && tokenResult.getBoolean("verification_sent"))
                        {
                            // Ask user for confirmation
                            return "verification_sent";
                        } else if (tokenResult.isNull("token") && tokenResult.isNull("idUser")) {
                            Log.d(TAG, "doInBackground: ERROR: " + tokenResult + " -- " + smsToken);
                            if (smsToken == null) {
                                //Invalid credentials
                                return "crederror";
                            } else {
                                //Invalid sms code?
                                return "smserror";
                            }
                        }

                    } catch (Exception e) {
                        MyLog.e(TAG, "Exception in creating JSONObject, raw data :" + responseData);
                    }
                }


            } catch (SSLPeerUnverifiedException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, getString(R.string.date_inaccurate), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Throwable t) {
                throwable = t;
            } finally {
                if (throwable != null) {
                    MyLog.e(TAG, throwable.toString());
                }
            }
            return null;
        }
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
        WebStorage.getInstance().deleteAllData();

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
            } else {
                onBackPressed();
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
            MyLog.d(TAG, "Page loaded: "+url);

            mLoading.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            MyLog.e(TAG, "Error code: " + errorCode);

            Toast.makeText(ActivityDetailWebsite.this, getString(R.string.settings_webview_error), Toast.LENGTH_LONG).show();

            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            MyLog.e(TAG, "SSL Error " + error.toString());
            Toast.makeText(ActivityDetailWebsite.this, getString(R.string.settings_webview_error), Toast.LENGTH_LONG).show();
            handler.cancel();
        }
    };

    public static Intent getIntent(Context c, TokenCallbackInterface frag, int siteId, String userToken, String gToken) {
        Intent intent = new Intent(c, ActivityDetailWebsite.class);
        intent.putExtra(Constants.INTENT_SITE_URL, Constants.WEBAPP.OPEN_SITE_URL.replace("{site}",Integer.toString(siteId)));
        intent.putExtra("siteId", siteId);

        if (userToken != null) {
            intent.putExtra("userToken", userToken);
        }

        mInterface = frag;

        return intent;
    }

    public interface TokenCallbackInterface {
        void setUserToken(String token);
        void setGeneratedToken(String gtoken);
    }
}
