/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.util.webservice;

import nl.victronenergy.BuildConfig;
import nl.victronenergy.R;
import nl.victronenergy.models.BaseResponse;
import nl.victronenergy.util.Constants.RESPONSE_CODE;
import nl.victronenergy.util.MyLog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.view.ContextThemeWrapper;

import com.google.gson.GsonBuilder;

/**
 * Helps parsing the Json files to objects
 *
 * @author Victron Energy
 */
public class JsonParserHelper {
	private static final String LOG_TAG = "JsonParserHelper";

	private static JsonParserHelper jsonParserInstance;
	private boolean mIsDialogShowing;

	/**
	 * Returns an instance of the JsonParserHelper
	 *
	 * @return Instance of the JsonParserHelper
	 */
	public static JsonParserHelper getInstance() {
		if (jsonParserInstance == null) {
			jsonParserInstance = new JsonParserHelper();
		}
		return jsonParserInstance;
	}

	/**
	 * Parses a Json String and creates an object of the given class
	 *
	 * @param pRestResponse
	 *        The RestResponse to check, if the status code is ok it should contain the proper json string
	 * @param pJsonClass
	 *        The class we want to create an object from
	 * @return Object of the requested class, null if there is something wrong with the json
	 */
	public <T> T parseJson(RestResponse pRestResponse, Class<T> pJsonClass) {
		T parsedJsonObject = null;

		try {
			parsedJsonObject = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(pRestResponse.getData(), pJsonClass);
		} catch (Exception ex) {
			MyLog.e(LOG_TAG, "Error parsing JSON for class: " + pJsonClass.getSimpleName(), ex);
		}

		// Checks if the response is valid, if not show a dialog
		if (!isResponseValid((BaseResponse) parsedJsonObject)) {
			return null;
		}

		return parsedJsonObject;
	}

	/**
	 * Parses a Json String and creates an object of the given class, this version also shows an error dialog if needed
	 *
	 * @param pContext
	 *        The context needed to load the resource strings
	 * @param pRestResponse
	 *        The RestResponse to check, if the status code is ok it should contain the proper json string
	 * @param pJsonClass
	 *        The class we want to create an object from
	 * @return Object of the requested class, null if there is something wrong with the json
	 */
	public <T> T parseJsonAndShowError(Context pContext, RestResponse pRestResponse, Class<T> pJsonClass) {
		if (pContext == null) {
			MyLog.e(LOG_TAG, "Context is null");
			return null;
		}

		T parsedJsonObject = null;

		// Check if there is a status code, if not just ignore the response and show the user that there was a problem
		// reaching the server
		if (pRestResponse.getStatusCode() > 0) {
			try {
				parsedJsonObject = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(pRestResponse.getData(), pJsonClass);

				// Check the status code and show the user a message if it's not ok
				BaseResponse parsedResponse = (BaseResponse) parsedJsonObject;

				// Checks if the response is valid, if not show a dialog
				if (!isResponseValid((BaseResponse) parsedJsonObject)) {
					showDialogWithMessage(pContext, pContext.getString(R.string.error_unable_to_connect));
					return null;
				}

				// If the response is ok or the session id has expired don't inform the user
				if (parsedResponse.status.code != RESPONSE_CODE.RESPONSE_OK && parsedResponse.status.code != RESPONSE_CODE.RESPONSE_SESSION_ID) {
					String message = getMessageForErrorCode(pContext, parsedResponse.status.code);

					// Show some debug info when this is a debug build
					if (BuildConfig.DEBUG) {
						message += "\n\n[" + parsedResponse.status.code + "] " + parsedResponse.status.message;
					}

					showDialogWithMessage(pContext, message);
				}
			} catch (Exception ex) {
				MyLog.e(LOG_TAG, "Error parsing JSON for class: " + pJsonClass.getSimpleName(), ex);
				showDialogWithMessage(pContext, pContext.getString(R.string.error_unable_to_connect));
			}
		} else {
			showDialogWithMessage(pContext, pContext.getString(R.string.error_unable_to_connect));
		}

		return parsedJsonObject;
	}

	/**
	 * Shows a dialog that informs the user that he didn't enter a valid emailaddress
	 *
	 * @param pContext
	 *        the context needed to load the string resources
	 * @param pMessage
	 *        The message you want to display to the user
	 */
	private void showDialogWithMessage(Context pContext, String pMessage) {
		if (!mIsDialogShowing) {
			mIsDialogShowing = true;
			new AlertDialog.Builder(new ContextThemeWrapper(pContext, R.style.AlertDialogCustom))
					.setTitle(pContext.getString(R.string.app_name_complete)).setMessage(pMessage).setIcon(R.drawable.ic_launcher_base)
					.setNeutralButton(pContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int which) {
							dialogInterface.dismiss();
							mIsDialogShowing = false;
						}
					}).setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialogInterface) {
							mIsDialogShowing = false;
						}
					}).show();
		}
	}

	/**
	 * Checks if the response is a valid response
	 *
	 * @param baseResponse
	 *        The baseResponse to check
	 * @return True if the response is valid, false if not
	 */
	private boolean isResponseValid(BaseResponse baseResponse) {
		if (baseResponse != null && baseResponse.status != null) {
			return true;
		}

		return false;
	}

	/**
	 * Returns an error code message for a certain error code
	 *
	 * @param pContext
	 *        Context needed to load the resource String
	 * @param pErrorCode
	 *        The error code to retrieve the error message for
	 * @return The error message we want to display to the user
	 */
	private String getMessageForErrorCode(Context pContext, int pErrorCode) {
		switch (pErrorCode) {
			case RESPONSE_CODE.RESPONSE_NOT_ALLOWED:
				return pContext.getString(R.string.response_not_allowed);
			case RESPONSE_CODE.RESPONSE_OLD_VERSION:
				return pContext.getString(R.string.response_old_version);
			case RESPONSE_CODE.RESPONSE_USER_NOT_FOUND:
				return pContext.getString(R.string.response_invalid_login);
			case RESPONSE_CODE.RESPONSE_WRONG_USER_PASS:
				return pContext.getString(R.string.response_invalid_login);
			default:
				return pContext.getString(R.string.error_unable_to_connect);
		}
	}
}
