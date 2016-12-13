package nl.victronenergy.util;

import nl.victronenergy.VictronApplication;
import nl.victronenergy.util.Constants.DEFAULT_VALUE;
import nl.victronenergy.util.Constants.SHARED_PREFERENCES;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Contains functions for the user data
 * 
 * @author M2Mobi
 */
public class UserUtils {
	private static final String LOG_TAG = "UserUtils";

	/**
	 * Delete userdata
	 * 
	 * @param pContext
	 *        Context to load the user data from the shared preferences
	 */
	public static void deleteUserData(Context pContext) {
		SharedPreferences.Editor editor = pContext.getSharedPreferences(SHARED_PREFERENCES.VICTRON_PREFERENCES, Context.MODE_PRIVATE).edit();
		editor.remove(SHARED_PREFERENCES.USERNAME);
		editor.remove(SHARED_PREFERENCES.PASSWORD);
		editor.commit();
	}

	/**
	 * Returns the current sessionID
	 * 
	 * @param pContext
	 *        The context needed to retrieve the sessionId from the preferences
	 * @return The sesseionId
	 */
	public static String getSessionID(Context pContext) {
		return pContext.getSharedPreferences(SHARED_PREFERENCES.VICTRON_PREFERENCES, Context.MODE_PRIVATE).getString(SHARED_PREFERENCES.SESSIONID,
				DEFAULT_VALUE.SESSION_ID);
	}

	/**
	 * Save the sessionID to shared preferences
	 * 
	 * @param pContext
	 *        The context needed to save the sessionId to the shared preferences
	 * @param pSessionId
	 *        The sessionId to save
	 */
	public static void saveSessionID(Context pContext, String pSessionId) {
		pContext.getSharedPreferences(SHARED_PREFERENCES.VICTRON_PREFERENCES, Context.MODE_PRIVATE).edit()
				.putString(SHARED_PREFERENCES.SESSIONID, pSessionId).commit();
	}

	/**
	 * Save the username to the shared preferences, used for autorelogin
	 * 
	 * @param pContext
	 *        The context needed to save the username to shared preferences
	 * @param username
	 *        The username to save
	 */
	public static void saveUsername(Context pContext, String username) {
		pContext.getSharedPreferences(SHARED_PREFERENCES.VICTRON_PREFERENCES, Context.MODE_PRIVATE).edit()
				.putString(SHARED_PREFERENCES.USERNAME, username).commit();
	}

	/**
	 * Returns the username of the current logged in user
	 * 
	 * @param pContext
	 *        The context needed to load the username from the shared preferences
	 * @return The username
	 */
	public static String getUsername(Context pContext) {
		return pContext.getSharedPreferences(SHARED_PREFERENCES.VICTRON_PREFERENCES, Context.MODE_PRIVATE).getString(SHARED_PREFERENCES.USERNAME, "");
	}

	/**
	 * Returns the password of the current logged in user
	 * 
	 * @param pContext
	 *        The context needed to load the password from shared preferences
	 * @return The Password
	 */
	public static String getPassword(Context pContext) {
		String password = pContext.getSharedPreferences(SHARED_PREFERENCES.VICTRON_PREFERENCES, Context.MODE_PRIVATE).getString(
				SHARED_PREFERENCES.PASSWORD, "");
		String unencryptedPass = null;
		try {
			unencryptedPass = SimpleCrypto.decrypt(VictronApplication.getEncryptionMasterKey(), password);
		} catch (Exception e) {
			MyLog.e(LOG_TAG, "Exception decrypting data\n" + e);
			return "";
		}

		return unencryptedPass;
	}

	/**
	 * Save the password to shared preferences
	 * 
	 * @param pContext
	 *        used to access the shared preferences
	 * @param password
	 *        the passwordt that needs to be encrypted and saved
	 * @return false if the encryption fails and the password can't be encrypted<br/>
	 *         true when the encryption went ok and the password is saved
	 */
	public static boolean savePassword(Context pContext, String password) {
		String encryptedPass = null;
		try {
			encryptedPass = SimpleCrypto.encrypt(VictronApplication.getEncryptionMasterKey(), password);
		} catch (Exception e) {
			MyLog.e(LOG_TAG, "unexpected error encrypting data\n" + e);
			return false;
		}

		pContext.getSharedPreferences(SHARED_PREFERENCES.VICTRON_PREFERENCES, Context.MODE_PRIVATE).edit()
				.putString(SHARED_PREFERENCES.PASSWORD, encryptedPass).commit();
		return true;
	}

}
