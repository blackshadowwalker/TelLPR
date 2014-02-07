package com.baiduapi;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.teleframe.teflpr.G;
import com.teleframe.teflpr.MainActivity;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import com.teleframe.teflpr.G;

public class BDGPS {

	private static String TAG= "com.base.GPS";
	private static Context mContext=null;
	private static BDGPS gps = null;
	private static String key = "zUNZPgeP77nGbRrmLzZuUBBt";

	// 定义地图引擎管理类  
	private static BMapManager mBMapManager = null;
	// 定义搜索服务类   
	private static MKSearch mMKSearch; 
	private static LocationClient mLocationClient = null;
	private static BDLocationListener myListener = new MyLocationListener();

	private static BDLocation location = null;
	private static String	  address = "";


	public static void init(Context c, Application app){
		MySearchListener.init(c);
		mContext = c;

		mBMapManager = new BMapManager(app);
		mBMapManager.init(key,null); // MyGeneralListener继承自MKGeneralListener接口
		mBMapManager.start();

		// 初始化MKSearch   
		mMKSearch = new MKSearch();  
		mMKSearch.init(mBMapManager, new  MySearchListener());  

		mLocationClient = new LocationClient(c.getApplicationContext());     //声明LocationClient类
		mLocationClient.setAK(key);
		mLocationClient.registerLocationListener( myListener );    //注册监听函数
		mLocationClient.start();
	}

	public static BDGPS getInstance(){
		if(gps==null)
			gps = new BDGPS();
		return gps;
	}

	public BDGPS(){

	}

	public static void UpdateLocation(){
		LocationManager GpsManager  = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE); 
		Location        location    = GpsManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 
		if(location==null)
			location    = GpsManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); 
		if(location==null){
			LocationClientOption option = new LocationClientOption();
			option.setOpenGps(true);
			option.setAddrType("all");//返回的定位结果包含地址信息
			option.setCoorType("gcj02");//返回的定位结果是百度经纬度,默认值gcj02
			option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms,小于1000（ms）时，采用一次定位模式
			option.disableCache(true);//禁止启用缓存定位
			option.setPoiNumber(0);    //最多返回POI个数   
			//	option.setPoiDistance(1000); //poi查询距离        
			//	option.setPoiExtraInfo(false); //是否需要POI的电话和地址等详细信息      
			mLocationClient.setLocOption(option);

			mLocationClient.start();
			if (mLocationClient == null ){
				Toast.makeText(mContext, "定位服务创建失败!", Toast.LENGTH_LONG).show(); 
				Log.d(TAG, "mLocationClient is null.");
			}
			else if( mLocationClient.isStarted()){
				mLocationClient.requestLocation();
			}else {
				Toast.makeText(mContext, "定位服务没有启动!", Toast.LENGTH_LONG).show(); 
				Log.d("LocSDK3", "mLocationClient is not started.");
			}
		}
	}

	public static void setLocation(BDLocation location) {
		if(location!=null){
			// 将用户输入的经纬度值转换成int类型   
			Log.i(TAG, "request reverseGeocode by location");
			int longitude = (int) location.getLongitude();
			int latitude = (int) location.getLatitude();

			// 查询该经纬度值所对应的地址位置信息   
			mMKSearch.reverseGeocode(new GeoPoint(latitude, longitude));  
		}
		BDGPS.location = location;
	}
	public static BDLocation getLocation() {
		return location;
	}

	public static String getAddress() {
		return address;
	}

	public static void setAddress(String address) {
		if(address!=null && !address.isEmpty())
			BDGPS.address = address;
	}

	public static Context getmContext() {
		return mContext;
	}

	public static void setmContext(Context mContext) {
		BDGPS.mContext = mContext;
	}

}

