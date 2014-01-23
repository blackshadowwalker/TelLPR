package com.teleframe.teflpr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.base.CustomExceptionHandler;
import com.base.ImageDispose;
import com.base.Util;
import com.lpr.LPR;
import com.lpr.LprService;

import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.base.*;

public class MainActivity extends Activity {

	private static String  TAG = "Teleframe.TELLPR.MainActivity";

	private static Toast toast;
	public static Handler handler;

	private Intent mIntent;
	private Bitmap mBitmap=null;
	private TextView mTextView;
	private Button mButton;  
	private Button mbtBrowser;  
	private ImageView imageView;  
	private File mPhotoFile;  
	private String mPhotoPath;  
	private LPR lprHandel = new LPR();
	private static short[] pixs = null;
	private static int[] pix = null;
	private String tempString = "";

	public void showInfo(String str){
		toast = Toast.makeText(this.getApplicationContext(), str, Toast.LENGTH_LONG);
		if(toast!=null){
			toast.setGravity(Gravity.CENTER, 0, 0); 
			toast.show();
		}
		if(mTextView!=null)
			mTextView.setText(str);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.karl_main);

		ImageDispose.init(this,  getWindowManager());

		/*
		File f = Util.GetLogFile();
		if(f!=null)
			Debug.startMethodTracing(f.getAbsolutePath()); //开启跟踪
		else
			Debug.startMethodTracing(Util.COMPANY_NAME+"-"+Util.PRODUCTION_NAME); //开启跟踪
		 */
		Debug.startNativeTracing();
		BaseInit.init(this);

		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what){
				case S.ACTION_LPR_RETURN:
					Log.e(TAG, "handleMessage:"+(String) msg.obj);
					break;
				default:
					break;
				}
			}

		};

		mIntent  =  getIntent();  

		mTextView = (TextView) findViewById(R.id.textView);
		imageView = (ImageView) findViewById(R.id.imageView);
		mButton = (Button) findViewById(R.id.button);  
		mbtBrowser = (Button) findViewById(R.id.btbrowser);  

		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);  
		MemoryInfo mi = new MemoryInfo();  
		am.getMemoryInfo(mi);
		int memClass = am.getMemoryClass();
		Log.e(TAG, "memClass="+memClass);
		int maxsize =  S.MAX_BYTE /1024/1024;
		if(memClass <  maxsize * 0.8){
			tempString = "您的手机内存限制["+memClass+"M]，不足以运行车牌识别所需["+maxsize+"M*80%]";
			Log.e(TAG, tempString);
			showInfo(tempString);
			try {
				throw new Exception("tempString");
			} catch (Exception e) {
				e.printStackTrace();
			}
			mButton.setEnabled(false);
			mbtBrowser.setEnabled(false);
		}else if(mi.availMem < S.MAX_BYTE) {
			Log.e(TAG, "failed to new memory! mi.availMem="+mi.availMem);
			tempString = "您的手机内存已不足！"; 
			showInfo(tempString);
			System.gc();
			mButton.setEnabled(false);
			mbtBrowser.setEnabled(false);
		}

		mButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				bindService(new Intent(LprService.ACTION), conn, BIND_AUTO_CREATE);
				try {  
					Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");  //
					//	intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);  //with  MediaStore.ACTION_IMAGE_CAPTURE 
					mPhotoFile = Util.GetImageFile();
					if(mPhotoFile==null){
						showInfo( "无发写入图片");
						return;
					}
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));  
					startActivityForResult(intent, S.RESULT_LOAD_CAMERA_IMAGE);  
				} catch (Exception e) {  
					Log.e(TAG, "MediaStore.ACTION_IMAGE_CAPTURE  error ");
					e.printStackTrace();
				}  
			}
		} );  

		mbtBrowser.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				
				//选择照片的时候也一样，我们用Action为Intent.ACTION_GET_CONTENT，  
				//有些人使用其他的Action但我发现在有些机子中会出问题，所以优先选择这个 
				//Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				Intent intent = new Intent();  
				intent.setType("image/*");  
				intent.setAction(Intent.ACTION_GET_CONTENT);  
				startActivityForResult(intent, S.RESULT_LOAD_IMAGE);
			}
		});

	}

	public static Handler getHandler(){
		return handler;
	}

	ServiceConnection conn = new ServiceConnection() {  
		public void onServiceConnected(ComponentName name, IBinder service) {  
			Log.v(TAG, "onServiceConnected");  
		}  
		public void onServiceDisconnected(ComponentName name) {  
			Log.v(TAG, "onServiceDisconnected");  
		}  
	};  

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	public boolean JudgeImage(Bitmap bmp){
		if(bmp.getWidth()*bmp.getHeight() > S.MAX_SIZE){
			showInfo("像素太高，请降低摄像头的像素到300万以下!");
			if(bmp.isRecycled()) 
				bmp.recycle();
		}
		return true;
	}

	public void StartDetectLPRFromFile(Bitmap rawBitmap){

		if(mBitmap==null){
			return;
		}
		if(JudgeImage(rawBitmap)==false)
			return;

		int rawHeight = rawBitmap.getHeight(); 
		int rawWidth = rawBitmap.getWidth(); 
		int size = rawHeight * rawWidth;
		if(size*4 > S.MAX_BYTE){
			showInfo("图片太大，请降低像素到200万!");
			return ;
		}

		if(pix == null)
			pix = new int[size];
		if(pixs == null)
			pixs = new short[size*3];

		Log.d(TAG, "get pixes !");
		rawBitmap.getPixels( pix, 0, rawWidth, 0, 0, rawWidth, rawHeight); 
		//	if(!rawBitmap.isRecycled())  rawBitmap.recycle(); //// Cannot draw recycled bitmaps
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
				showInfo( "未识别");
			else
				showInfo( "车牌:"+str );
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(resultCode == RESULT_CANCELED)
			return;

		if(this==null || MainActivity.class==null){
			Log.e(TAG, "onActivityResult  this==null");
			android.os.Process.killProcess(android.os.Process.myPid());  
			System.exit(1);  
			return;
		}

		if(resultCode == RESULT_OK){
			switch(requestCode){
			case S.RESULT_LOAD_IMAGE:
				if(data!=null){
					if (data.getExtras() != null)  
						mIntent.putExtras(data.getExtras());  
					if (data.getData()!= null)  
						mIntent.setData(data.getData());  
					Log.d(TAG, "onActivityResult  RESULT_LOAD_IMAGE");
					processImage(S.RESULT_LOAD_IMAGE, resultCode, mIntent);  
				}
				break;
			case S.RESULT_LOAD_CAMERA_IMAGE:
				Log.d(TAG, "onActivityResult  RESULT_LOAD_CAMERA_IMAGE");
				if(mPhotoFile!=null)
					mPhotoPath  = mPhotoFile.getPath() ;
				processImage(S.RESULT_LOAD_CAMERA_IMAGE, resultCode, mIntent);  
				break;
			default:
				Intent IntentParent = getIntent();
				setResult (RESULT_OK, IntentParent);
				super.onActivityResult(requestCode, resultCode, data);
			}
		}
	}
	private void processImage(int requestCode, int resultCode, Intent data){
		if(mBitmap!=null && !mBitmap.isRecycled()){
			mBitmap.recycle();
			System.gc();//提醒系统及时回收
		}
		switch (requestCode) {  
		case S.RESULT_LOAD_IMAGE:
			if (data != null) {  
				//取得返回的Uri,基本上选择照片的时候返回的是以Uri形式，但是在拍照中有得机子呢Uri是空的，所以要特别注意  
				Uri mImageCaptureUri = data.getData();  
				
				Intent lprService = new Intent(LprService.ACTION);
				lprService.setFlags(S.RESULT_LOAD_IMAGE);
				lprService.setData(mImageCaptureUri);
				startService(lprService);  
				
				if (mImageCaptureUri != null) {  
					try {  
						//方法1    这个方法是根据Uri获取Bitmap图片的静态方法  
						//	mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageCaptureUri); //大图片会死掉
						//方法2  //大图片会死掉
						//mPhotoPath = ImageDispose.getFilePathFromContentUri(mImageCaptureUri, this.getContentResolver());
						//mBitmap = BitmapFactory.decodeFile(mPhotoPath, null); 
						mBitmap = ImageDispose.ImageAdjusted(mImageCaptureUri);
						if(mBitmap!=null)
							imageView.setImageBitmap(mBitmap);  
					} catch (Exception e) {  
						e.printStackTrace();  
					}  
				} else {  
					//返回的Uri不为空时，那么图片信息数据都会在Uri中获得。如果为空，那么我们就进行下面的方式获取  
					Bundle extras = data.getExtras();  
					if (extras != null) {  
						//这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片  
						mBitmap = extras.getParcelable("data");  
						if (mBitmap != null) {  
							Log.d(TAG, "LOGAD Bundle Bitmap");
							imageView.setImageBitmap(mBitmap); 
						}
					}  
				} 
				
				Log.d(TAG,"Thread StartDetectLPRFromFile 正在识别...");
			//	StartDetectLPRFromFile(mBitmap);
			}  
			break;  
		case S.RESULT_LOAD_CAMERA_IMAGE:
			
			Uri uri = Uri.parse("content:"+mPhotoPath);
			
			Intent lprService = new Intent(LprService.ACTION);
			lprService.setFlags(S.RESULT_LOAD_IMAGE);
			lprService.setData(uri);
			startService(lprService);  
			
			mBitmap = ImageDispose.ImageAdjusted(mPhotoPath);
			if(mBitmap!=null)
				imageView.setImageBitmap(mBitmap);  
			else
				Log.d(TAG, "RESULT_LOAD_CAMERA_IMAGE  mBitmap == null");
			
			
		//	StartDetectLPRFromFile(mBitmap);
			
			break;
		default :
			break;
		}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		//	Debug.stopMethodTracing();
		unbindService(conn);  
		Debug.stopNativeTracing();
	}

	public void About(){
		PackageManager pm = getPackageManager();//context为当前Activity上下文 
		PackageInfo pi = null;
		try {
			pi = pm.getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}
		String msg= "信帧电子技术(北京)有限公司";
		if(pi!=null){
			int versionCode = pi.versionCode;
			String versionName = pi.versionName;
			msg += "\n" +"版本:"+versionName;
		}
		Builder alertDialog = new AlertDialog.Builder(this); 
		alertDialog.setTitle("关于");
		alertDialog.setIcon(R.drawable.ic_launcher);
		alertDialog.setMessage(msg); 
		alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//
			}
		});
		alertDialog.create(); 
		alertDialog.show(); 
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch(item.getItemId()){
		case R.id.about:
			About();
			break;
		case R.id.exit:
			android.os.Process.killProcess(android.os.Process.myPid());  
			System.exit(1);  
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}  

}