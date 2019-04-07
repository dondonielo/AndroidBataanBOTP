package com.example.newbataan.nbBlueTooth;

public interface Constants {

    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME_CONNECTED_TO = 4;
    public static final int MESSAGE_DEVICE_NAME_CONNECTING_TO = 5;
    public static final int MESSAGE_TOAST = 6;

    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

}