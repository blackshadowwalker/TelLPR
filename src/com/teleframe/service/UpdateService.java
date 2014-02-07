package com.teleframe.service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.base.GPS;
import com.base.S;
import com.base.SIMCardInfo;
import com.teleframe.teflpr.MainActivity;
import com.teleframe.teflpr.R;

import android.app.AlertDialog;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class UpdateService extends IntentService{

	private static final String TAG = "UpdateService" ;  
	public static final String ACTION = "com.teleframe.service.UpdateService";  
	static String path = "";

	private static Context mContext=null;
	AlertDialog dialog =null;

	public UpdateService() {
		super(TAG);
	}

	public UpdateService(String name) {
		super(name);
	}

	public static void init(Context c){
		mContext = c;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		long id = Thread.currentThread().getId(); 
		Log.v(TAG, "UpdateService onStart, Thread.id="+id);  
	}

	public static JSONObject checkVersion(){
		JSONObject json = null;

		int NewVersionCode = 0;
		String  NewVersionName = "";
		String description="";
		String apkUrl = "";

		try {
			SIMCardInfo siminfo = SIMCardInfo.getInstance();
			
			PackageManager pm = mContext.getPackageManager();//context为当前Activity上下文 
			PackageInfo pi = null;
			try {
				pi = pm.getPackageInfo(mContext.getPackageName(), 0);
			} catch (NameNotFoundException e1) {
				e1.printStackTrace();
			}
			String version="";
			if(pi!=null)
				version = pi.versionName;
			
			Location location = GPS.getLocation();
			String strLati="";
			String strLong="";
			if (location!=null) {    
				strLati = Double.toString(location.getLatitude());    
                strLong = Double.toString(location.getLongitude()); 
			}
			path = S.SERVER_URL+"/update/?update=true&app_name=TelLPR"+
			 	"&phone="+siminfo.getNativePhoneNumber()+
				"&latitude="+strLati+"&longitude="+strLong+"&version="+version;
			
			Log.i(TAG, "Update Path = " +path);
			
			URL url = new URL(path);
			HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(3000);//设置连接主机超时（单位：毫秒）
			conn.setReadTimeout(3000);//设置从主机读取数据超时（单位：毫秒）
			conn.connect();  
			// 获取文件大小  
			int length = conn.getContentLength();  
			InputStream is = conn.getInputStream(); 
			BufferedInputStream bis = new BufferedInputStream(is); 
			byte buffer[] = new byte[1024];  
			String msg = "";
			String temp;
			int len=-1;
			while((len =bis.read(buffer))!=-1){ 
				temp = new String(buffer,"UTF-8");
				msg += temp;
			}
			System.out.println(msg.trim());
			json = new  JSONObject(msg.trim());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "UpdateService onHandleIntent ...");

		JSONObject json = checkVersion();
		
		Message msg = Message.obtain();
		if(json!=null){
			msg.obj= json;
			msg.what = S.ACTION_UPDATE;
			MainActivity.handler.sendMessage(msg);
		}else{
			msg.obj= "连接服务器失败!";
			msg.what = S.ACTION_DEFAULT;
			MainActivity.handler.sendMessage(msg);
		}
	}

}
