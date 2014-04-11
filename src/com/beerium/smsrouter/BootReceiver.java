package com.beerium.smsrouter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		Log.e("BootReceiver", "onReceive");
		Intent startIntent = new Intent(context.getResources().getString(R.string.id_start));
		context.startService(startIntent);
	}
}
