package com.example.newbataan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.newbataan.R;


public class MainMenu extends Activity implements View.OnClickListener{
	
	private Button btnPap;
	private Button btnAbout;
	private Button btnHelp;
	private Button btnExit;
	private Button btnMult;
	private TextView titleText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.mainui);
		
		Typeface font = Typeface.createFromAsset(getAssets(), "fonts/androidnation.ttf");
		Typeface fontTitle = Typeface.createFromAsset(getAssets(), "fonts/CFNuclearWar-Regular.ttf");
        
		titleText = (TextView) findViewById(R.id.titleMain);
		titleText.setTypeface(fontTitle);
		btnPap = (Button) findViewById(R.id.pap_button);
		btnPap.setTypeface(font);
		btnMult = (Button) findViewById(R.id.two_player_button);
		btnMult.setTypeface(font);
		btnAbout = (Button) findViewById(R.id.about_button);
		btnAbout.setTypeface(font);
		btnHelp = (Button) findViewById(R.id.help_button);
		btnHelp.setTypeface(font);
		btnExit = (Button) findViewById(R.id.exit_button);
		btnExit.setTypeface(font);
		
		btnPap.setOnClickListener(this);
		btnMult.setOnClickListener(this);
		btnAbout.setOnClickListener(this);
		btnHelp.setOnClickListener(this);
		btnExit.setOnClickListener(this);
		
		
	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onClick(View v) {
	
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.pap_button:
			startActivity(new Intent(MainMenu.this, MainNewBataan.class));
			break;
			
		case R.id.two_player_button:
			startActivity(new Intent(MainMenu.this, Multiplayer.class));
			break;
			//TODO to be edited after bluetooth
		case R.id.help_button:
			startActivity(new Intent(MainMenu.this, HelpMenu.class));
			break;
			
		case R.id.about_button:
			startActivity(new Intent(MainMenu.this, AboutMenu.class));
			break;
			
		case R.id.exit_button:
			onBackPressed();
			break;
	}
		
	}
	public void onBackPressed(){
		showDialog(2);
	}
	
	@Override
	 protected void onDestroy() {
	    android.os.Process.killProcess(android.os.Process.myPid());
	    super.onDestroy();
	}
	
	public Dialog onCreateDialog(int id){
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		switch(id){
            case 2:
            	builder.setTitle("Exit Bataan");
                builder.setMessage("Are you sure you want to exit?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        finish();
                    }
                }).setNegativeButton("No", null);
                dialog = builder.create();
            break;
		}
		
		return dialog;
	}

	
}
