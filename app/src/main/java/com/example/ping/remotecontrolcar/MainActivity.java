package com.example.ping.remotecontrolcar;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static BluetoothAdapter btAdapter; //for scanning BT devices and querying a list of bonded(paired) devices
    public static BluetoothSocket btSocket; //for connecting device and transfer data
    private static BluetoothDevice device;
    private static BroadcastReceiver btReceiver;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //fixed, serial port
    //private OutputStream output;
    private ImageButton image;
    private TextView direction;
    private AnimationDrawable frameAnimation;
    private ConnectListener connectListener;
    private BTDiscoveryListener listener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        direction = (TextView)findViewById(R.id.direction);
        image = (ImageButton)findViewById(R.id.imageButton);
        connectListener = new ConnectListener();
        listener = new BTDiscoveryListener();

        //Determine if the Android device supports Bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null) {
            //if the device doesn't support BT
            Toast.makeText(this, "The device doesn't support bluetooth", Toast.LENGTH_LONG).show();
            return;
        }
        //Turn on Bluetooth if disabled
        if(!btAdapter.isEnabled()) {
            btAdapter.enable();
            Toast.makeText(this,"~Enable Bluetooth~",Toast.LENGTH_SHORT).show();
        }
        btReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // Get the background, which has been compiled to an AnimationDrawable object

                if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
                {
                    // Start the animation (looped playback by default)
                    Toast.makeText(MainActivity.this,"Discovery started!",Toast.LENGTH_SHORT).show();
                    image.setBackgroundResource(R.drawable.bt_animation);
                    frameAnimation = (AnimationDrawable) image.getBackground();
                    frameAnimation.start();
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    if (frameAnimation.isRunning()) {
                        frameAnimation.stop();
                        image.setBackgroundResource(R.drawable.btdiscovery4);
                        Toast.makeText(MainActivity.this,"沒有發現藍芽裝置",Toast.LENGTH_SHORT).show();
                    }
                }
                //find any device
                else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    Toast.makeText(MainActivity.this,"發現藍芽裝置",Toast.LENGTH_SHORT).show();
                    btAdapter.cancelDiscovery();
                    if (frameAnimation.isRunning()) {
                        frameAnimation.stop();
                        image.setBackgroundResource(R.drawable.btdiscovery4);
                    }
                    //get the BT device object
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //if(device.getName().equals("beaglebone-0"))
                    if(device.getAddress().equals("00:1A:7D:DA:71:0D"))
                    {
                        // 連接建立之前的先配對
                        // pair
                        if (device.getBondState() == BluetoothDevice.BOND_NONE)
                        {
                            AlertDialog.Builder ask=new AlertDialog.Builder(MainActivity.this);
                            ask.setMessage("是否要與beaglebone-0配對?");
                            ask.setPositiveButton("是", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int i) {
                                    try {
                                        Method creMethod = BluetoothDevice.class.getMethod("createBond");
                                        creMethod.invoke(device);
                                        image.setBackgroundResource(R.drawable.btpair);
                                        direction.setText("Connect to beaglebone!");
                                        image.setOnClickListener(connectListener);

                                    } catch (NoSuchMethodException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            ask.setNegativeButton("否", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int i) {
                                    //do nothing
                                }
                            });
                            ask.show();
                        }
                        else if(device.getBondState() == BluetoothDevice.BOND_BONDED)
                        {
                            Toast.makeText(MainActivity.this,"與beaglebone-0已配對",Toast.LENGTH_SHORT).show();
                            image.setBackgroundResource(R.drawable.btpair);
                            direction.setText("Connect to beaglebone!");
                            image.setOnClickListener(connectListener);
                        }
                    }
                }
            }
        };
        boolean pairedWithRCCar = false;
        //Get the Bluetooth module device
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        //If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice tmpdevice : pairedDevices) {
                if(tmpdevice.getAddress().equals("00:1A:7D:DA:71:0D")){
                //if (tmpdevice.getName().equals("beaglebone-0")) {
                    Toast.makeText(this,"與beaglebone-0已配對",Toast.LENGTH_SHORT).show();
                    device = tmpdevice;
                    pairedWithRCCar = true;
                    direction.setText("Connect to beaglebone!");
                    image.setBackgroundResource(R.drawable.btpair);
                    image.setOnClickListener(connectListener);
                    break;
                }
            }
        }
        if(!pairedWithRCCar){//if there is no paired device
            //while(!btAdapter.startDiscovery()){}
            // Register the BroadcastReceiver
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(btReceiver, filter); // Don't forget to unregister during onDestroy
            image.setOnClickListener(listener);
        }
    }
    /*@Override
    protected void onDestroy()
    {
        super.onDestroy();
        destroy();
    }*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            ConfirmExit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void ConfirmExit(){
        AlertDialog.Builder ad=new AlertDialog.Builder(MainActivity.this);
        ad.setMessage("確定要離開此程式嗎?");
        ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//exit  button
            public void onClick(DialogInterface dialog, int i) {
                destroy();
                //MainActivity.this.finish();//finish activity
            }
        });
        ad.setNegativeButton("否",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                //do nothing
            }
        });
        ad.show();
    }
    public void destroy()
    {
        if(btAdapter!=null && btAdapter.isEnabled())
            btAdapter.disable();
        try {
            if (btSocket!=null && btSocket.isConnected()){
                //output.close();
                btSocket.close();
            }
            unregisterReceiver(btReceiver);
        } catch (IOException e) {
            e.printStackTrace();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.function, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_discovery:
                if(!btAdapter.isDiscovering())
                    btAdapter.startDiscovery();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/
    public class BTDiscoveryListener implements  OnClickListener
    {
        @Override
        public void onClick(View v) {
            if(!btAdapter.isDiscovering())
                btAdapter.startDiscovery();
        }
    }
    public class ConnectListener implements OnClickListener
    {
        @Override
        public void onClick(View v) {
            Toast.makeText(MainActivity.this, "Start to make a connection!", Toast.LENGTH_SHORT).show();
            if(device.getBondState() == BluetoothDevice.BOND_BONDED) {
                try {
                    btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                }catch(IOException e){
                    Log.v("#bluetoothSocket",e.getMessage());
                }
                try {
                    btSocket.connect();
                    connectSuccess(true);
                } catch (IOException connectException) {
                    try {
                        btSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                        btSocket.connect();
                        connectSuccess(true);
                    } catch (IOException e) {
                        Log.v("#IOException", e.toString());
                        connectSuccess(false);
                    } catch (InvocationTargetException e) {
                        Log.v("#ITException", e.toString());
                        connectSuccess(false);
                    } catch (NoSuchMethodException e1) {
                        Log.v("#NoSuchMethodException", e1.toString());
                        connectSuccess(false);
                    } catch (IllegalAccessException e) {
                        Log.v("#IllegalAccessException", e.toString());
                        connectSuccess(false);
                    }
                }
            }
            else
                Toast.makeText(MainActivity.this, "No paired devices!", Toast.LENGTH_SHORT).show();
        }
    }
    public void connectSuccess(boolean success)
    {
        if(success)
        {
            Toast.makeText(MainActivity.this, "Connect Successfully!", Toast.LENGTH_SHORT).show();
            //image.setOnClickListener(null);
            //change view to control view
            Intent intent = new Intent();
            intent.setClass(MainActivity.this,ControlActivity.class);
            startActivity(intent);
            MainActivity.this.finish();
        }
        else
        {
            Toast.makeText(MainActivity.this, "Connection failed! Make Sure beaglebone-0 is ready and try again!", Toast.LENGTH_LONG).show();
        }
    }
}
