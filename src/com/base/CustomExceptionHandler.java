package com.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.teleframe.teflpr.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class CustomExceptionHandler implements UncaughtExceptionHandler {

	private static final String TAG = "Teleframe.TELLPR.CustomExceptionHandler";

	private static UncaughtExceptionHandler mDefaultHandler;
	private static CustomExceptionHandler INSTANCE = new CustomExceptionHandler();  
	private static Context mContext;  
	//用来存储设备信息和异常信息   
	private Map<String, String> infos = new HashMap<String, String>();  
	//用于格式化日期,作为日志文件名的一部分   
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");  
	public static Throwable throwable=null;

	public CustomExceptionHandler() {
	}

	public static CustomExceptionHandler getInstance() {  
		return INSTANCE;  
	}  

	public void init(Context ctx) {  
		mContext = ctx;  
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();  
		Thread.setDefaultUncaughtExceptionHandler(this);  
	}  

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if(ex==null){
			return ;
		}

		if (!handleException(ex) && mDefaultHandler != null) {
			//如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex); 

			throwable = ex;
			Log.e(TAG, "------------ uncaughtException ------------\n " + throwable.getMessage());

			new Thread() {  
				@Override  
				public void run() {  
					Looper.prepare();  
					Builder  alert = new AlertDialog.Builder(mContext);
					alert.setTitle("温馨提示").setCancelable(false); 
					alert.setIcon(R.drawable.ic_launcher);
					alert.setMessage("异常信息...\n"+throwable.getMessage()).setNeutralButton("我知道了", new OnClickListener() {  
						@Override  
						public void onClick(DialogInterface dialog, int which) { 
							android.os.Process.killProcess(android.os.Process.myPid());  
							System.exit(1);  
						}  
					})  
					.create().show();  
					Looper.loop();  
				}  
			}.start();  
		}else{
			try {
				Thread.sleep(1000); 
			}
			catch (InterruptedException e) { 
				Log.e(TAG, "error : ", e);
			} 
		}
	}

	private boolean handleException(Throwable ex) {  
		if (ex == null) {  
			return true;  
		}  
		//使用Toast来显示异常信息   
		new Thread() {  
			@Override  
			public void run() {  
				Looper.prepare();  
				Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG).show();  
				Looper.loop();  
			}  
		}.start();  
		//收集设备参数信息    
		collectDeviceInfo(mContext);  
		//保存日志文件    
		saveCrashInfo2File(ex);  
		return true;  
	} 


	/** 
	 * 收集设备参数信息 
	 * @param ctx 
	 */  
	public void collectDeviceInfo(Context ctx) {  
		try {  
			PackageManager pm = ctx.getPackageManager();  
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);  
			if (pi != null) {  
				String versionName = pi.versionName == null ? "null" : pi.versionName;  
				String versionCode = pi.versionCode + "";  
				infos.put("versionName", versionName);  
				infos.put("versionCode", versionCode);  
			}  
		} catch (NameNotFoundException e) {  
			Log.e(TAG, "an error occured when collect package info", e);  
		}  
		Field[] fields = Build.class.getDeclaredFields();  
		for (Field field : fields) {  
			try {  
				field.setAccessible(true);  
				infos.put(field.getName(), field.get(null).toString());  
				Log.d(TAG, field.getName() + " : " + field.get(null));  
			} catch (Exception e) {  
				Log.e(TAG, "an error occured when collect crash info", e);  
			}  
		}  
	}  

	/** 
	 * 保存错误信息到文件中 
	 *  
	 * @param ex 
	 * @return  返回文件名称,便于将文件传送到服务器 
	 */  
	private String saveCrashInfo2File(Throwable ex) {  

		StringBuffer sb = new StringBuffer();  
		for (Map.Entry<String, String> entry : infos.entrySet()) {  
			String key = entry.getKey();  
			String value = entry.getValue();  
			sb.append(key + "=" + value + "\n");  
		}  

		Writer writer = new StringWriter();  
		PrintWriter printWriter = new PrintWriter(writer);  
		ex.printStackTrace(printWriter);  
		Throwable cause = ex.getCause();  
		while (cause != null) {  
			cause.printStackTrace(printWriter);  
			cause = cause.getCause();  
		}  
		printWriter.close();  
		String result = writer.toString();  
		sb.append(result);  
		try {  
			long timestamp = System.currentTimeMillis();  
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {  
				File file = Util.GetCrashFile();
				if(file!=null){
					FileOutputStream fos = new FileOutputStream(file);  
					fos.write(sb.toString().getBytes());  
					fos.close();  
					return file.getAbsolutePath();  
				}
			}  
		} catch (Exception e) {  
			Log.e(TAG, "an error occured while writing file...", e);  
		}  
		return null;  
	}  
}  

