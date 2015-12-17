package com.example.ping.remotecontrolcar;

import android.app.AlertDialog;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Ping on 2015/12/16.
 */
public class ControlActivity extends AppCompatActivity {
    BluetoothSocket socket = MainActivity.btSocket;
    OutputStream output;
    ImageButton forwards;
    ImageButton stop;
    ImageButton backwards;
    ButtonListener buttonListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control);
        try {
            output = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        buttonListener = new ButtonListener();
        forwards = (ImageButton)findViewById(R.id.forwards);
        forwards.setOnClickListener(buttonListener);
        stop = (ImageButton)findViewById(R.id.stop);
        stop.setOnClickListener(buttonListener);
        backwards = (ImageButton)findViewById(R.id.backwards);
        backwards.setOnClickListener(buttonListener);

    }
    public class ButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            int command;
            String sendData;
            if(v.getId()==R.id.stop)
            {
                command=0; //set command 0
            }
            else if(v.getId()==R.id.forwards)
            {
                command=1; //set command 1
            }
            else
            {
                command=2; //set command 2
            }
            sendData = command+"\n";
            if(socket.isConnected())
            {
                try {
                    output.write(sendData.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
                Toast.makeText(ControlActivity.this,"Sorry, the connection was close!",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        destroy();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            ConfirmExit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void ConfirmExit(){
        AlertDialog.Builder ad=new AlertDialog.Builder(ControlActivity.this);
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
        /*if(btAdapter!=null && btAdapter.isEnabled())
            btAdapter.disable();
        try {
            if (socket!=null && socket.isConnected()){
                //output.close();
                socket.close();
            }
            unregisterReceiver(btReceiver);
        } catch (IOException e) {
            e.printStackTrace();
        }
        android.os.Process.killProcess(android.os.Process.myPid());*/
    }
}
