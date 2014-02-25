package com.teleframe.teflpr;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.base.SIMCardInfo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class SettingActivity extends Activity {

	private String TAG = SettingActivity.class.getPackage().toString();
	private TextView mTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.setting);
		mTextView = (TextView) findViewById(R.id.setting_textView); 
		
		SIMCardInfo sim = SIMCardInfo.getInstance();
		Map map = sim.getSimMap();
		Iterator  it = map.keySet().iterator();
		String text="";
		String key ="";
		while(it.hasNext()){
			key = (String) it.next() ;
			text +=  key +"="+ map.get(key) + "\r\n";
		}
		Log.d(TAG, text);
		mTextView.setText(text);
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
