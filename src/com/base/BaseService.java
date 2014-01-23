package com.base;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BaseService extends Service {
	private static final String TAG = "BaseService" ;  
	public static final String ACTION = "com.base.BaseService";  

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TAG, "BaseService onBind");  
		return null;
	}
	@Override
	public boolean onUnbind(Intent intent) {
		Log.v(TAG, "BaseService onUnbind");  
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		Log.v(TAG, "BaseService onCreate");  
		super.onCreate();
	}

	@Override
	public void onRebind(Intent intent) {
		Log.v(TAG, "BaseService onRebind");  
		super.onRebind(intent);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.v(TAG, "BaseService onStart");  
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "BaseService onStartCommand");  
		return super.onStartCommand(intent, flags, startId);
	}

	

}
