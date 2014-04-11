package com.beerium.smsrouter;

interface ISmsRedirect {
		boolean getServiceStatus(); 
		void startService();
		void stopService();
}
