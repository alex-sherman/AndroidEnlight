package net.vector57.mrpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Vector on 11/12/2016.
 */

public class MRPC extends Thread {
    public UUID uuid;
    protected SocketTransport transport;
    protected HashMap<Integer, Result> results = new HashMap<>();
    private HashMap<String, PathCacheEntry> pathCache = new HashMap<>();
    protected int id = 1;

    private static Gson _gson;

    public static Gson gson() {
        if(_gson == null) {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Message.class, new MessageTypeAdapter());
            _gson = builder.create();
        }
        return _gson;
    }

    private volatile boolean running = false;
    public MRPC(InetAddress broadcastAddress, Map<String, List<PathCacheEntry.UUIDEntry>> pathCache) throws SocketException {
        this(broadcastAddress);
        if(pathCache != null) {
            for (Map.Entry<String, List<PathCacheEntry.UUIDEntry>> entry : pathCache.entrySet()) {
                this.pathCache.put(entry.getKey(), new PathCacheEntry(entry.getValue()));
            }
        }
    }

    public MRPC(InetAddress broadcastAddress) throws SocketException {
        transport = new SocketTransport(this, broadcastAddress, 50123);
        uuid = UUID.randomUUID();

        running = true;
        this.start();
        transport.start();
    }

    public void close() {
        running = false;
        try {
            transport.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        transport = null;
        try {
            this.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected synchronized PathCacheEntry getPathEntry(String path) {
        if(!pathCache.containsKey(path))
            pathCache.put(path, new PathCacheEntry());
        return pathCache.get(path);
    }

    public synchronized HashMap<String, List<PathCacheEntry.UUIDEntry>> getPathCache() {
        HashMap<String, List<PathCacheEntry.UUIDEntry>> output = new HashMap<>();
        for (Map.Entry<String, PathCacheEntry> entry : pathCache.entrySet()) {
            output.put(entry.getKey(), new ArrayList<>(entry.getValue().getUUIDs()));
        }
        return output;
    }

    private synchronized void pollResults() {
        for(Iterator<Map.Entry<Integer, Result>> it = results.entrySet().iterator(); it.hasNext(); ) {
            Result r = it.next().getValue();
            if(r.isCompleted())
                it.remove();
            else if (r.needsResend()) {
                transport.send(r.request, r.remainingAddresses());
                r.markSent();
            }
        }
    }

    @Override
    public void run() {
        while(running) {
            pollResults();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void RPC(String path, Object value) {
        RPC(path, value, null, true);
    }
    public synchronized void RPC(String path, Object value, Result.Callback callback) {
        RPC(path, value, callback, true);
    }
    public synchronized void RPC(String path, Object value, Result.Callback callback, boolean resend) {
        if(transport != null) {
            Collection<PathCacheEntry.UUIDEntry> requiredResponses = resend ? getPathEntry(path).onSend() : new ArrayList<PathCacheEntry.UUIDEntry>();
            Message.Request m = new Message.Request(id, uuid.toString(), path, value);
            results.put(id, new Result(requiredResponses, m, callback));
            transport.send(m, getPathEntry(path).getAddresses());
            id++;
        }
    }
    public synchronized void onReceive(Message message, InetAddress source) {
        if(message.dst.equals(uuid.toString())) {
            if (message instanceof Message.Response) {
                Message.Response response = (Message.Response) message;
                Result r = results.get(message.id);
                if (r != null) {
                    getPathEntry(r.request.dst).onRecv(message.src, source);
                    r.resolve(response);
                }
            } else if (message instanceof Message.Request) {

            }
        }
    }
}
