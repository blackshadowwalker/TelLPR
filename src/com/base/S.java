package com.base;

import android.os.Debug;
import android.util.Log;

public class S {
	private static String TAG=S.class.getPackage()+".S";
	
	public static final int RESULT_LOAD_IMAGE = 301;
	public static final int RESULT_LOAD_CAMERA_IMAGE = 302;
	
	public static final int  IMAGE_MIN_HEIGHT= 1920;
	public static final int  IMAGE_MIN_WIDHT = 1080;
	public static final int MIN_IMAGE_SIZE = IMAGE_MIN_WIDHT*IMAGE_MIN_HEIGHT;//图片最小尺寸 130W
	public static final int MAX_IMAGE_SIZE = 2048*1536;//图片最大尺寸 300W
	public static final int MAX_BYTE = MAX_IMAGE_SIZE * 2 * 3 + MAX_IMAGE_SIZE * 4 ;
	
	//action 
	public static final int ACTION_DEFAULT = 500;
	public static final int ACTION_LPR_RETURN = 501;
	public static final int ACTION_SHOW_IMAGE = 502;
	public static final int ACTION_UPDATE_CHECK = 503;
	public static final int ACTION_UPDATE = 504;
	public static final int ACTION_CHECK_CRASH = 505;
	
	//Email
	public static String EMAIL_SERVER_ADDRESS = "crash_service@126.com";
	
	public static String EMAIL_HOST = "smtp.126.com";
	public static String EMAIL_FROM = "crash_client@126.com";
	public static String EMAIL_USERNAME = "crash_client";
	public static String EMAIL_PASSWORD = "karl.li";
	public static String EMAIL_TO = EMAIL_SERVER_ADDRESS;
	
	//
	public static String SERVER_URL = "http://teleframe.xicp.net";
	
	public static void logcatMemory(){
		android.os.Debug.MemoryInfo mi = new android.os.Debug.MemoryInfo();
		Debug.getMemoryInfo(mi);
		Log.d(TAG, " mi.dalvikPss="+mi.dalvikPss);
		Log.d(TAG, " mi.nativePss="+mi.nativePss);
		Log.d(TAG, " mi.otherPss="+mi.otherPss);
		Log.d(TAG, " mi.getTotalPss="+mi.getTotalPss());
	}
	
	
}
