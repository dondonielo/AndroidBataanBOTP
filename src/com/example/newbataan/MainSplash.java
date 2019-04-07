package com.example.newbataan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;



public class MainSplash extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.splash);
		
		Thread loadTimer = new Thread(){
			
			public void run(){
				try{
					sleep(5000);
					Intent menuIntent = new Intent(MainSplash.this, MainMenu.class);
					startActivity(menuIntent);
				 }
				catch(InterruptedException e){
					e.printStackTrace();
				}
				finally{
					finish();		
				}
				
			}
			
		};
		
		loadTimer.start();
	}

	
}