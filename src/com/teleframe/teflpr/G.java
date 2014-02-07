package com.teleframe.teflpr;

import com.teleframe.teflpr.BuildConfig;

import android.app.Application;
import android.content.Context;

public class G extends Application {

	private static Context context=null;
	private static Application app=null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		context = this.getApplicationContext();
	}

	public static Context getContext() {
		return context;
	}

	public static void setContext(Context context) {
		G.context = context;
	}

}
