package net.vector57.mrpc;

import android.content.Context;
import android.os.Handler;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Vector on 11/12/2016.
 */

public class MRPC extends Thread {
    public UUID uuid;
    TransportThread transport;
    private HashMap<Integer, Result> results = new HashMap<>();
    private HashMap<String, PathCacheEntry> pathCache = new HashMap<>();
    private int id = 1;
    private Context mainContext;
    private Handler mainHandler;
    private volatile boolean running = false;
    public MRPC(Context mainContext) {
        this.setDaemon(true);
        this.mainContext = mainContext;
        mainHandler = new Handler(mainContext.getMainLooper());
        uuid = UUID.randomUUID();
    }

    @Override
    public synchronized void start() {
        try {
            transport = new SocketTransport(this, 50123);
            transport.start();
            running = true;
            super.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public synchronized void close() throws InterruptedException {
        running = false;
        join();
        transport.close();
        transport = null;
    }

    private synchronized PathCacheEntry getPathEntry(String path) {
        if(!pathCache.containsKey(path))
            pathCache.put(path, new PathCacheEntry());
        return pathCache.get(path);
    }

    private synchronized void pollResults() {
        Set<Map.Entry<Integer, Result>> f = results.entrySet();
        for(Iterator<Map.Entry<Integer, Result>> it = results.entrySet().iterator(); it.hasNext(); ) {
            Result r = it.next().getValue();
            if(r.isCompleted())
                it.remove();
            else if (r.needsResend()) {
                transport.sendAsync(r.request);
                r.markSent();
            }
        }
    }

    @Override
    public void run() {
        while(running) {
            pollResults();
        }
    }

    public void RPC(String path, Object value) {
        RPC(path, value, null);
    }
    public synchronized void RPC(String path, Object value, Result.Callback callback) {
        Set<String> requiredResponses = getPathEntry(path).onSend();
        Message.Request m = new Message.Request(id, uuid.toString(), path, value);
        results.put(id, new Result(requiredResponses, m, callback));
        transport.sendAsync(m);
        id++;
    }
    public synchronized void onReceive(Message message) {
        if(message.dst.equals(uuid.toString())) {
            if (message instanceof Message.Response) {
                Message.Response response = (Message.Response) message;
                Result r = results.get(message.id);
                if (r != null) {
                    getPathEntry(r.request.dst).onRecv(message.src);
                    r.resolve(mainHandler, response);
                }
            } else if (message instanceof Message.Request) {

            }
        }
    }
}
