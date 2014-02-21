package com.base;

import com.lpr.LprService;
import com.teleframe.service.UpdateService;

import android.app.Application;
import android.content.Context;

public  class BaseInit {

	public static void init(Context c, Application app){
		
		LprService.init(c);
		UpdateService.init(c);
		Util.init(c);
		SIMCardInfo.init(c);
		EmailUtil.init(c);
		GPS.init(c, app);
		
	//	if(BuildConfig.DEBUG==false)
			BaseExceptionHandler.getInstance().init(c);  //传入参数必须为Activity，否则AlertDialog将不显示。
	}

}
