package nl.victronenergy.util;

import nl.victronenergy.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

/**
 * @author M2Mobi
 */
public class SmsSent extends BroadcastReceiver {

	private final String LOG_TAG = getClass().getSimpleName();

	@Override
	public void onReceive(Context ctx, Intent intent) {

		MyLog.i(LOG_TAG, "[SmsSent onReceive] getAction: " + intent.getAction());

		if (!intent.getAction().equals(Constants.SMS_COMMAND_SENT)) {
			return;
		}

		/**
		 * If the sms was not sent
		 */
		if (getResultCode() != Activity.RESULT_OK) {
			notifyDataChanged(ctx, intent);
		}

		switch (getResultCode()) {
			case Activity.RESULT_OK:
				Toast.makeText(ctx, ctx.getString(R.string.sms_sent), Toast.LENGTH_SHORT).show();
				break;
			case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				Toast.makeText(ctx, ctx.getString(R.string.sms_send_failed) + ": " + ctx.getString(R.string.sms_generic_failure), Toast.LENGTH_SHORT)
						.show();
				break;
			case SmsManager.RESULT_ERROR_NO_SERVICE:
				Toast.makeText(ctx, ctx.getString(R.string.sms_send_failed) + ": " + ctx.getString(R.string.sms_no_service), Toast.LENGTH_SHORT)
						.show();
				break;
			case SmsManager.RESULT_ERROR_NULL_PDU:
				Toast.makeText(ctx, ctx.getString(R.string.sms_send_failed) + ": " + ctx.getString(R.string.sms_null_pdu), Toast.LENGTH_SHORT).show();
				break;
			case SmsManager.RESULT_ERROR_RADIO_OFF:
				Toast.makeText(ctx, ctx.getString(R.string.sms_send_failed) + ": " + ctx.getString(R.string.sms_radio_off), Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				Toast.makeText(ctx, ctx.getString(R.string.sms_send_failed), Toast.LENGTH_SHORT).show();
				break;
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
