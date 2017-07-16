package net.vector57.android.mrpc;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.vector57.mrpc.MRPC;
import net.vector57.mrpc.Message;
import net.vector57.mrpc.PathCacheEntry;
import net.vector57.mrpc.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vector on 11/12/2016.
 */

public class AndroidMRPC {
    private Handler mainHandler;
    private VolleyMRPC volleyMRPC;
    private String proxyUrl;
    public MRPC mrpc;


    public AndroidMRPC(Context mainContext, InetAddress broadcastAddress, Map<String, List<PathCacheEntry.UUIDEntry>> pathCache) throws SocketException {
        mrpc = new MRPC(broadcastAddress, pathCache);
        Init(mainContext);
    }

    public AndroidMRPC(Context mainContext, String proxyURL) {
        volleyMRPC = new VolleyMRPC(mainContext);
        this.proxyUrl = proxyURL;
        Init(mainContext);
    }

    void Init(Context ctx) {
        mainHandler = new Handler(ctx.getMainLooper());
    }

    public void RPC(final String path, final Object value, final Result.Callback callback, final boolean resend) {
        final Result.Callback wrappedCallback = new Result.Callback() {
            @Override
            public void onResult(final Message.Response response) {
                mainHandler.post(new Runnable() {
                     @Override
                     public void run() {
                         callback.onResult(response);
                     }
                });

            }
        };
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                if(volleyMRPC != null)
                    volleyMRPC.rpc(proxyUrl, path, value, callback);
                else if(mrpc != null)
                    mrpc.RPC(path, value, callback == null ? null : wrappedCallback, resend);
                return null;
            }
        }.execute();
    }

    public void close() {
        if(mrpc != null)
            mrpc.close();
    }
}
