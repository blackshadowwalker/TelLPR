package com.base;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class Util {

	public static String  TAG = "Teleframe.TELLPR.MainActivity";
	public static final String COMPANY_NAME = "Teleframe";
	public static final String PRODUCTION_NAME = "TELLPR";
	public static final String LOG = "log";
	public static final String NATIVE = "Native";
	public static final String CRASH = "crash";
	public static final String TEMP = "temp";
	public static final String IMAGE = "image";
	public static final String VIDEO = "video";

	public static final String BASE_PATH = COMPANY_NAME+"/"+PRODUCTION_NAME;

	public static final int TYPE_IMAGE = 1;
	public static final int TYPE_VIDEO = 2;
	public static final int TYPE_LOG = 3;
	public static final int TYPE_NATIVE = 4;
	public static final int TYPE_CRASH = 5;
	public static final int TYPE_TEMP = 6;

	private static Context mContext;  
	
	public static void init(Context ctx) {  
		mContext = ctx;  
	}  

	// log 
	public static String GetLogDir(){
		return BASE_PATH+"/"+LOG;
	}
	public static File GetLogFile(){
		return GetOutputFile(TYPE_LOG);
	}

	// image
	public static String GetImageDir(){
		return BASE_PATH+"/"+IMAGE;
	}
	public static File GetImageFile(){
		return GetOutputFile(TYPE_IMAGE);
	}

	// video
	public static String GetVideoDir(){
		return BASE_PATH+"/"+VIDEO;
	}
	public static File GetVideoFile(){
		return GetOutputFile(TYPE_VIDEO);
	}

	// Crash
	public static String GetCrashDir(){
		return BASE_PATH+"/"+CRASH;
	}
	public static File GetCrashFile(){
		return GetOutputFile(TYPE_CRASH);
	}

	// Native
	public static String GetNaiveDir(){
		return BASE_PATH+"/"+NATIVE;
	}
	public static File GetNativeFile(){
		return GetOutputFile(TYPE_NATIVE);
	}

	// Temp
	public static String GetTempDir(){
		return BASE_PATH+"/"+TEMP;
	}
	public static File GetTempFile(){
		return GetOutputFile(TYPE_TEMP);
	}

	public static  File GetOutputFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			return  null;
		}
		String Path = Environment.getExternalStorageDirectory()+"/"+GetPath(type);//获取跟目录 
		File mediaStorageDir = new File(Path);
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()) {
				Log.e(TAG, "创建目录失败["+mediaStorageDir+"]");
				new Exception("创建目录失败["+mediaStorageDir+"]").printStackTrace();
				return null;
			}else{
				Log.d(TAG, "创建目录成功["+mediaStorageDir+"]");
				new Exception("创建目录成功["+mediaStorageDir+"]").printStackTrace();
			}
		}
		File file = GetFile(type, Path);
		if (!file.exists()) {  
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}  
		}  
		return file;
	}

	private static String GetPath(int type){
		String Path = null;
		switch(type){
		case TYPE_IMAGE:
			Path = GetImageDir();
			break;
		case TYPE_VIDEO:
			Path = GetVideoDir();
			break;
		case TYPE_LOG:
			Path = GetLogDir();
			break;
		case TYPE_NATIVE:
			Path = GetNaiveDir();
			break;
		case TYPE_CRASH:
			Path = GetCrashDir();
			break;
		default:
			Path = BASE_PATH+"/"+TEMP;	
		}
		return Path;
	}

	private static File GetFile(int type, String path){
		File file = null;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		switch(type){
		case TYPE_IMAGE:
			file = new File(path + File.separator +"IMG_"+ timeStamp + ".jpg");
			break;
		case TYPE_VIDEO:
			file = new File(path + File.separator +"VID_"+ timeStamp + ".mp4");
			break;
		case TYPE_LOG:
			file = new File(path + File.separator +"log_"+ timeStamp + ".log");
			break;
		case TYPE_NATIVE:
			file = new File(path + File.separator +"native_"+ timeStamp + ".log");
			break;
		case TYPE_CRASH:
			file = new File(path + File.separator +"carsh_"+ timeStamp + ".log");
			break;
		default:
			file = new File(path + File.separator +"temp_"+ timeStamp + ".file");
		}
		return file;
	}




}
