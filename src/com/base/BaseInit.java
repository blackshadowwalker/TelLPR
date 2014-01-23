package com.base;

import com.lpr.LprService;

import android.content.Context;

public  class BaseInit {

	public static void init(Context c){
		
		new LprService().init(c);
		
		CustomExceptionHandler crashHandler = CustomExceptionHandler.getInstance();   
		crashHandler.init(c);  //传入参数必须为Activity，否则AlertDialog将不显示。
		
		Util.init(c);
	}

}
