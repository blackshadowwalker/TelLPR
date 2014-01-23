package com.lpr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.base.BaseService;
import com.base.ImageDispose;
import com.base.S;
import com.teleframe.teflpr.MainActivity;


public class LprService extends BaseService {

	private static final String TAG = "LprService" ;  
	public static final String ACTION = "com.lpr.LprService";  

	private static short[] pixs = null;
	private static int[] pix = null;

	private LPR lprHandel = new LPR();

	private Context ctx=null;

	private Bitmap mBitmap=null;
	private String mPhotoPath="";


	public void init(Context c){
		this.ctx = c;
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
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "LprService onStartCommand");  
		return super.onStartCommand(intent, flags, startId);
	}

	
	@Override
	public void onStart(Intent intent, int startId) {
	//	super.onStart(intent, startId);
		Log.v(TAG, "LprService onStart");  

		if(intent.getFlags() == S.RESULT_LOAD_IMAGE){
			mBitmap = ImageDispose.ImageAdjusted(intent.getData());
		}else{
			Bundle bundle=intent.getExtras();
			Log.v(TAG, "flag= " +bundle.get("flag"));

			mPhotoPath =  (String) bundle.get("data");
			mBitmap = ImageDispose.ImageAdjusted(mPhotoPath);
		}
		
		Message msg = Message.obtain();
		msg.obj= StartDetectLPRFromFile(mBitmap);
		msg.what = S.ACTION_LPR_RETURN;
		MainActivity.getHandler().handleMessage(msg);
	}

	public String StartDetectLPRFromFile(Bitmap rawBitmap){

		if(mBitmap==null){
			return "";
		}

		int rawHeight = rawBitmap.getHeight(); 
		int rawWidth = rawBitmap.getWidth(); 
		int size = rawHeight * rawWidth;
		if(size*4 > S.MAX_BYTE){
			return "图片太大，请降低像素到200万!";
		}

		if(pix == null)
			pix = new int[size];
		if(pixs == null)
			pixs = new short[size*3];

		Log.d(TAG, "get pixes !");
		rawBitmap.getPixels( pix, 0, rawWidth, 0, 0, rawWidth, rawHeight); 
		if(!rawBitmap.isRecycled())  rawBitmap.recycle(); //// Cannot draw recycled bitmaps
		for(int i=0,j=0; i<size; i++)
		{
			pixs[j++] = (short) ((pix[i] & 0x00FF0000) >> 16);
			pixs[j++] = (short) ((pix[i] & 0x0000FF00) >> 8);
			pixs[j++] = (short) ((pix[i] & 0x000000FF) >> 0);
		}
		pix = null;

		String str = null;
		try{
			Log.d(TAG, "call  DetectLPR ");
			byte test[] = lprHandel.DetectLPR(pixs, rawWidth, rawHeight, S.MAX_BYTE);
			//	byte test[]={(byte)0xd4,(byte) 0xa5,(byte)0x41,(byte)0x42};
			pixs = null;
			str = new String(test,"GB2312");
			Log.d(TAG, "callback of  DetectLPR ");
		}catch(Exception e){
			Log.e(TAG, "Detect LPR error!");
			e.printStackTrace();
		}
		if(str!=null){
			Log.i(TAG, "车牌:"+str);
			if(str.trim().equals(""))
				return ( "未识别");
			else
				return ( "车牌:"+str );
		}
		return "";
	}


	@Override
	public void onRebind(Intent intent) {
		Log.v(TAG, "LprService onRebind");  
		super.onRebind(intent);
	}

}
