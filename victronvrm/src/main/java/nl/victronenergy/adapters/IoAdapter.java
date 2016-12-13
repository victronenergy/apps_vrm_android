package nl.victronenergy.adapters;

import android.content.pm.PackageManager;
import nl.victronenergy.BuildConfig;
import nl.victronenergy.R;
import nl.victronenergy.models.Attribute;
import nl.victronenergy.models.Site;
import nl.victronenergy.util.Constants;
import nl.victronenergy.util.IoExtenderUtils;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The IOAdapter shows a list of ioObject and their status<br/>
 * For output ioObjects a toggle is shown which can toggle the output on or off<br/>
 * When a toggle is pressed the user will be asked if he wants to send the SMS command to toggle the ioObject's status<br/>
 * If the user agrees an sms will be send to toggle the IO status
 *
 * @author M2Mobi
 */
public class IoAdapter extends BaseAdapter {
	private final String LOG_TAG = "IoAdapter";

	private Context mContext;
	private Site mSite;
	private boolean mShowStatus;

	public IoAdapter(Context context, Site site, boolean showStatus) {
		mContext = context;
		mSite = site;
		mShowStatus = showStatus;
	}

	@Override
	public int getCount() {
		if (mSite.getIoExtenderData() != null) {
			return mSite.getIoExtenderData().getIoExtenderCount();
		}
		return 0;
	}

	/**
	 * Set the toggle state (enabled or disabled)
	 *
	 * @param toggleButton
	 *        The imagebutton of the toggle
	 * @param pIsEnabled
	 *        The state of the toggle we want the toggle to be in
	 */
	private void setToggleState(Attribute ioObject, ImageButton toggleButton, boolean pIsEnabled) {
		if (ioObject == null) {
			return;
		}
		if (ioObject.isOpen()) {
			if (pIsEnabled) {
				toggleButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.switch_on).mutate());
			} else {
				toggleButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.switch_on_disabled).mutate());
			}
		} else {
			if (pIsEnabled) {
				toggleButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.switch_off).mutate());
			} else {
				toggleButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.switch_off_disabled).mutate());
			}
		}
		toggleButton.setEnabled(pIsEnabled);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// We have to inflate a new view or else the click listener will break
		convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_io, parent, false);

		Attribute ioObject = mSite.getIoExtenderData().getIoExtenderAtIndex(position);
		if (ioObject == null) {
			return convertView;
		}

		ImageView iv = (ImageView) convertView.findViewById(R.id.iv_io_icon);
		TextView tvLabel = (TextView) convertView.findViewById(R.id.tv_io_name);
		TextView tvValue = (TextView) convertView.findViewById(R.id.tv_io_value);
		ImageButton toggleButton = (ImageButton) convertView.findViewById(R.id.ib_toggle_io);
		toggleButton.setFocusable(false);

		tvLabel.setText(ioObject.getLabel());

		// Check if we want to show the status at all
		if (mShowStatus) {
			// Show switch when it's an output else show values
			if (ioObject.attributeCode.contains(Constants.OUTPUT_CODE_PREFIX)) {
				tvValue.setVisibility(View.GONE);

				if (mSite.areChangesAllowed()) {
					setToggleState(ioObject, toggleButton, true);
				} else {
					setToggleState(ioObject, toggleButton, false);
				}

				registerListener(toggleButton, position, !mSite.canEdit());

			} else if (ioObject.attributeCode.contains(Constants.INPUT_CODE_PREFIX)) {
				toggleButton.setVisibility(View.GONE);
				tvValue.setVisibility(View.VISIBLE);
				if (ioObject.isOpen()) {
					tvValue.setText(mContext.getResources().getString(R.string.io_state_open));
				} else {
					tvValue.setText(mContext.getResources().getString(R.string.io_state_closed));
				}
			} else if (ioObject.attributeCode.equals(Constants.ATTRIBUTE.IO_TEMPERATURE)) {
				toggleButton.setVisibility(View.GONE);
				tvValue.setVisibility(View.VISIBLE);
				tvValue.setText(mContext.getResources().getString(R.string.io_temperature, ioObject.getFloatValue()));
			}
		}/* don't show status */
		else {
			toggleButton.setVisibility(View.GONE);
			tvValue.setVisibility(View.GONE);
		}

		IoExtenderUtils.setPic(IoExtenderUtils.getIOPicturePath(mContext, mSite.getIdSite(), ioObject.attributeCode), iv);

		return convertView;
	}

	/**
	 * Register a listener for the toggle button depending if it is a normal user, demo user or user without phone
	 * number; Show a Toast message when toggle not available
	 *
	 * @param toggleButton
	 *        The toggle button to register the listener for
	 * @param position
	 *        Position of the item in the listview
	 * @param isDemoUser
	 *        Request method as demo user or not, depends on the result it will return (more advanced options visible)
	 */
	private void registerListener(ImageButton toggleButton, final int position, final boolean isDemoUser) {
		toggleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (TextUtils.isEmpty(mSite.getPhonenumber())) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.error_no_phonenumber), Toast.LENGTH_SHORT).show();
				} else if (isDemoUser) {
					Toast.makeText(mContext, mContext.getString(R.string.error_demo_user_message), Toast.LENGTH_SHORT).show();
				} else if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
					Toast.makeText(mContext, mContext.getString(R.string.error_unable_to_send_text), Toast.LENGTH_SHORT).show();
				} else {
					showIoSwitchStatusSmsDialog(mContext, position);
				}
			}
		});
	}

	/**
	 * Set Site object that contains the IO Extender data
	 *
	 * @param pSite
	 *        The Site object that contains the IO Extender data
	 */
	public void setSiteObject(Site pSite) {
		mSite = pSite;
		notifyDataSetChanged();
	}

	@Override
	public Attribute getItem(int position) {
		if (mSite.getIoExtenderData() != null) {
			return mSite.getIoExtenderData().getIoExtenderAtIndex(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Build and show a dialog<br/>
	 *
	 * @param context
	 *        The context needed to show the dialog
	 * @param position
	 *        The position of the ioObject
	 */
	public void showIoSwitchStatusSmsDialog(final Context context, final int position) {
		Attribute ioObject = mSite.getIoExtenderData().getIoExtenderAtIndex(position);
		if (ioObject == null) {
			return;
		}

		final String smsPhoneNumber;
		if (BuildConfig.BUILD_TYPE.equals("debug")) {
			smsPhoneNumber = BuildConfig.DEBUG_PHONE_NUMER;
		} else {
			smsPhoneNumber = mSite.getPhonenumber();
		}
		final String smsCommand = IoExtenderUtils.prepareSmsCommand(mSite, ioObject.attributeCode, !ioObject.isOpen());
		Spanned msgCommand = Html.fromHtml(String.format(mContext.getString(R.string.sms_dialog_message), smsCommand, smsPhoneNumber));

		new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom)).setTitle(context.getString(R.string.sms_dialog_title))
				.setMessage(msgCommand).setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

						boolean originalState = toggleOutputState(position);

						notificationSmsSent(context, smsPhoneNumber, smsCommand, position, originalState);

						// notify linked generator button
						notifyGeneratorChanged(context, position, originalState);
					}
				}).setNegativeButton(R.string.cancel_dialog, null).show();
	}

	/**
	 * The generator button might be attached to one of the outputs, so when the attached IO state changes, the
	 * generator button needs to be changed too, we do this by broadcasting an intent
	 *
	 * @param context
	 *        The context needed to broadcast the intent
	 * @param position
	 *        The position of the ioObject
	 * @param originalState
	 *        The original state of the ioObject
	 */
	private void notifyGeneratorChanged(Context context, int position, boolean originalState) {
		Attribute ioObject = mSite.getIoExtenderData().getIoExtenderAtIndex(position);
		if (ioObject == null) {
			return;
		}

		String generatorIoCode = IoExtenderUtils.getGeneratorIoCode(context, mSite.getIdSite());
		if (!TextUtils.isEmpty(generatorIoCode) && generatorIoCode.equals(ioObject.attributeCode)) {
			Intent i = new Intent(Constants.BROADCAST_TOGGLE_BUTTON);
			i.putExtra(Constants.INTENT_SITE_ID, mSite.getIdSite());
			i.putExtra(Constants.INTENT_IO_INDEX, position);
			i.putExtra(Constants.INTENT_IO_CODE, ioObject.attributeCode);
			i.putExtra(Constants.INTENT_IS_GENERATOR_BTN, true);
			i.putExtra(Constants.INTENT_ORIGINAL_STATE, originalState);
			i.putExtra(Constants.INTENT_FIRED_BY_SMS_BROADCAST, false);
			context.sendBroadcast(i);
		}
	}

	/**
	 * Toggle output button state
	 *
	 * @param position
	 *        The position of the IO button we want to toggle
	 * @return The original state of the ioObject, needed to toggle back the ioObject status/toggle when the sms failed
	 */
	public boolean toggleOutputState(int position) {
		Attribute ioObject = mSite.getIoExtenderData().getIoExtenderAtIndex(position);
		if (ioObject == null) {
			return false;
		}

		boolean originalState = ioObject.isOpen();

		ioObject.setStatus(!originalState);
		notifyDataSetChanged();

		return originalState;
	}

	/**
	 * Send the SMS command and prepare some intents to broadcast when sms sent/delivered
	 *
	 * @param context
	 *        Needed to setup the broadcasts
	 * @param smsPhoneNumber
	 *        The phonenumber to send the sms to
	 * @param smsCommand
	 *        The sms command to send to the phone number
	 * @param position
	 *        The position of the ioObject that get's toggled
	 * @param originalState
	 *        The origin state of the ioObject we toggled
	 */
	private void notificationSmsSent(final Context context, String smsPhoneNumber, String smsCommand, int position, boolean originalState) {
		Attribute ioObject = mSite.getIoExtenderData().getIoExtenderAtIndex(position);
		if (ioObject == null) {
			return;
		}
		IoExtenderUtils.sendSMS(context, smsPhoneNumber, smsCommand, mSite.getIdSite(), position, ioObject.attributeCode, false, originalState);
	}
}
