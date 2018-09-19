package nl.victronenergy.activities;

import nl.victronenergy.R;
import nl.victronenergy.models.UserResponse;
import nl.victronenergy.util.AnalyticsConstants;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.Constants.LOADER_ID;
import nl.victronenergy.util.Constants.POST;
import nl.victronenergy.util.Constants.RESPONSE_CODE;
import nl.victronenergy.util.Constants.WEBAPP;
import nl.victronenergy.util.UserUtils;
import nl.victronenergy.util.webservice.JsonParserHelper;
import nl.victronenergy.util.webservice.RestResponse;
import nl.victronenergy.util.webservice.WebserviceAsync;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.google.analytics.tracking.android.EasyTracker;

/**
 * Login screen for authentication.
 *
 * @author M2Mobi
 */
public class ActivityLogin extends ActionBarActivity implements OnClickListener, LoaderCallbacks<RestResponse>, OnEditorActionListener {
	private static final String LOG_TAG = "ActivityLogin";

	private long mLoginStartTime;
	private EditText mEditTextEmail;
	private EditText mEditTextPassword;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Force portrait orientation on phone
		if (!getResources().getBoolean(R.bool.is_phone)) {
			getSupportActionBar().hide();
		}

		setContentView(R.layout.activity_login);

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

	/**
	 * Initialize the view
	 */
	private void initView() {
		// Hide actionbar title
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		mEditTextEmail = (EditText) findViewById(R.id.edittext_email);
		mEditTextPassword = (EditText) findViewById(R.id.edittext_password);

		findViewById(R.id.button_demo).setOnClickListener(this);
		findViewById(R.id.button_sign_in).setOnClickListener(this);
		findViewById(R.id.button_privacy_policy).setOnClickListener(this);
		mEditTextPassword.setOnEditorActionListener(this);
		findViewById(R.id.textview_forgot_password).setOnClickListener(this);

		// If there is a username saved, make sure to prefill the fields
		if (!TextUtils.isEmpty(UserUtils.getUsername(this)) && !TextUtils.isEmpty(UserUtils.getPassword(this))) {
			mEditTextEmail.setText(UserUtils.getUsername(this));
			mEditTextPassword.setText(UserUtils.getPassword(this));
		}
	}

	/**
	 * Login with the userid
	 *
	 * @param pUserResponse
	 */
	private void openSiteList(UserResponse pUserResponse) {
		// Track time it took to login
		long loginEndTime = System.currentTimeMillis();
		EasyTracker.getTracker().sendTiming(AnalyticsConstants.CAT_TIMING_WEBSERVICE, loginEndTime - mLoginStartTime,
				AnalyticsConstants.TIMING_LOGIN, AnalyticsConstants.TIMING_LABEL_SPLASH);

		// Save the sessionId
		UserUtils.saveSessionID(this, pUserResponse.data.user.sessionId);
		UserUtils.saveUsername(this, mEditTextEmail.getText().toString());
		UserUtils.savePassword(this, mEditTextPassword.getText().toString());

		Intent intentMain = new Intent(this, ActivitySiteSummary.class);
		intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intentMain);

		finish();
	}

	/**
	 * Process the login, save the password and username and call the loginloader
	 */
	private void processLogin() {
		// Analytics stuff
		EasyTracker.getTracker().sendEvent(AnalyticsConstants.CAT_UI_ACTION, AnalyticsConstants.BUTTON_PRESS, AnalyticsConstants.LOGIN_BUTTON, null);

		// Check if the user entered a valid email address
		String email = mEditTextEmail.getText().toString();
		if (!isEmailAddressValid(email)) {
			showDialogWithMessage(getString(R.string.invalid_email_address));
			return;
		}

		// Check if the user entered a password
		String pass = mEditTextPassword.getText().toString();
		if (TextUtils.isEmpty(pass)) {
			showDialogWithMessage(getString(R.string.invalid_password));
			return;
		}

		callLoginLoader(mEditTextEmail.getText().toString(), pass);
	}

	/**
	 * Clear focus on EditText view and close soft keyboard
	 */
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mEditTextPassword.getWindowToken(), 0);
		mEditTextPassword.clearFocus();
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
		// Keep track of the start login time for analytics
		mLoginStartTime = System.currentTimeMillis();

		// Disable buttons
		setLoginButtonsDisabled(true);

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
	 * Parse the login response
	 *
	 * @param pRestResponse
	 *        The restresponse that should contain the userinfo json
	 */
	private void parseLoginResponse(RestResponse pRestResponse) {
		UserResponse userResponse = JsonParserHelper.getInstance().parseJsonAndShowError(this, pRestResponse, UserResponse.class);
		if (userResponse != null && userResponse.status != null) {
			if (userResponse.status.code == RESPONSE_CODE.RESPONSE_OK) {
				openSiteList(userResponse);
			}
		}
	}

	/**
	 * Set the state of the screen, and disable the login buttons, show indicator accordingly
	 *
	 * @param disabled
	 *        True if the buttons should be disabled, false if the buttons should be enabled
	 */
	private void setLoginButtonsDisabled(boolean disabled) {
		// Disable buttons and show the user we are trying to log in
		if (disabled) {
			findViewById(R.id.button_demo).setEnabled(false);
			findViewById(R.id.button_sign_in).setEnabled(false);
			findViewById(R.id.layout_login_progress).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.button_demo).setEnabled(true);
			findViewById(R.id.button_sign_in).setEnabled(true);
			findViewById(R.id.layout_login_progress).setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.button_demo:
				// Analytics stuff
				EasyTracker.getTracker().sendEvent(AnalyticsConstants.CAT_UI_ACTION, AnalyticsConstants.BUTTON_PRESS, AnalyticsConstants.DEMO_BUTTON,
						null);
				callLoginLoader(Constants.DEMO_EMAIL, Constants.DEMO_PASSWORD);
				break;

			case R.id.button_sign_in:
				processLogin();
				break;
			case R.id.textview_forgot_password:
				String forgotPasswordURL = WEBAPP.FORGOT_PASSWORD_URL;
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(forgotPasswordURL));
				startActivity(browserIntent);
				break;
            case R.id.button_privacy_policy:
                Intent privacyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.victronenergy.com/privacy-policy"));
                startActivity(privacyIntent);
                break;
		}
	}

	/**
	 * Shows a dialog that informs the user that he didn't enter a valid emailaddress
	 */
	private void showDialogWithMessage(String pMessage) {
		new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom)).setTitle(getString(R.string.app_name_complete))
				.setMessage(pMessage).setIcon(R.drawable.ic_launcher_base)
				.setNeutralButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	/**
	 * Checks if the email adres is valid
	 *
	 * @param email
	 *        The email adres that needs to be checked
	 * @return True if the email adres is valid
	 */
	private boolean isEmailAddressValid(String email) {
		return Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}

	@Override
	public Loader<RestResponse> onCreateLoader(int loaderId, Bundle params) {
		WebserviceAsync loader = WebserviceAsync.newInstance(ActivityLogin.this);
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

		setLoginButtonsDisabled(false);
	}

	@Override
	public void onLoaderReset(Loader<RestResponse> loader) {
		// Do nothing
	}

	@Override
	public boolean onEditorAction(TextView pTextView, int pActionId, KeyEvent pKeyEvent) {
		if (pActionId == EditorInfo.IME_ACTION_DONE) {
			hideKeyboard();
			processLogin();
			return true;
		}
		return false;
	}
}
