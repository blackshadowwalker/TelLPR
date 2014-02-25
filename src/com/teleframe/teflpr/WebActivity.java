package com.teleframe.teflpr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends Activity {

	private WebView webview;

	@Override
	public void onBackPressed() {
//		webview.goBack(); //goBack()表示返回WebView的上一页面  
		super.onBackPressed();
	}
	
	private static String Tag = WebActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 Intent intent = getIntent();

		//实例化WebView对象  
		webview = new WebView(this);  
		//设置WebView属性，能够执行Javascript脚本  
		webview.getSettings().setJavaScriptEnabled(true);  
		webview.getSettings().setAllowFileAccess(true);
		webview.getSettings().setLoadsImagesAutomatically(true);
		
		//设置缩放 
		webview.getSettings().setSupportZoom(true);  
		webview.getSettings().setBuiltInZoomControls(true); 
		webview.getSettings().setAllowContentAccess(true);
		webview.getSettings().setUseWideViewPort(true);

		//当前网页的链接仍在webView中跳转
		webview.setWebViewClient(new WebViewClient() {  
		    @Override  
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {  
		        view.loadUrl(url);  
		        return true;  
		    }  
		});  
		
		//加载需要显示的网页
		String url = intent.getStringExtra("url");
		if(url.startsWith("http"))
			;
		else
			url = "file://"+url;
		Log.i(Tag, url );
		webview.loadUrl(url);  
		//设置Web视图  
		setContentView(webview);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home){
			this.finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override  
    public boolean onCreateOptionsMenu(Menu menu) {  
        // Inflate the menu; this adds items to the action bar if it is present.  
        getMenuInflater().inflate(R.menu.main, menu);  
        this.getActionBar().setDisplayHomeAsUpEnabled(true);
        return true;  
    }  

}
