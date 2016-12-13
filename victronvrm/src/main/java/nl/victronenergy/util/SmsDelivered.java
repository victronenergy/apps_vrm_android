package nl.victronenergy.util;

import nl.victronenergy.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * @author M2Mobi
 */
public class SmsDelivered extends BroadcastReceiver {

	private final String LOG_TAG = getClass().getSimpleName();

	@Override
	public void onReceive(Context ctx, Intent intent) {

		MyLog.i(LOG_TAG, "[onReceive] getAction: " + intent.getAction());

		if (!intent.getAction().equals(Constants.SMS_COMMAND_DELIVERED)) {
			return;
		}

		/**
		 * If the sms was not delivered
		 */
		if (getResultCode() != Activity.RESULT_OK) {
			Toast.makeText(ctx, ctx.getString(R.string.sms_not_delivered), Toast.LENGTH_SHORT).show();
			notifyDataChanged(ctx, intent);
		}
	}

	private void notifyDataChanged(Context ctx, Intent intent) {
		Intent i = new Intent(Constants.BROADCAST_TOGGLE_BUTTON);
		i.putExtra(Constants.INTENT_SITE_ID, intent.getIntExtra(Constants.INTENT_SITE_ID, -1));
		i.putExtra(Constants.INTENT_IO_INDEX, intent.getIntExtra(Constants.INTENT_IO_INDEX, -1));
		i.putExtra(Constants.INTENT_IO_CODE, intent.getStringExtra(Constants.INTENT_IO_CODE));
		i.putExtra(Constants.INTENT_IS_GENERATOR_BTN, intent.getBooleanExtra(Constants.INTENT_IS_GENERATOR_BTN, true));
		i.putExtra(Constants.INTENT_ORIGINAL_STATE, intent.getBooleanExtra(Constants.INTENT_ORIGINAL_STATE, true));
		i.putExtra(Constants.INTENT_FIRED_BY_SMS_BROADCAST, true);
		ctx.sendBroadcast(i);
	}
}
