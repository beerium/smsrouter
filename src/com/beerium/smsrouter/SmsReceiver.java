package com.beerium.smsrouter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

	private final String TAG = "SmsReceiver";
	SmsManager smsManager = SmsManager.getDefault();
	SharedPreferences preferences;
//	private boolean flag = true;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Object[] pdus = (Object[]) intent.getExtras().get("pdus");
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(), 0);
		preferences = context.getSharedPreferences("smsrouter", Context.MODE_PRIVATE);
		String dest_number = preferences.getString("dest_number", "");
		
		for (Object pdu : pdus) {
			SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
			String content = message.getMessageBody();
			String address = message.getOriginatingAddress();
			Log.d(TAG, "SMS body: " + content);
			Log.d(TAG, "sender: " + address);
			
			String shortNumber = address.length() > 11 ? address.substring(address.length() - 11) : address;
			Log.d(TAG, shortNumber);
			if (shortNumber.equals(dest_number) ){
				if ("@0".equals(content)) {
					abortBroadcast();
					SmsRedirectService.IsServiceStarted = false;
					smsManager.sendTextMessage(dest_number, null, "Redirect Service STOPPED!",  pi, null);
					Log.d(TAG, "Redirect Service STOPPED!");
				} else if ("@1".equals(content)) {
					abortBroadcast();
					if (SmsRedirectService.IsServiceStarted == false)  {
						SmsRedirectService.IsServiceStarted = true;
						
						Intent startIntent = new Intent(context.getResources().getString(R.string.id_start));
						context.startService(startIntent);

						smsManager.sendTextMessage(dest_number, null, "Redirect Service RESTARTED!",  pi, null);
						Log.d(TAG, "Redirect Service RESTARTED!");
					} else {
						smsManager.sendTextMessage(dest_number, null, "Redirect Service already STARTED!",  pi, null);
						Log.d(TAG, "Redirect Service already STARTED!");
					}

				} else {
					smsManager.sendTextMessage(dest_number, null, "<" + address + ">" +  content,  pi, null);
					Log.d(TAG, "Redirect for " + address );
				}
			} else {
				smsManager.sendTextMessage(dest_number, null, "<" + address + ">" +  content,  pi, null);
				Log.d(TAG, "Redirect for " + address);
			}
		}
	}
}
