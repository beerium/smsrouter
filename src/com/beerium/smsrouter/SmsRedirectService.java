package com.beerium.smsrouter;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Telephony.Sms;
import android.util.Log;

public class SmsRedirectService extends Service {

	private SmsReceiver receiver;
	private IntentFilter filter;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	
	public static boolean IsServiceStarted = false;
	
	@Override
	public void onCreate() {
		preferences = getSharedPreferences("smsrouter", MODE_PRIVATE);
		editor = preferences.edit();

		filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(1000);
		receiver = new SmsReceiver ();
		Log.d("SmsService", "Service Created!");
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String actionStart = getResources().getString(R.string.id_start);
		
		registerReceiver(receiver, filter);
		
		if (intent != null) {
			String action = intent.getAction();
			if (actionStart.equals(action)) {
				Log.d("SmsService", "baseService Started!");
			}
			
			SmsRedirectService.IsServiceStarted = false;
			editor.putBoolean("service_status", SmsRedirectService.IsServiceStarted);
			editor.commit();

		} else {
			SmsRedirectService.IsServiceStarted = preferences.getBoolean("service_status", false);
			Log.d("SmsService", "Service Restarted! status： " + SmsRedirectService.IsServiceStarted);
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent paramIntent) {
		return binder;
	}
	
	private SmsServiceBinder binder = new SmsServiceBinder();
	
	public class SmsServiceBinder extends ISmsRedirect.Stub {
		
		@Override
		public boolean getServiceStatus() {
			return SmsRedirectService.IsServiceStarted;
		}
		
		@Override
		public void startService() {
			registerReceiver(receiver, filter);
			SmsRedirectService.IsServiceStarted = true;
			editor.putBoolean("service_status", SmsRedirectService.IsServiceStarted);
			editor.commit();

			Log.d("SmsService", "Service Started!");
		}
		
		@Override
		public void stopService() {
			if (!SmsRedirectService.IsServiceStarted) return;
			
			unregisterReceiver(receiver);
			SmsRedirectService.IsServiceStarted = false;
			editor.putBoolean("service_status", SmsRedirectService.IsServiceStarted);
			editor.commit();

			Log.d("SmsService", "Service Stopped!");
		}
	}
}
