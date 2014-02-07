package com.base;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;

public class GPS {

	private static Context mContext=null;
	private static GPS gps = null;

	public static void init(Context c, Application app){
		mContext = c;
	}

	public static GPS getInstance(){
		if(gps==null)
			gps = new GPS();
		return gps;
	}

	public GPS(){

	}

	public static boolean checkGPS(){
		LocationManager locationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public static Location getLocation(){
		LocationManager GpsManager  = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE); 
		Location        location    = GpsManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 
		if(location==null)
			location    = GpsManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); 
		return location;
	}

	// Wifi是否可用  
	private boolean IsWifiEnable() {  
		WifiManager wifiManager = (WifiManager) mContext  
				.getSystemService(Context.WIFI_SERVICE);  
		return wifiManager.isWifiEnabled();  
	}  

}
