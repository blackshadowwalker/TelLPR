package com.teleframe.teflpr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.MessagingException;

import org.json.JSONException;
import org.json.JSONObject;

import com.base.BaseExceptionHandler;
import com.base.EmailUtil;
import com.base.GPS;
import com.base.ImageDispose;
import com.base.Util;
import com.lpr.LPR;
import com.lpr.LprService;

import android.location.Location;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Movie;
import android.graphics.drawable.ColorDrawable;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.base.*;
import com.teleframe.service.UpdateService;
import com.teleframe.view.*;

public class MainActivity extends Activity {

	private static String  TAG = "Teleframe.TELLPR.MainActivity";
	private static Context mContext = null;

	Thread mThread=null;

	private static Toast toast;
	public static Handler handler;
	Intent lprServiceIntent = null;
	Intent updateServiceIntent = null;

	ProgressDialog loading = null; 

	private Intent mIntent;
	private Bitmap mBitmap=null;
	private TextView mTextView;
	private TextView mTextViewGPS;
	private Button mButton;  
	private Button mbtBrowser;  
	private ImageView imageView;  
	private File mPhotoFile;  
	private String mPhotoPath;  
	private String tempString = "";
	private String mImagePath="";
	private String mPlate="";

	public void showInfo(String str){
		toast = Toast.makeText(this.getApplicationContext(), str, Toast.LENGTH_LONG);
		if(toast!=null){
			toast.setGravity(Gravity.CENTER, 0, 0); 
			toast.show();
		}
		if(mTextView!=null)
			mTextView.setText(str);
	}
	public void  toast(String str){
		Toast.makeText(mContext.getApplicationContext(), str, Toast.LENGTH_LONG).show();
	}
	public void  alert(String title, String msg){
		Builder alertDialog = new AlertDialog.Builder(mContext); 
		alertDialog.setTitle(title);
		alertDialog.setIcon(R.drawable.ic_launcher);
		alertDialog.setMessage(msg); 
		alertDialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		alertDialog.create(); 
		alertDialog.show(); 
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.karl_main);
		mContext = this;

		ImageDispose.init(this,  getWindowManager());
		lprServiceIntent = new Intent(this, LprService.class);
		/*
		File f = Util.GetLogFile();
		if(f!=null)
			Debug.startMethodTracing(f.getAbsolutePath()); //开启跟踪
		else
			Debug.startMethodTracing(Util.COMPANY_NAME+"-"+Util.PRODUCTION_NAME); //开启跟踪
		 */
		//Debug.startNativeTracing();
		BaseInit.init(this, this.getApplication());

		updateServiceIntent = new Intent(this, UpdateService.class);
		//	startService(updateServiceIntent);  

		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				mPlate="";
				switch(msg.what){
				case S.ACTION_LPR_RETURN:
					//车牌识别返回结果显示，from LprService
					if(loading!=null)
						loading.dismiss();
					String lpr = (String) msg.obj;
					Log.i(TAG, "handleMessage:"+lpr);
					if(loading!=null)
						loading.dismiss();
					mPlate = lpr;
					showInfo("车牌："+lpr);
					SavePlateToFile();//保存车牌到文件
					Location location = GPS.getLocation();
					if(location!=null)
						mTextViewGPS.setText("经度:"+location.getLatitude()+" 纬度:"+location.getLongitude());
					else if(GPS.checkGPS()==false)
						mTextViewGPS.setText("GPS未开启");
					else
						mTextViewGPS.setText("获取经纬度失败!");
					break;
				case S.ACTION_SHOW_IMAGE:
					//显示图片
					if(mBitmap!=null && !mBitmap.isRecycled()){
						mBitmap.recycle();
					}
					mImagePath = (String)msg.obj;
					mBitmap = ImageDispose.ImageAdjustedDisplay(mImagePath, true);
					Log.e(TAG, (String) msg.obj);
					if(mBitmap!=null)
						imageView.setImageBitmap(mBitmap);
					Log.d(TAG, "显示图片imageView.setImageBitmap(mBitmap);");
					S.logcatMemory();
					break;
				case S.ACTION_UPDATE:
					if(loading!=null)
						loading.dismiss();
					Update(msg);
					break;
				case S.ACTION_CHECK_CRASH: // 检测 崩溃日志
					new Thread(){
						@Override
						public void run() {
							Looper.prepare();
							try {
								BaseExceptionHandler.CheckCrashReport();
								CheckUpdate();
							} catch (Exception e) {
								e.printStackTrace();
							}
							Looper.loop();
						}
					}.start();
					break;
				case S.ACTION_DEFAULT:
					if(loading!=null)
						loading.dismiss();
					if(msg.obj instanceof String){
						String msginfo = (String)msg.obj;
						showInfo(msginfo);
					}
					break;
				default:
					if(loading!=null)
						loading.dismiss();
					if(msg.obj instanceof String){
						String msginfo = (String)msg.obj;
						if(msginfo!=null || !msginfo.isEmpty())
							Toast.makeText(mContext, msginfo, Toast.LENGTH_LONG).show();
						//	showInfo(msginfo);
					}
					break;
				}
			}
		};

		mIntent  =  getIntent();  

		mTextView = (TextView) findViewById(R.id.textView);
		mTextViewGPS = (TextView) findViewById(R.id.textViewGPS);
		imageView = (ImageView) findViewById(R.id.imageView);
		mButton = (Button) findViewById(R.id.button);  
		mbtBrowser = (Button) findViewById(R.id.btbrowser);  

		CheckMemery();

		imageView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(mImagePath==null || mImagePath.isEmpty()==true)
					return;
				Intent intent = new Intent();  
				intent.setClass(MainActivity.this, WebActivity.class);
				intent.putExtra("url", mImagePath);
				startActivity(intent);
			}
		});
		imageView.setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View arg0) {
				//	SavePlateToFile();
				Display currentDisplay = getWindowManager().getDefaultDisplay();
				int dw = currentDisplay.getWidth(); 
				int dh = currentDisplay.getHeight(); 
				String blank = "";
				for(int i=0; i<dw; i++)
					blank +=" ";
				Toast toast = Toast.makeText(mContext,blank, Toast.LENGTH_LONG);
				LinearLayout toastView = (LinearLayout) toast.getView();
				MyCustomView mView = new MyCustomView(mContext);  
				//	toast.setGravity(Gravity.CENTER_VERTICAL,500, 500);
				toastView.setGravity(Gravity.CENTER_VERTICAL);
				mView.setX(dw/2-100);
				mView.setY(dh/2-100);
				//	toast.setView(mView);
				toastView.addView(mView, 1);
				toast.show();
				return true;
			}

		});

		mButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				try {  
					Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");  //
					//	intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);  //with  MediaStore.ACTION_IMAGE_CAPTURE 
					mPhotoFile = Util.GetImageFile();
					if(mPhotoFile==null){
						showInfo( "无发写入图片");
						return;
					}
					intent.putExtra(MediaStore.EXTRA_OUTPUT,  Uri.fromFile(mPhotoFile));  
					startActivityForResult(intent, S.RESULT_LOAD_CAMERA_IMAGE);  
				} catch (Exception e) {  
					Log.e(TAG, "MediaStore.ACTION_IMAGE_CAPTURE  error ");
					e.printStackTrace();
				}  
			}
		} );  

		mbtBrowser.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				//1. Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				//2. 选择照片的时候也一样，我们用Action为Intent.ACTION_GET_CONTENT，  
				//有些人使用其他的Action但我发现在有些机子中会出问题，所以优先选择这个 
				Intent intent = new Intent();  
				intent.setType("image/*");  
				intent.setAction(Intent.ACTION_GET_CONTENT);  
				startActivityForResult(intent, S.RESULT_LOAD_IMAGE);
			}
		});

		Message msg = Message.obtain();
		msg.what = S.ACTION_CHECK_CRASH;
		handler.handleMessage(msg);

	}

	public void SavePlateToFile(){
		if(mPlate!=null && !mPlate.isEmpty() &&
				mImagePath!=null && !mImagePath.isEmpty()){

			String newPath = Util.GetPlateDir();
			File f = new File(newPath);
			if(f.exists()==false)
				f.mkdirs();

			Location location = GPS.getLocation();
			String gps = null;
			if(location!=null)
				gps = "latitude="+location.getLatitude()+"&longitude="+location.getLongitude();

			newPath  += "/plate="+mPlate;
			if(gps!=null)
				newPath += "&"+gps; 

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hhmmss");
			newPath += "&time="+sdf.format(new Date());
			newPath += ".jpg";
			Log.i(TAG, "plate :" +newPath);
			File targetFile = new File(newPath);
			try {
				targetFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			File sourceFile = new File(mImagePath);
			if(sourceFile.exists()){
				try {
					FileUtil.copyFile(sourceFile, targetFile);
					toast("文件成功保存到车牌库");
				} catch (IOException e) {
					e.printStackTrace();
					toast("文件保存失败,文件不存在");
				}
			}else{
				Log.i(TAG, "sourceFile is not exists :" + mImagePath);
			}
		}else{
			Log.i(TAG, "plate ==null");
		}

	}

	public void CheckUpdate(){
		JSONObject json = UpdateService.checkVersion();
		if(json==null){
			Log.e(TAG, "CheckUpdate json==null");
			return; 
		}
		int NewVersionCode = 0;
		try {
			NewVersionCode = (int) json.getLong("versionCode");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		PackageManager pm = getPackageManager();//context为当前Activity上下文 
		PackageInfo pi = null;
		try {
			pi = pm.getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}
		if(NewVersionCode>pi.versionCode)
			startService(updateServiceIntent); 
	}

	private void Update(Message msg) {
		if( !(msg.obj instanceof JSONObject ))
			return;
		JSONObject json = (JSONObject) msg.obj;

		PackageManager pm = getPackageManager();//context为当前Activity上下文 
		PackageInfo pi = null;
		try {
			pi = pm.getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}

		int NewVersionCode = 0;
		try {
			NewVersionCode = (int) json.getLong("versionCode");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String apkUrl="";
		String NewVersionName="";
		String description="";
		try {
			apkUrl = json.getString("url");
			description = json.getString("description");
			NewVersionName = json.getString("versionName");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if(NewVersionName==null)
			NewVersionName = "";
		if(description==null)
			description = "";

		final String downloadurl = apkUrl;
		String msg1= "信帧电子技术(北京)有限公司\n\n";
		//	msg1 += "车牌识别\n\n";
		boolean bGetNew = false;
		if(pi!=null){
			if(NewVersionCode>pi.versionCode){
				int versionCode = pi.versionCode;
				String versionName = pi.versionName;
				msg1 += "检测到新版本!\n\n";
				msg1 += "当前版本: V"+versionName+ "\n";
				msg1 += "最新版本: V"+NewVersionName+"\n";
				description = description.replaceAll("\\\\n", "\n");
				msg1 += "更新内容: \n"+ description +"\n";
				bGetNew = true;
			}else{
				msg1 += "您的已是最新版本:"+pi.versionName+"\n";
			}
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);  

		builder.setTitle("版本更新");
		builder.setIcon(R.drawable.ic_launcher);
		builder.setMessage(msg1); 
		if(bGetNew){
			builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadurl));
					startActivity(it);
					dialog.dismiss();
				}
			});
		}
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		//	dialog.setCanceledOnTouchOutside(false);  //点击外面区域不会让dialog消失  
		//	dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG));
		dialog.show();

	}

	/**
	 * 检查手机内存和内存限制是否足够
	 */
	public boolean CheckMemery(){
		boolean ret = true;

		android.os.Debug.MemoryInfo mi = new android.os.Debug.MemoryInfo();
		Debug.getMemoryInfo(mi);

		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);  
		MemoryInfo mi2 = new MemoryInfo();  
		am.getMemoryInfo(mi2);
		int memClass = am.getMemoryClass();
		Log.e(TAG, "memClass="+memClass);
		int maxsize =  ( S.MAX_BYTE + mi.dalvikPss*1024)/1024/1024;
		if(memClass <  maxsize){
			ret = false;
			tempString = "您的手机内存限制["+memClass+"M]，不足以运行车牌识别所需["+maxsize+"M]";
			Log.e(TAG, tempString);
			showInfo(tempString);
			try {
				throw new Exception("tempString");
			} catch (Exception e) {
				e.printStackTrace();
			}
			mButton.setEnabled(false);
			mbtBrowser.setEnabled(false);
		}else if(mi2.availMem < S.MAX_BYTE) {
			ret = false;
			Log.e(TAG, "failed to new memory! mi.availMem="+mi2.availMem);
			tempString = "您的手机内存已不足！"; 
			showInfo(tempString);
			System.gc();
			mButton.setEnabled(false);
			mbtBrowser.setEnabled(false);
		}

		S.logcatMemory();
		Log.d(TAG, " 您的手机内存限制["+memClass+"M]，运行车牌识别所需["+maxsize+"M]");

		return ret;
	}

	public static Handler getHandler(){
		return handler;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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

	public void waiting(){
	}

	private void processImage(int requestCode, int resultCode, Intent data){

		switch (requestCode) {  
		case S.RESULT_LOAD_IMAGE:
			if (data != null) {  
				//取得返回的Uri,基本上选择照片的时候返回的是以Uri形式，但是在拍照中有得机子呢Uri是空的，所以要特别注意  
				Uri mImageCaptureUri = data.getData();  
				if (mImageCaptureUri != null) {  

					//					mTextView.setText(mContext.getString(R.string.loading_process));
					loading = ProgressDialog.show(mContext, "", mContext.getString(R.string.loading_process), true);
					loading.show();

					String path = ImageDispose.GetFilePathFromContentUri(mImageCaptureUri, this.getContentResolver());
					Message msg = new Message();
					msg.obj = path;
					msg.what = S.ACTION_SHOW_IMAGE;
					handler.sendMessage(msg);

					lprServiceIntent.putExtra("flag", S.RESULT_LOAD_IMAGE);
					lprServiceIntent.putExtra("data", path);
					startService(lprServiceIntent);  

					Log.d(TAG,"Started lprService ");
				} else {  
					//如果Uri为空，那么我们就进行下面的方式获取  
					Bundle extras = data.getExtras();  
					if (extras != null) {  
						Log.d(TAG,"Show image from  Bundle");
						//这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片  
						mBitmap = extras.getParcelable("data");  
						if (mBitmap != null) {  
							Log.d(TAG, "LOGAD Bundle Bitmap");
							imageView.setImageBitmap(mBitmap); 
						}
					}  
				} 
			}  
			break;  
		case S.RESULT_LOAD_CAMERA_IMAGE:
			if(mPhotoFile!=null){

				//				mTextView.setText(mContext.getString(R.string.loading_process));
				loading = ProgressDialog.show(mContext, "", mContext.getString(R.string.loading_process), true);
				loading.show();

				String path = mPhotoFile.getAbsolutePath();
				Message msg = new Message();
				msg.obj = path;
				msg.what = S.ACTION_SHOW_IMAGE;
				handler.sendMessage(msg);

				lprServiceIntent.putExtra("flag", S.RESULT_LOAD_IMAGE);
				lprServiceIntent.putExtra("data", path);
				startService(lprServiceIntent);  

				Log.d(TAG,"Started lprService ");
			}
			break;
		default :
			break;
		}

	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(lprServiceIntent);
		stopService(updateServiceIntent);
		//	Debug.stopMethodTracing();
		Debug.stopNativeTracing();
	}

	private MediaScannerConnection mConnection = null;

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch(item.getItemId()){
		case R.id.about:
			//String s = null;	s.getBytes();  //for debug crash report;
			About();
			break;
		case R.id.plateBrowser:
			//			Intent intent = new Intent();
			//			intent.setType("image/*");
			//			intent.setAction(Intent.ACTION_VIEW);
			//			startActivity(intent);

			Intent intent = new Intent();  
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
			intent.setAction(android.content.Intent.ACTION_VIEW);  
			File  dir =new  File(Util.GetPlateDir());
			File  files [] = dir.listFiles();
			if(files.length>0){
				intent.setDataAndType(Uri.fromFile(files[0]), "image/*");  
				startActivity(intent);  
			}else{
				toast("车牌库暂无图片");
			}
			break; 
		case R.id.exit:
			android.os.Process.killProcess(android.os.Process.myPid());  
			System.exit(1);  
			break;
		case R.id.update:
			loading = ProgressDialog.show(MainActivity.this, "",
					"正在检测最新版本...", true);
			startService(updateServiceIntent);
			break;
		case R.id.action_settings:
			if(checkGPS())
			{
				UpdateService.checkVersion();
				Location location= GPS.getLocation();
				if (location!=null) {    
					Toast.makeText(this, "经度："+location.getLatitude() + "  纬度："+location.getLongitude(),
							Toast.LENGTH_LONG).show(); 
				}else{
					Toast.makeText(this, "获取经纬度失败!", Toast.LENGTH_LONG).show(); 
				}
			}
			break;
		case R.id.sysConfig:
			SysConfig();
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}  

	public boolean checkGPS(){

		if(GPS.checkGPS()==false){
			Builder alertDialog = new AlertDialog.Builder(this); 
			alertDialog.setTitle("提示");
			alertDialog.setIcon(R.drawable.ic_launcher);
			alertDialog.setMessage("您的GPS未开启，是否要打开GPS ?"); 
			alertDialog.setNegativeButton("打开", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent();
					intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivityForResult(intent, 0);
				}
			});
			alertDialog.setPositiveButton("取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

				}
			});
			alertDialog.create(); 
			alertDialog.show();
			return GPS.checkGPS();
		}else{
			return true;
		}
	}

	public void SysConfig(){
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);  
		MemoryInfo mi2 = new MemoryInfo();  
		am.getMemoryInfo(mi2);
		int memClass = am.getMemoryClass();
		//	int maxsize =  ( S.MAX_BYTE + mi.dalvikPss*1024)/1024/1024;
		//	tempString = "运行车牌识别所需["+maxsize+"M]";
		android.os.Debug.MemoryInfo mi = new android.os.Debug.MemoryInfo();
		Debug.getMemoryInfo(mi);
		tempString  = "LargerHeap = "+memClass+" M\n";
		tempString += "JavaHeap = "+mi.dalvikPss + " KB\n";
		tempString += "NativeHeap = "+mi.nativePss + " KB\n";
		tempString += "OtherHeap = "+mi.otherPss + " KB\n";
		tempString += "TotalHeap = "+mi.getTotalPss() + " KB\n";

		Builder alertDialog = new AlertDialog.Builder(this); 
		alertDialog.setTitle("系统状态");
		alertDialog.setIcon(R.drawable.ic_launcher);
		alertDialog.setMessage(tempString); 
		alertDialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//
			}
		});
		alertDialog.create(); 
		alertDialog.show(); 
	}

	public void About(){

		PackageManager pm = getPackageManager();//context为当前Activity上下文 
		PackageInfo pi = null;

		try {
			pi = pm.getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}
		String msg= "信帧电子技术(北京)有限公司\n";
		msg += "\n车牌识别\n\n";
		if(pi!=null){
			int versionCode = pi.versionCode;
			String versionName = pi.versionName;
			msg += "版本:"+versionName+"\n";
		}
		msg += "Copyright @ 2011-2014 Teleframe.\n";
		msg += "All Rights Reserved.\n";
		msg += "http://www.teleframe.cn";

		Builder alertDialog = new AlertDialog.Builder(this); 
		alertDialog.setTitle("关于");
		alertDialog.setIcon(R.drawable.ic_launcher);
		alertDialog.setMessage(msg); 
		alertDialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//
				//				Intent intent = new Intent();  
				//				intent.setClass(MainActivity.this, WebActivity.class);
				//				intent.putExtra("url","http://www.teleframe.cn");
				//				startActivity(intent);
			}
		});
		alertDialog.create(); 
		alertDialog.show(); 
	}


	long waitTimes = 3*1000;  
	long touchTime = 0;  

	@Override  
	public void onBackPressed() {  
		long currentTime = System.currentTimeMillis();  
		if((currentTime-touchTime)>=waitTimes) {  
			Toast.makeText(this, "再按一次返回键退出程序", (int) waitTimes).show();  
			touchTime = currentTime;  
		}else {  
			finish();  
			android.os.Process.killProcess(android.os.Process.myPid());  
			System.exit(1);  
		}  
	} 


	//自定义一个类，继承View    
	class MyCustomView extends View{    

		private Movie mMovie;    
		private long mMovieStart;    

		public MyCustomView(Context context) {    
			super(context);    
			//以文件流的方式读取文件    
			mMovie = Movie.decodeStream(    
					getResources().openRawResource(R.drawable.xiaozai));    
		}    

		@Override    
		protected void onDraw(Canvas canvas) {    

			long curTime = android.os.SystemClock.uptimeMillis();    
			//第一次播放    
			if(mMovieStart == 0){    
				mMovieStart = curTime;    
			}    

			if(mMovie != null){    
				int duration = mMovie.duration();   

				int relTime = (int)((curTime - mMovieStart)% duration);    
				mMovie.setTime(relTime);    
				mMovie.draw(canvas, 0, 0);    

				//强制重绘      
				invalidate();    

			}     
			super.onDraw(canvas);    
		}    
	}    
}