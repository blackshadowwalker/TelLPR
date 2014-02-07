package com.lpr;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.base.ImageDispose;
import com.base.S;
import com.teleframe.teflpr.MainActivity;


public class LprService extends IntentService {

	private static final String TAG = "LprService" ;  
	public static final String ACTION = "com.lpr.LprService";  

	private static Context ctx=null;

	private Bitmap mBitmap=null;
	private String mPhotoPath="";

	public LprService() {
		super(TAG);
	}

	public LprService(String name) {
		super(name);
	}

	public static void init(Context c){
		ctx = c;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TAG, "LprService onBind");  
		return super.onBind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.v(TAG, "LprService onUnbind");  
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "LprService onCreate");  
	}



	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		long id = Thread.currentThread().getId(); 
		Log.v(TAG, "LprService onStart, Thread.id="+id);  
	}

	@Override
	public void onDestroy() {
		stopSelf();
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		long id = Thread.currentThread().getId(); 
		Log.v(TAG, "LprService onHandleIntent. Thread.id="+id);  
		Bundle extras = intent.getExtras();
		if(extras==null || extras.getInt("flag")<1){
			Log.e(TAG, " onStart extras <1 ");
			return ;
		}
		String path =  (String) extras.get("data");
		
		Log.d(TAG, "onHandleIntent");
		S.logcatMemory();
		
		mBitmap = ImageDispose.ImageAdjusted(path);
		
		Log.d(TAG, "ImageDispose.ImageAdjusted");
		S.logcatMemory();
		
		Message msg = Message.obtain();
		
		String str="";
		if(mBitmap.getWidth()*mBitmap.getHeight() >  S.MAX_IMAGE_SIZE){
			if(mBitmap.isRecycled()) 
				mBitmap.recycle();
			str = "图片["+mBitmap.getWidth()+" x "+mBitmap.getHeight()+"]太大，请降低像素到300万!";
			msg.what = S.ACTION_DEFAULT;
		}else{
			Log.d(TAG, "Before StartDetectLPRFromBmp");
			S.logcatMemory();
			
			str = LPR.getInstance().StartDetectLPRFromBmp(mBitmap);
			if(!mBitmap.isRecycled())  
				mBitmap.recycle(); //// Cannot draw recycled bitmaps

			Log.d(TAG, "after StartDetectLPRFromBmp");
			S.logcatMemory();
			
			Log.i(TAG, "车牌:"+str);
			if(str==null || str.isEmpty()){
				str =  ( "未识别");
				msg.what = S.ACTION_DEFAULT;
			}else{
//				str =  "车牌:"+str ;
				msg.what = S.ACTION_LPR_RETURN;
			}
		}
		System.gc();
		
		msg.obj= str;
		
		Log.v(TAG, "msg.obj="+msg.obj);  


		MainActivity.handler.sendMessage(msg);

	}

}
