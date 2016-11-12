package net.vector57.enlight;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.net.SocketException;

import mrpc.Message;
import mrpc.SocketTransport;
import mrpc.TransportThread;

public class MainActivity extends AppCompatActivity {

    TransportThread transport;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    0);
        }
        try {
            transport = new SocketTransport(50123);
            transport.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
    public void faff(View view) {
        Message m = new Message.Request("faff");
        transport.sendAsync(m);
    }
}
