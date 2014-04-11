package com.beerium.smsrouter;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity  implements OnClickListener{

	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	private EditText txtDestNumber;
	private String TAG = "MainActivity";

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnStartService:
			try {
				startSmsRedirectService();
				Toast.makeText(this, getResources().getString(R.string.tip_service_started), 2000).show();
				checkServiceStatus();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			break;
			
		case R.id.btnStopService:
			try {
				stopSmsRedirectService();
				Toast.makeText(this, getResources().getString(R.string.tip_service_stoped), 2000).show();
				checkServiceStatus();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			break;
			
		case R.id.btnSave:
			String dest_number = txtDestNumber.getText().toString();
			editor.putString("dest_number", dest_number);
			editor.commit();
			
			PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(), 0);
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(dest_number, null, "TEST SMS!",  pi, null);
			Toast.makeText(this, "TEST SMS sent ... ", 3000).show();
			break;
			
		case R.id.btnStartCallRedirect:
			String command = String.format("tel:**21*%s%s" , preferences.getString("dest_number", ""), Uri.encode("#"));
			Log.d(TAG, "command = " + command);
			Intent startIntent = new Intent("android.intent.action.CALL",
					Uri.parse(command));
			startActivity(startIntent);
			break;
			
		case R.id.btnStopCallRedirect:
			String stopCommand = String.format("tel:%s21%s", Uri.encode("##"),Uri.encode("#"));
			Intent stopIntent = new Intent("android.intent.action.CALL",
					Uri.parse(stopCommand)
			); 
			
			startActivity(stopIntent);

			break;
		}
	}
	
	private void startSmsRedirectService() throws RemoteException {
		binder.startService();
	}
	
	private void stopSmsRedirectService() throws RemoteException {
		binder.stopService();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		preferences = getSharedPreferences("smsrouter", MODE_PRIVATE);
		editor = preferences.edit();
		
		txtDestNumber = (EditText) findViewById(R.id.dest_number);

		String result = preferences.getString("dest_number", "");
		txtDestNumber.setText(result);
		
		Button btnSave = (Button)findViewById(R.id.btnSave);
		btnSave.setOnClickListener(this);
		
		Button btnStartService = (Button)findViewById(R.id.btnStartService);
		btnStartService.setOnClickListener(this);
		
		Button btnStopService = (Button)findViewById(R.id.btnStopService);
		btnStopService.setOnClickListener(this);
		
		Button btnStartCallRedirect = (Button)findViewById(R.id.btnStartCallRedirect);
		btnStartCallRedirect.setOnClickListener(this);

		Button btnStopCallRedirect = (Button)findViewById(R.id.btnStopCallRedirect);
		btnStopCallRedirect.setOnClickListener(this);

		Intent intent = new Intent(this, SmsRedirectService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		
		Intent startIntent = new Intent(getResources().getString(R.string.id_start));
		startService(startIntent);
	}
	
	private ISmsRedirect binder;
	
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = ISmsRedirect.Stub.asInterface(service);
			try {
				checkServiceStatus();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
		}
		
	};

	private void checkServiceStatus() throws RemoteException {
		
		boolean isServiceStarted = binder.getServiceStatus();
		
		TextView lblServiceStatus = (TextView)findViewById(R.id.lblServiceStatus);
		
		if (isServiceStarted) {
			lblServiceStatus.setText(getResources().getString(R.string.tip_service_started));
		} else {
			lblServiceStatus.setText(getResources().getString(R.string.tip_service_stoped));
		}
	}
	

	@Override
	protected void onDestroy() {
		unbindService(conn);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
