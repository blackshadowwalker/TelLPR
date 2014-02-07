package com.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
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

import javax.mail.MessagingException;

import com.baidu.location.BDLocation;
import com.teleframe.teflpr.MainActivity;
import com.teleframe.teflpr.R;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.Looper;
import android.os.Debug.MemoryInfo;
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

	ProgressDialog loading = null; 

	private boolean bOK = true;

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

	
	private String filePath="";
	private String filenames [] =null;
	
	private boolean handleException(Throwable ex) {  
		if (ex == null) {  
			return false;  
		}  

		//使用Toast来显示异常信息   
		new Thread() {  
			@Override  
			public void run() {  
				Looper.prepare();  
				//	Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG).show();  
				loading = ProgressDialog.show(mContext, "", "很抱歉,程序出现异常, 正在处理...", true);
				Looper.loop();  
			}  
		}.start();  

		//收集设备参数信息    
		collectDeviceInfo(mContext);  
		//保存日志文件    
		filePath = saveCrashInfo2File(ex);  
		System.out.println(filePath);
		if(filenames==null)
			filenames = new String[1];
		filenames[0] = filePath;
		
		if(filePath==null){
			bOK = false;
		}else{
			new Thread(){
				@Override  
				public void run() { 
					Looper.prepare();  
					try {
						System.out.println(filenames[0] );

						SIMCardInfo siminfo = SIMCardInfo.getInstance();
						String subject = "Crash Report of \""+ mContext.getString(R.string.app_name)+"\" " +
								"@ Phone: " + siminfo.getNativePhoneNumber()+" ["+siminfo.getProvidersName()+"]";

						StringBuffer bodyText = null;
						if(filePath!=null)
							bodyText = GetFileCrashContent(filePath);

						if(bodyText==null)
							bodyText = new StringBuffer();
						
						bodyText.append("\n\n");

						Location location= GPS.getLocation();
						String GPSinfo="";
						if(location!=null)
						{
							GPSinfo = ("latitude="+location.getLatitude()+"&longitude="+location.getLongitude());
						}
						String mailto [] =  {S.EMAIL_TO};
						String cc [] = {};
						String bcc [] = {};

						try{
							EmailUtil.getInstance().sendMail(S.EMAIL_HOST, S.EMAIL_FROM, true, 
									S.EMAIL_USERNAME, S.EMAIL_PASSWORD, 
									mailto,  cc,  bcc , subject,
									subject + "\n"+GPSinfo+"\n\n"+bodyText.toString(), null );
							bodyText.setLength(0);
							bodyText = null;
							File file =  new File(filePath);
							if(file.canWrite())	
								file.delete();
							file = null;
							//	loading = ProgressDialog.show(mContext, "", "异常处理完成,即将退出.", true);
							//	Toast.makeText(mContext, "异常处理完成,即将退出.", Toast.LENGTH_LONG).show();  

							Builder alertDialog = new AlertDialog.Builder(mContext); 
							alertDialog.setTitle("提示");
							alertDialog.setIcon(R.drawable.ic_launcher);
							alertDialog.setMessage("异常处理完成,即将退出. 请重新启动程序."); 
							alertDialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									android.os.Process.killProcess(android.os.Process.myPid());  
									System.exit(1);  
								}
							});
							alertDialog.create(); 
							alertDialog.show(); 
						}catch(MessagingException e){
							
							String fname = saveCrashInfo2File(e); 
							
							Builder alertDialog = new AlertDialog.Builder(mContext); 
							alertDialog.setTitle("提示");
							alertDialog.setIcon(R.drawable.ic_launcher);
							alertDialog.setMessage("异常已完成处理,但报告发送失败,即将退出."); 
							alertDialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									android.os.Process.killProcess(android.os.Process.myPid());  
									System.exit(1);  
								}
							});
							alertDialog.create(); 
							alertDialog.show(); 
						}
						loading.dismiss();


					} catch (Exception e) {
						e.printStackTrace();
						bOK = false;

					}
					bOK = true;
					Looper.loop();  
				}
			}.start();
		}

		return bOK;  
	} 

	public static StringBuffer GetFileCrashContent(String filePath){

		StringBuffer byteText = null;
		File f = new File(filePath);
		int  bufsize=1024;
		try {
			byte buffer[] = new byte[bufsize]; 
			String temp = "";
			InputStream is = new FileInputStream(f);
			byteText = new StringBuffer(is.available()+2);
			int len = -1;
			while( (len = is.read(buffer,0, bufsize)) != -1){
				temp =(new String(buffer)).substring(0, len);
				byteText.append(temp);
			}
			is.close();
			f=null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		//	System.out.println(byteText.toString());
		return byteText;
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
				//	Log.d(TAG, field.getName() + " : " + field.get(null));  
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

		StringBuffer sb = new StringBuffer(1024*4);  

		SimpleDateFormat fm = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		sb.append("DateTime="+fm.format(new Date())+"\n");

		for (Map.Entry<String, String> entry : infos.entrySet()) {  
			String key = entry.getKey();  
			String value = entry.getValue();  
			sb.append(key + "=" + value + "\n");  
		}  

		sb.append("\n");
		sb.append("MemoryInfo:\n");
		android.os.Debug.MemoryInfo mi = new MemoryInfo();
		Debug.getMemoryInfo(mi);
		sb.append("mi.getTotalPss="+mi.getTotalPss()+"KB\n");
		sb.append("mi.dalvikPss="+mi.dalvikPss+"KB\n");
		sb.append("mi.nativePss="+mi.nativePss+"KB\n");
		sb.append("mi.otherPss="+mi.otherPss+"KB\n");
		ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);  
		android.app.ActivityManager.MemoryInfo mi2 = new android.app.ActivityManager.MemoryInfo();  
		am.getMemoryInfo(mi2);
		int memClass = am.getMemoryClass();
		sb.append("am.getMemoryClass="+memClass+"M"+"\n");

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

		Log.e(TAG, result);

		sb.append("\n");
		sb.append("Exception:\n");
		sb.append(result);  
		sb.append("\n---END---\n");  
		try {  
			long timestamp = System.currentTimeMillis();  
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {  
				File file = Util.GetCrashFile();
				if(file!=null){
					FileOutputStream fos = new FileOutputStream(file);  
					fos.write(sb.toString().getBytes());  
					sb.setLength(0);
					sb = null;
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

