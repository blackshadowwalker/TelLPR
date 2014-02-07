package com.baiduapi;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.base.GPS;

public class MyLocationListener implements BDLocationListener {

	private static String TAG = "com.baiduapi.MyLocationListener";

	String getError(int error){
		//@返回值：
		switch(error){
		case 61: return "GPS定位结果";
		case 62: return "扫描整合定位依据失败。此时定位结果无效";
		case 63: return "网络异常，没有成功向服务器发起请求。此时定位结果无效";
		case 65: return "定位缓存的结果";
		case 66: return "离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果";
		case 67: return "离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果";
		case 68: return "网络连接失败时，查找本地离线定位时对应的返回结果";
		case 161: return "表示网络定位结果";
		case 162:
		case 167: 
		default:
			return "服务端定位失败";
		}
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		if (location == null){
			System.out.println("onReceiveLocation : location == null ");
			return ;
		}
		BDGPS.setLocation(location);
		if (location.getLocType() == BDLocation.TypeNetWorkLocation){
			Log.i(TAG,"setAddress : "+location.getAddrStr());
			BDGPS.setAddress(location.getAddrStr());
		}
		Log.e(TAG,"error code : "+location.getLocType()+": " + getError(location.getLocType()));
		Log.i(TAG,"latitude : "+location.getLatitude()+"  lontitude: "+location.getLongitude());

		//	Toast.makeText(GPS.getmContext(), getError(location.getLocType()), Toast.LENGTH_LONG).show();
	}


	public void onReceivePoi(BDLocation poiLocation) {
		if (poiLocation == null){
			return ;
		}
		StringBuffer sb = new StringBuffer(256);
		sb.append("Poi time : ");
		sb.append(poiLocation.getTime());
		sb.append("\nerror code : ");
		sb.append(poiLocation.getLocType());
		sb.append("\nlatitude : ");
		sb.append(poiLocation.getLatitude());
		sb.append("\nlontitude : ");
		sb.append(poiLocation.getLongitude());
		sb.append("\nradius : ");
		sb.append(poiLocation.getRadius());
		if (poiLocation.getLocType() == BDLocation.TypeNetWorkLocation){
			sb.append("\naddr : ");
			sb.append(poiLocation.getAddrStr());
		} 
		if(poiLocation.hasPoi()){
			sb.append("\nPoi:");
			sb.append(poiLocation.getPoi());
		}else{             
			sb.append("noPoi information");
		}
		System.out.println(sb.toString());
	}
}