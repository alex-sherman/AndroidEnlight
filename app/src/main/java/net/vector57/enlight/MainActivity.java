package net.vector57.enlight;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.util.HashSet;
import java.util.List;

import mrpc.MRPC;
import mrpc.Message;
import mrpc.Result;

public class MainActivity extends AppCompatActivity {

    MRPC mrpc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mrpc = new MRPC(getApplicationContext());
        attachSwitch(R.id.hallway, "/hallway.relay");
        attachSwitch(R.id.livingRoom, "/LivingRoom.relay");
        attachSwitch(R.id.couch, "/Couch.relay");
        attachSwitch(R.id.desk, "/Desk.relay");
    }
    private void attachSwitch(int switchId, final String path) {
        updateSwitch(switchId, path);
        ((Switch)findViewById(switchId)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mrpc.RPC(path, isChecked);
            }
        });
    }
    private void updateSwitch(int switchId, final String path) {
        final Switch sw = (Switch)findViewById(switchId);
        mrpc.RPC(path, null, new Result.Callback() {
            @Override
            public void onSuccess(JsonElement value) {
                Boolean b = Message.gson().fromJson(value, Boolean.class);
                if(b != null) {
                    sw.setChecked(b);
                }
            }
        });
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
                //((TextView)findViewById(R.id.device_list)).setText(devices.toString());
                super.onSuccess(value);
            }
        });
    }
}
