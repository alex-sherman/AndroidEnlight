package net.vector57.android.mrpc;

import android.content.ComponentCallbacks2;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.appcompat.BuildConfig;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;


import net.vector57.mrpc.MRPC;
import net.vector57.mrpc.PathCacheEntry;
import net.vector57.mrpc.Result;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alex Sherman on 12/20/2016.
 */

public class MRPCActivity extends AppCompatActivity {
    private static int open_mrpcs = 0;
    private static AndroidMRPC _mrpc;
    public static AndroidMRPC mrpc() { return _mrpc; }
    public static void mrpc(String path, Object value) { mrpc(path, value, null, true); }
    public static void mrpc(String path, Object value, Result.Callback callback)
    { mrpc(path, value, callback, true); }
    public static void mrpc(String path, Object value, Result.Callback callback, boolean resend)
    { if(_mrpc != null) _mrpc.RPC(path, value, callback, resend); }
    public static String path_cache_preference_key = "MRPC_path_cache";

    private synchronized AndroidMRPC allocateMRPC() {
        if(open_mrpcs == 0) {
            if(BuildConfig.DEBUG && _mrpc != null)
                throw new AssertionError("Reference counting logic failure");

            Type t = new TypeToken<Map<String, List<PathCacheEntry.UUIDEntry>>>() {}.getType();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String pathCacheJSON = sharedPref.getString(path_cache_preference_key, "{}");
            Map<String, List<PathCacheEntry.UUIDEntry>> pathCache;
            try {
                pathCache = MRPC.gson().fromJson(pathCacheJSON, t);
            }
            catch (JsonSyntaxException e) {
                pathCache = new HashMap<>();
            }

            try {
                _mrpc = new AndroidMRPC(this, Util.getBroadcastAddress(this), pathCache);
            } catch (IOException e) {
                throw new AssertionError("I don't like checked exceptions");
            }
        }
        open_mrpcs++;
        return _mrpc;
    }

    private synchronized void deallocateMRPC() {
        open_mrpcs--;
        if(open_mrpcs == 0) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPref.edit().putString(path_cache_preference_key, MRPC.gson().toJson(_mrpc.getPathCache())).apply();
            _mrpc.close();
            _mrpc = null;
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if(level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            Log.d("AndroidMRPC", "trimmed");
        }
    }

    @Override
    protected void onStart() {
        allocateMRPC();
        super.onStart();
    }

    @Override
    protected void onStop() {
        deallocateMRPC();
        super.onStop();
    }
}
