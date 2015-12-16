package com.example.ping.remotecontrolcar;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
}
