package net.vector57.android.mrpc;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import net.vector57.mrpc.MRPC;
import net.vector57.mrpc.Message;
import net.vector57.mrpc.PathCacheEntry;
import net.vector57.mrpc.Result;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.Map;

/**
 * Created by Vector on 11/12/2016.
 */

public class AndroidMRPC extends MRPC {
    private Handler mainHandler;

    public AndroidMRPC(Context mainContext, InetAddress broadcastAddress, Map<String, List<PathCacheEntry.UUIDEntry>> pathCache) throws SocketException {
        super(broadcastAddress, pathCache);
        mainHandler = new Handler(mainContext.getMainLooper());
    }

    public AndroidMRPC(Context mainContext, InetAddress broadcastAddress) throws SocketException {
        super(broadcastAddress);
        mainHandler = new Handler(mainContext.getMainLooper());
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
                AndroidMRPC.super.RPC(path, value, callback == null ? null : wrappedCallback, resend);
                return null;
            }
        }.execute();

    }
}
