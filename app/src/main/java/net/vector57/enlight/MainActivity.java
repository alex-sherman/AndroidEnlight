package net.vector57.enlight;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
        mrpc = new MRPC(getApplicationContext());
    }
    HashSet<String> devices = new HashSet<>();
    public void faff(View view) {
        mrpc.RPC("*.alias", null, new Result.Callback() {
            @Override
            public void onSuccess(JsonElement value) {
                List<String> names = Message.gson().fromJson(value, new TypeToken<List<String>>(){}.getType());
                if(names != null) {
                    for (String name : names) {
                        devices.add(name);
                    }
                }
                ((TextView)findViewById(R.id.device_list)).setText(devices.toString());
                super.onSuccess(value);
            }
        });
    }
}
