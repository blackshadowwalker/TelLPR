package com.baiduapi;

import android.content.Context;
import android.widget.Toast;

import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.base.GPS;

public class MySearchListener implements MKSearchListener {    
	
	private static Context mContext=null;
	
	public static void init(Context c){
		mContext = c;
	}
	
	@Override    
	public void onGetAddrResult(MKAddrInfo result, int iError) {    
		//返回地址信息搜索结果    
		if (result == null) {  
			System.out.println("onGetAddrResult :result == null ");
			return;  
		}  
		StringBuffer sb = new StringBuffer();  
		// 经纬度所对应的位置   
		sb.append(result.strAddr).append("");  
	
		BDGPS.setAddress(result.strAddr);
		
		System.out.println("result.strAddr = "+result.strAddr);

		// 判断该地址附近是否有POI（Point of Interest,即兴趣点）   
		if (null != result.poiList) {  
			// 遍历所有的兴趣点信息   
			for (MKPoiInfo poiInfo : result.poiList) {  
				sb.append("----------------------------------------").append("/n");  
				sb.append("名称：").append(poiInfo.name).append("/n");  
				sb.append("地址：").append(poiInfo.address).append("/n");  
				sb.append("经度：").append(poiInfo.pt.getLongitudeE6() / 1000000.0f).append("/n");  
				sb.append("纬度：").append(poiInfo.pt.getLatitudeE6() / 1000000.0f).append("/n");  
				sb.append("电话：").append(poiInfo.phoneNum).append("/n");  
				sb.append("邮编：").append(poiInfo.postCode).append("/n");  
				// poi类型，0：普通点，1：公交站，2：公交线路，3：地铁站，4：地铁线路   
				sb.append("类型：").append(poiInfo.ePoiType).append("/n");  
			}  
		}  
		// 将地址信息、兴趣点信息显示在TextView上   
	//	Toast.makeText(mContext, sb.toString(), Toast.LENGTH_LONG).show();  
	}    
	@Override    
	public void onGetDrivingRouteResult(MKDrivingRouteResult result, int iError) {    
		//返回驾乘路线搜索结果    
	}    
	@Override    
	public void onGetPoiResult(MKPoiResult result, int type, int iError) {    
		//返回poi搜索结果    
	}    
	@Override    
	public void onGetTransitRouteResult(MKTransitRouteResult result, int iError) {    
		//返回公交搜索结果    
	}    
	@Override    
	public void onGetWalkingRouteResult(MKWalkingRouteResult result, int iError) {    
		//返回步行路线搜索结果    
	}    
	@Override        
	public void onGetBusDetailResult(MKBusLineResult result, int iError) {    
		//返回公交车详情信息搜索结果    
	}    
	@Override    
	public void onGetSuggestionResult(MKSuggestionResult result, int iError) {    
		//返回联想词信息搜索结果    
	}  
	@Override   
	public void onGetShareUrlResult(MKShareUrlResult result , int type, int error) {  
		//在此处理短串请求返回结果.   
	}
	@Override
	public void onGetPoiDetailSearchResult(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}   
}  

