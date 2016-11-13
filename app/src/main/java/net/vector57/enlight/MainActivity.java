package net.vector57.enlight;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.net.SocketException;

import mrpc.MRPC;
import mrpc.Message;
import mrpc.Result;
import mrpc.SocketTransport;
import mrpc.TransportThread;

public class MainActivity extends AppCompatActivity {

    MRPC mrpc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mrpc = new MRPC();
    }
    boolean herp = false;
    public void faff(View view) {
        mrpc.RPC("/hallway.relay", !herp, new Result.Callback() {
            @Override
            public void onSuccess(JsonElement value) {
                if(value.isJsonPrimitive()) {
                    JsonPrimitive prim = value.getAsJsonPrimitive();
                    if(prim.isBoolean())
                        herp = prim.getAsBoolean();
                }
                super.onSuccess(value);
            }
        });
    }
}
