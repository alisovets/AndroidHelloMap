package alisovets.example.hellogooglemap.dialog;

import alisovets.example.hellogooglemap.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

/**
 * The class to simplify the creation of dialogs
 * @author Alexander Lisovets, 2014
 *
 */
public class DialogCreator {

	/**
	 * creates a dialog with the specified layout and the dismiss listener
	 * @param context
	 * @param layoutResourceId the resource Id of the layout 
	 * @param dismissListener  the dismiss listener
	 * @return the created dialog
	 */
	public static AlertDialog openWaitDialog(Context context, int layoutResourceId, OnDismissListener dismissListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		builder.setView(inflater.inflate(layoutResourceId, null));

		AlertDialog dialog = builder.create();
		dialog.setOnDismissListener(dismissListener);
		if (!((Activity) context).isFinishing()) {
			dialog.show();
		}
		return dialog;
	}

	/**
	 * Creates a dialog with the "OK" button and the title and message determined of the their resource Ids      
	 * @param context
	 * @param titleResourceId - the resource id of the title string
	 * @param messageResourceId - the resource id of the message string
	 */
	public static void messageDialog(final Context context, final int titleResourceId, final int messageResourceId) {
		Handler handler = new Handler();
		handler.post(new Runnable() {

			@Override
			public void run() {
				if (!((Activity) context).isFinishing()) {
					new AlertDialog.Builder(context).setTitle(titleResourceId).setMessage(messageResourceId)
							.setPositiveButton(R.string.ok_button_caption, null).setOnCancelListener(null).show();
				}

			}
		});

	}
	
	/**
	 * Creates a dialog without buttons and with the title and message which is determined of the their resource Ids       
	 * @param context
	 * @param titleResourceId - the resource id of the title string
	 * @param messageResourceId - the resource id of the message string
	 */
	public static void messageDialogNoButton(final Context context, final int titleResourceId, final int messageResourceId) {
		Handler handler = new Handler();
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!((Activity) context).isFinishing()) {
					new AlertDialog.Builder(context).setTitle(titleResourceId).setMessage(messageResourceId).show();
				}
			}
		});

	}
	
	/**
	 * Creates a dialog with view that determines by specified resource Id       
	 * @param activity
	 * @param viewResourceId - the resource id of the view
	 */
	public static void viewDialog(final Activity activity, final int viewResourceId) {		
		LayoutInflater inflater = LayoutInflater.from(activity);
		final View dialogView = inflater.inflate(viewResourceId, null, false);
		
		Handler handler = new Handler();
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!activity.isFinishing()) {
					new AlertDialog.Builder(activity).setView(dialogView).show();
					
				}
			}
		});

	}

	/**
	 * Creates a dialog to select a item 
	 * @param activity
	 * @param titleResourceId
	 * @param arrayResourceId
	 * @param selectOnClickListener
	 */
	public static void selectItemDialog(final Activity activity, final int titleResourceId, final int arrayResourceId, final OnClickListener selectOnClickListener) {
		Handler handler = new Handler();
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!activity.isFinishing()) {
					new AlertDialog.Builder(activity).setTitle(titleResourceId).setItems(arrayResourceId, selectOnClickListener).show();
				}
			}
		});

	}

	/**
	 * 
	 * Creates dialog to report that the network is not available 
	 * The dialog has two buttons: 
	 * "Setting" to open network setting;
	 * "Close" to close activity.
	 * @param context
	 * @param messageResourceId
	 */
	public static void noNetworkChangeSettingOrCloseDialog(final Context context, final int messageResourceId) {
		Handler handler = new Handler();
		handler.post(new Runnable() {

			@Override
			public void run() {
				if (((Activity) context).isFinishing()) {
					return;
				}
				new AlertDialog.Builder(context).setTitle(R.string.no_connection_caption).setMessage(messageResourceId)
						.setPositiveButton(R.string.change_settings_btn_caption, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int whichButton) {
								Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.startActivity(new Intent(intent));

							}
						}).setNegativeButton(R.string.close_application, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int whichButton) {
								((Activity) context).finish();
							}
						}).setOnCancelListener(new DialogInterface.OnCancelListener() {

							@Override
							public void onCancel(DialogInterface dialog) {
								((Activity) context).finish();
							}
						}).show();
			}
		});

	}

	/**
	 * creates the dialog to report about determining location problem
	 * the dialog has two buttons: 
	 * "Setting" to open Location settings;
	 * "Cancel" to close the dialog
	 * @param context
	 * @param titleResource
	 * @param messageResource
	 */
	public static void determinateLocationProblemChangeSettingDialog(final Context context, final int titleResource, final int messageResource) {
		Handler handler = new Handler();
		handler.post(new Runnable() {

			@Override
			public void run() {
				if (((Activity) context).isFinishing()) {
					return;
				}
				new AlertDialog.Builder(context).setTitle(titleResource).setMessage(messageResource)
						.setPositiveButton(R.string.change_settings_btn_caption, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int whichButton) {
								Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.startActivity(new Intent(intent));

							}
						}).setNegativeButton(R.string.cancel_button_caption, null).setOnCancelListener(null).show();
			}
		});
	}

	/**
	 * Create a dialog to propose the user to use the last saved location. 
	 * @param activity - the calling activity
	 * @param yesOnClickListener 
	 */
	public static void determinateLocationProblemUseLastKnownDialog(final Activity activity, final OnClickListener yesOnClickListener) {
		Handler handler = new Handler();
		handler.post(new Runnable() {

			@Override
			public void run() {
				if (activity.isFinishing()) {
					return;
				}
				new AlertDialog.Builder(activity).setTitle(R.string.failed_to_get_current_location)
						.setMessage(R.string.use_last_known_location_quest).setPositiveButton(R.string.yes_button_caption, yesOnClickListener)
						.setNegativeButton(R.string.no_button_caption, null).setOnCancelListener(null).show();
			}
		});
	}

	
	/**
	 * Creates a dialog to ask a question the user and process user's positive response
	 * @param activity
	 * @param titleResource
	 * @param questionResource
	 * @param yesOnClickListener
	 */
	public static void questionYesCancelDialog(final Activity activity, final int titleResource, final int questionResource,
			final OnClickListener yesOnClickListener) {
		Handler handler = new Handler();
		handler.post(new Runnable() {

			@Override
			public void run() {
				if (activity.isFinishing()) {
					return;
				}
				new AlertDialog.Builder(activity).setTitle(titleResource).setMessage(questionResource)
						.setPositiveButton(R.string.yes_button_caption, yesOnClickListener).setNegativeButton(R.string.cancel_button_caption, null)
						.setOnCancelListener(null).show();
			}
		});
	}

}
