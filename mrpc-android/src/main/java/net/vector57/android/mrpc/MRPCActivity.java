package net.vector57.android.mrpc;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
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
    public static String proxy_url_preference_key = "MRPC_proxy_url";
    public static String proxy_url_api_key_key = "MRPC_api_key";
    public static String proxy_url_is_https_key = "MRPC_url_is_https";


    private synchronized AndroidMRPC allocateMRPC() {
        if(open_mrpcs == 0) {
            if(BuildConfig.DEBUG && _mrpc != null)
                throw new AssertionError("Reference counting logic failure");

            Type t = new TypeToken<Map<String, List<PathCacheEntry.UUIDEntry>>>() {}.getType();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String pathCacheJSON = sharedPref.getString(path_cache_preference_key, "{}");
            String proxyUrl = sharedPref.getString(proxy_url_preference_key, "");
            String apiKey = sharedPref.getString(proxy_url_api_key_key, "");
            Boolean isHTTPS = sharedPref.getBoolean(proxy_url_is_https_key, false);
            Map<String, List<PathCacheEntry.UUIDEntry>> pathCache;
            try {
                pathCache = MRPC.gson().fromJson(pathCacheJSON, t);
            }
            catch (JsonSyntaxException e) {
                pathCache = new HashMap<>();
            }
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (wifi.isConnected()) {
                try {
                    _mrpc = new AndroidMRPC(this, Util.getBroadcastAddress(this), pathCache);
                } catch (IOException e) {
                    throw new AssertionError("I don't like checked exceptions");
                }
            }
            else
                _mrpc = new AndroidMRPC(this, "http" + (isHTTPS ? "s" : "") + "://"+proxyUrl+"/api/rpc?api_key="+apiKey);
        }
        open_mrpcs++;
        return _mrpc;
    }

    private synchronized void deallocateMRPC() {
        open_mrpcs--;
        if(open_mrpcs == 0) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            if(_mrpc.mrpc != null)
                sharedPref.edit().putString(path_cache_preference_key, MRPC.gson().toJson(_mrpc.mrpc.getPathCache())).apply();
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
