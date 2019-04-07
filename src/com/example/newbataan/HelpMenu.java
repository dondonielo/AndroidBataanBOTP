package com.example.newbataan;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

public class HelpMenu extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.help);
		WebView aWebView = (WebView) findViewById(R.id.helpGame);
		//aWebView.getSettings().setJavaScriptEnabled(true);
		aWebView.loadUrl("file:///android_asset/html/helpGame.html");
		
		
	}
}
