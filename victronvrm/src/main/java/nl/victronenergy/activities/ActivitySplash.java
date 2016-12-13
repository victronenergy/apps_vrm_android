package nl.victronenergy.activities;

import nl.victronenergy.R;
import nl.victronenergy.models.UserResponse;
import nl.victronenergy.util.AnalyticsConstants;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.LOADER_ID;
import nl.victronenergy.util.Constants.POST;
import nl.victronenergy.util.Constants.RESPONSE_CODE;
import nl.victronenergy.util.Constants.SHARED_PREFERENCES;
import nl.victronenergy.util.UserUtils;
import nl.victronenergy.util.webservice.JsonParserHelper;
import nl.victronenergy.util.webservice.RestResponse;
import nl.victronenergy.util.webservice.WebserviceAsync;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import com.google.analytics.tracking.android.EasyTracker;

/**
 * Splash screen, automatically tries to login the user and continue to the sitelist, if login fails it will open the
 * login screen
 *
 * @author M2mobi
 */
public class ActivitySplash extends FragmentActivity implements LoaderCallbacks<RestResponse> {
	private static final String LOG_TAG = "ActivitySplash";

	/* Timer related values in miliseconds */
	private static final int SPLASH_TIME = 2000;
	private static final int SPLASH_STEP = 1000;

	private MyCount mCounter;

	// Try auto login
	private boolean mTryLogin = false;
	private boolean mSplashFinished = false;
	private boolean mLoginFinished = true;
	private String mSessionId;

	/* Used to track login time for google analytics timing */
	private long mLoginStartTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_splash);

		// Check if there are saved login credentials, if so try to login
		SharedPreferences sp = getSharedPreferences(SHARED_PREFERENCES.VICTRON_PREFERENCES, MODE_PRIVATE);
		if (sp.contains(SHARED_PREFERENCES.USERNAME) && sp.contains(SHARED_PREFERENCES.PASSWORD)) {
			String username = UserUtils.getUsername(this);
			String password = UserUtils.getPassword(this);

			if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
				mLoginStartTime = System.currentTimeMillis();
				findViewById(R.id.layout_progress_login).setVisibility(View.VISIBLE);
				mLoginFinished = false;
				mTryLogin = true;

				callLoginLoader(username, password);
			}
		}
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

	/**
	 * Call the loader to try login
	 *
	 * @param email
	 *        The emailaddress you want to login with
	 * @param password
	 *        The password you want to login with
	 */
	private void callLoginLoader(String email, String password) {
		Bundle params = new Bundle();
		params.putString(POST.URI, Constants.WEBSERVICE_USER_LOGIN);
		params.putString(POST.EMAIL, email);
		params.putString(POST.PASSWORD, password);
		params.putString(POST.DEVICE_TYPE, getString(R.string.device_type));

		if (getSupportLoaderManager().getLoader(LOADER_ID.LOGIN) == null) {
			getSupportLoaderManager().initLoader(LOADER_ID.LOGIN, params, this);
		} else {
			getSupportLoaderManager().restartLoader(LOADER_ID.LOGIN, params, this);
		}
	}

	/**
	 * Opens the next screen this can be the login screen or the site list
	 **/
	private void openNextScreen() {
		if (mTryLogin) {
			if (mLoginFinished && mSplashFinished) {
				// Analytics
				long loginEndTime = System.currentTimeMillis();
				EasyTracker.getTracker().sendTiming(AnalyticsConstants.CAT_TIMING_WEBSERVICE, loginEndTime - mLoginStartTime,
						AnalyticsConstants.TIMING_LOGIN, AnalyticsConstants.TIMING_LABEL_SPLASH);

				// Save the sessionId
				UserUtils.saveSessionID(this, mSessionId);

				EasyTracker.getTracker().sendTiming(AnalyticsConstants.CAT_TIMING_WEBSERVICE, loginEndTime - mLoginStartTime,
						AnalyticsConstants.TIMING_LOGIN, AnalyticsConstants.TIMING_LABEL_SPLASH);
				startActivity(new Intent(this, ActivitySiteSummary.class));
			}
		} else {
			if (mSplashFinished) {
				startActivity(new Intent(this, ActivityLogin.class));
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mCounter = new MyCount(SPLASH_TIME, SPLASH_STEP);
		mCounter.start();
	}

	/**
	 * The cancel here prevents the application to launch multiple activities once the splash ends
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mCounter.cancel();
	}

	/**
	 * Countdown timer starts a timer and tries to open the next screen
	 *
	 * @author M2Mobi
	 */
	public class MyCount extends CountDownTimer {

		public MyCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			// Do nothing
		}

		@Override
		public void onFinish() {
			mSplashFinished = true;
			openNextScreen();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// Do nothing
		}
	}

	/**
	 * Parse the login response
	 *
	 * @param pRestResponse
	 *        The restresponse that should contain the userinfo json
	 */
	private void parseLoginResponse(RestResponse pRestResponse) {
		UserResponse userResponse = JsonParserHelper.getInstance().parseJson(pRestResponse, UserResponse.class);
		if (userResponse != null) {
			if (userResponse.status.code == RESPONSE_CODE.RESPONSE_OK) {
				mLoginFinished = true;
				mSessionId = userResponse.data.user.sessionId;
			} else {
				mTryLogin = false;
			}
		} else {
			mTryLogin = false;
		}
		openNextScreen();
	}

	@Override
	public Loader<RestResponse> onCreateLoader(int loaderId, Bundle params) {
		WebserviceAsync loader = WebserviceAsync.newInstance(ActivitySplash.this);
		loader.setParams(params);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<RestResponse> loader, RestResponse response) {
		switch (loader.getId()) {
			case LOADER_ID.LOGIN:
				parseLoginResponse(response);
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<RestResponse> arg0) {
		// Do nothing
	}
}
