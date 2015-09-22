/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.util;

import java.io.File;

import nl.victronenergy.models.Site;
import nl.victronenergy.util.Constants.IO_PICTURE;
import nl.victronenergy.util.Constants.SHARED_PREFERENCES;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Contains a set of functions used by the IO Extender and Generator
 *
 * @author Victron Energy
 */
public class IoExtenderUtils {

	/**
	 * Creates a string with the SMS command
	 *
	 * @param site
	 * @param ioCode
	 * @param command
	 * @return
	 */
	public static String prepareSmsCommand(Site site, String ioCode, boolean command) {
		String smsBody = "";

		smsBody += ioCodeToCommand(ioCode);

		if (command) {
			smsBody += Constants.SMS_COMMAND_ON;
		} else {
			smsBody += Constants.SMS_COMMAND_OFF;
		}

		return smsBody;
	}

	/**
	 * Returns output command for output code
	 *
	 * @param s
	 * @return
	 */
	public static String ioCodeToCommand(String s) {
		if (s.equals("OUT3")) {
			return Constants.SMS_COMMAND_OUTPUT + " 3 ";
		} else if (s.equals("OUT2")) {
			return Constants.SMS_COMMAND_OUTPUT + " 2 ";
		} else {
			return Constants.SMS_COMMAND_OUTPUT + " 1 ";
		}
	}

	/**
	 * Returns the output id
	 *
	 * @param s
	 * @return
	 */
	public static int ioCodeToInt(String s) {
		if (s.equals("OUT3")) {
			return 3;
		} else if (s.equals("OUT2")) {
			return 2;
		} else {
			return 1;
		}
	}

	/**
	 * Returns the selected IO Code, defaults to OUT1
	 *
	 * @param ctx
	 * @param siteID
	 * @return
	 */
	public static String getGeneratorIoCode(Context ctx, int siteID) {
		return ctx.getSharedPreferences(SHARED_PREFERENCES.VICTRON_PREFERENCES, Context.MODE_PRIVATE).getString(
				SHARED_PREFERENCES.GENERATOR_OUTPUT + siteID, null);
	}

	/**
	 * Save the selected IO Code to shared preferences
	 *
	 * @param ctx
	 * @param siteID
	 * @param ioCode
	 */
	public static void saveGeneratorIoCode(Context ctx, int siteID, String ioCode) {
		ctx.getSharedPreferences(SHARED_PREFERENCES.VICTRON_PREFERENCES, Context.MODE_PRIVATE).edit()
				.putString(SHARED_PREFERENCES.GENERATOR_OUTPUT + siteID, ioCode).commit();
	}

	/**
	 * Returns the generator state when the IO is closed
	 *
	 * @param ctx
	 * @param siteID
	 * @return
	 */
	public static int getGeneratorStateClosed(Context ctx, int siteID) {
		return ctx.getSharedPreferences(SHARED_PREFERENCES.VICTRON_PREFERENCES, Context.MODE_PRIVATE).getInt(
				SHARED_PREFERENCES.GENERATOR_STATE + siteID, 0);
	}

	/**
	 * Save the selected generator state for when the IO is closed
	 *
	 * @param ctx
	 * @param siteID
	 * @param state
	 */
	public static void saveGeneratorStateClosed(Context ctx, int siteID, int state) {
		ctx.getSharedPreferences(SHARED_PREFERENCES.VICTRON_PREFERENCES, Context.MODE_PRIVATE).edit()
				.putInt(SHARED_PREFERENCES.GENERATOR_STATE + siteID, state).commit();
	}

	/**
	 * Send an SMS using the SMS Manager (Don't forget to register the listener if you want to know the status of the
	 * sent sms)
	 *
	 * @param pContext
	 * @param pMessage
	 * @param pPhoneNumber
	 * @param pSiteId
	 *        The id of the site this message is send for
	 * @param pSiteIndex
	 *        The index of the site this message is send for
	 * @param pIsGeneratorButton
	 *        Indicates if this command is send by the generator button or by an IO Extender switch
	 * @param pOriginalState
	 *        The original state of the IO
	 */
	public static void sendSMS(Context pContext,
			String pPhoneNumber,
			String pMessage,
			int pSiteId,
			int pSiteIndex,
			String pIoCode,
			boolean pIsGeneratorButton,
			boolean pOriginalState) {

		Intent iSent = new Intent(Constants.SMS_COMMAND_SENT);
		iSent.putExtra(Constants.INTENT_SITE_ID, pSiteId);
		iSent.putExtra(Constants.INTENT_IO_INDEX, pSiteIndex);
		iSent.putExtra(Constants.INTENT_IS_GENERATOR_BTN, pIsGeneratorButton);
		iSent.putExtra(Constants.INTENT_ORIGINAL_STATE, pOriginalState);
		if (pIoCode != null) {
			iSent.putExtra(Constants.INTENT_IO_CODE, pIoCode);
		}

		Intent iDelivered = new Intent(Constants.SMS_COMMAND_DELIVERED);
		iDelivered.putExtra(Constants.INTENT_SITE_ID, pSiteId);
		iDelivered.putExtra(Constants.INTENT_IO_INDEX, pSiteIndex);
		iDelivered.putExtra(Constants.INTENT_IS_GENERATOR_BTN, pIsGeneratorButton);
		iDelivered.putExtra(Constants.INTENT_ORIGINAL_STATE, pOriginalState);
		if (pIoCode != null) {
			iDelivered.putExtra(Constants.INTENT_IO_CODE, pIoCode);
		}

		PendingIntent pSent = PendingIntent.getBroadcast(pContext, 0, iSent, PendingIntent.FLAG_CANCEL_CURRENT);
		PendingIntent pDelivered = PendingIntent.getBroadcast(pContext, 0, iDelivered, PendingIntent.FLAG_CANCEL_CURRENT);

		android.telephony.SmsManager sms = android.telephony.SmsManager.getDefault();
		sms.sendTextMessage(pPhoneNumber, null, pMessage, pSent, pDelivered);
	}

	/**
	 * Returns the path of a picture for an attribute for a given site
	 *
	 * @param pContext
	 *        The context needed to get the external files dir
	 * @param pSiteId
	 *        The id of the site
	 * @param pAttributeCode
	 *        The code of the attribute we want the picture of
	 * @return The path as a string
	 */
	public static String getIOPicturePath(Context pContext, int pSiteId, String pAttributeCode) {
		return pContext.getExternalFilesDir(null).getAbsolutePath() + "/" + IO_PICTURE.PREFIX + pSiteId + pAttributeCode + IO_PICTURE.EXTENSION;
	}

	/**
	 * Tries to set the picture in the imageview if the picture is available
	 */
	public static void setPic(String pPhotoPath, ImageView pImageViewPicture) {
		File file = new File(pPhotoPath);
		if (file.exists()) {
			ImageLoader.getInstance().displayImage("file:///" + pPhotoPath, pImageViewPicture);
		}
	}
}
