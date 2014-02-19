package com.teleframe.base;

import com.base.CustomExceptionHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Looper;
import android.widget.Toast;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try{
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);   
			NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();   
			NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);   
			NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  

			if ( activeNetInfo != null && activeNetInfo.getState() == State.CONNECTED) {   
				Toast.makeText( context, "网络连接成功[ "+activeNetInfo.getTypeName()+"] ["+ activeNetInfo.getExtraInfo()+"]", Toast.LENGTH_SHORT ).show();  

				boolean connected = false;
				if(wifiInfo!=null && wifiInfo.getState() ==State.CONNECTED) {//判断wifi是否已经连接
					connected = true;
				} 
				else if(wifiInfo!=null && mobNetInfo.getState() == State.CONNECTED) {//判断移动数据是否已经连接
					connected = true;
				}else{
					connected = false;
				}
				if(connected){
					new Thread(){
						@Override  
						public void run() { 
							Looper.prepare();  
							try {
							//	Thread.sleep(10*1000);//
								CustomExceptionHandler.CheckCrashReport();//发送报告
							}catch(Exception e){

							}
							Looper.loop();
						}
					}.start();
				}
			}   
			if( mobNetInfo != null ) {   
				//	Toast.makeText( context, "Mobile Network Type : " + mobNetInfo.getTypeName(), Toast.LENGTH_SHORT ).show();   
			}   

		}catch(Exception e){}
	} 

}
