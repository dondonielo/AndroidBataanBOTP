package com.example.newbataan;



import com.example.newbataan.nbBlueTooth.BataanBlueTooth;
import com.example.newbataan.nbBlueTooth.DeviceListActivity;
import com.example.newbataan.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;



public class Multiplayer extends Activity{
    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.d("fsfsfsf Game", " Crefsfsfsfate Game ");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
        setContentView(R.layout.twoplayer_menu);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/androidnation.ttf");

        TextView cButton = (TextView)findViewById(R.id.createGame_button);
        View createButton = findViewById(R.id.createGame_button);
        createButton.setOnClickListener(createGame_button);
        cButton.setTypeface(font);
        TextView jButton = (TextView)findViewById(R.id.joinGame_button);
        View joinGameButton = findViewById(R.id.joinGame_button);
        joinGameButton.setOnClickListener(joinGame_button);
        jButton.setTypeface(font);

    }


    View.OnClickListener createGame_button = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Log.d("Create Game", " Create Game ");
            Intent SciDamaBluetoothIntent = new Intent(Multiplayer.this, BataanBlueTooth.class);
            SciDamaBluetoothIntent.putExtra(BataanBlueTooth.PLAY_MODE, true);
            startActivity(SciDamaBluetoothIntent);
        }
    };

    View.OnClickListener joinGame_button = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Log.d("Join Game", " Join Game ");
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent( Multiplayer.this, DeviceListActivity.class);
            startActivityForResult(serverIntent, BataanBlueTooth.REQUEST_CONNECT_DEVICE_SECURE);

        }
    };

    @Override
    public void onStart() {
        super.onStart();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BataanBlueTooth.REQUEST_ENABLE_BT);
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Connecting","resultCode =============");
        switch (requestCode) {
            case BataanBlueTooth.REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {

                    Log.d("Connecting","resultCode == Activity.RESULT_OK");
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    //get the device name
                    String name =  data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_NAME);

                    Intent sciDamaBluetoothIntent = new Intent(Multiplayer.this, BataanBlueTooth.class);

                    sciDamaBluetoothIntent.putExtra(BataanBlueTooth.PLAY_MODE, false);
                    sciDamaBluetoothIntent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, address);
                    sciDamaBluetoothIntent.putExtra(DeviceListActivity.EXTRA_DEVICE_NAME, name);

                    startActivity(sciDamaBluetoothIntent);
                }
                break;
            case BataanBlueTooth.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(Multiplayer.this, R.string.not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }}
