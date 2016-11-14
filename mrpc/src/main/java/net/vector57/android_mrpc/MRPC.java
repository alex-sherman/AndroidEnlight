package net.vector57.android_mrpc;

import android.content.Context;
import android.os.Handler;

import java.net.SocketException;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Vector on 11/12/2016.
 */

public class MRPC {
    public UUID uuid;
    TransportThread transport;
    private HashMap<Integer, Result> results = new HashMap<>();
    private int id = 1;
    private Context mainContext;
    private Handler mainHandler;
    public MRPC(Context mainContext) {
        this.mainContext = mainContext;
        mainHandler = new Handler(mainContext.getMainLooper());
        uuid = UUID.randomUUID();
        try {
            transport = new SocketTransport(this, 50123);
            transport.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
    public void RPC(String path, Object value) {
        RPC(path, value, null);
    }
    public void RPC(String path, Object value, Result.Callback callback) {
        if(callback != null) {
            results.put(id, new Result(callback));
        }
        Message m = new Message.Request(id, uuid.toString(), path, value);
        transport.sendAsync(m);
        id++;
    }
    public void onReceive(Message message) {
        if(message.dst.equals(uuid.toString())) {
            if (message instanceof Message.Response) {
                Message.Response response = (Message.Response) message;
                Result r = results.get(message.id);
                if (r != null) {
                    r.resolve(mainHandler, response);
                }
            } else if (message instanceof Message.Request) {

            }
        }
    }
}
