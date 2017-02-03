package net.vector57.android.mrpc;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import net.vector57.mrpc.Message;
import net.vector57.mrpc.Result;
import net.vector57.mrpc.SocketTransport;
import net.vector57.mrpc.TransportThread;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Vector on 11/12/2016.
 */

public class MRPC extends net.vector57.mrpc.MRPC {
    private Handler mainHandler;
    private AsyncTask<Message, Boolean, Void> sendTask(final TransportThread transport) {
        return new AsyncTask<Message, Boolean, Void>() {
            @Override
            protected Void doInBackground(Message... params) {
                transport.send(params[0]);
                return null;
            }
        };
    }

    public MRPC(Context mainContext, InetAddress broadcastAddress, Map<String, List<String>> pathCache) throws SocketException {
        super(broadcastAddress, pathCache);
        mainHandler = new Handler(mainContext.getMainLooper());
    }

    public MRPC(Context mainContext, InetAddress broadcastAddress) throws SocketException {
        super(broadcastAddress);
        mainHandler = new Handler(mainContext.getMainLooper());
    }

    public synchronized void RPC(String path, Object value, final Result.Callback callback, boolean resend) {
        if(transport != null) {
            Set<String> requiredResponses = resend ? getPathEntry(path).onSend() : new HashSet<String>();
            Message.Request m = new Message.Request(id, uuid.toString(), path, value);
            Result.Callback wrappedCallback = new Result.Callback() {
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
            results.put(id, new Result(requiredResponses, m, wrappedCallback));
            sendTask(transport).execute(m);
            id++;
        }
    }
}
