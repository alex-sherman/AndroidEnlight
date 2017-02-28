package net.vector57.mrpc;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Vector on 11/14/2016.
 */

public class PathCacheEntry {
    private static final long TIMEOUT = 1000;

    public static class UUIDEntry {
        private transient Long lastReceived;
        String uuid;
        InetAddress address;
        UUIDEntry() { this(null, 0L, null); }
        UUIDEntry(String uuid, Long lastReceived, InetAddress address) {
            this.uuid = uuid;
            this.lastReceived = lastReceived;
            this.address = address;
        }
        boolean isStale(Long sendTime) {
            return lastReceived != 0 && sendTime - lastReceived >= TIMEOUT;
        }
    }
    private HashMap<String, UUIDEntry> entries = new HashMap<>();
    PathCacheEntry() { }
    PathCacheEntry(List<UUIDEntry> entries) {
        for (UUIDEntry entry : entries) {
            this.entries.put(entry.uuid, entry);
        }
    }
    synchronized Collection<UUIDEntry> onSend() {
        Long sendTime = System.currentTimeMillis();
        Iterator<Map.Entry<String, UUIDEntry>> iter = entries.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, UUIDEntry> entry = iter.next();
            //A value of 0 denotes a response was previously received in time
            //TODO: If new messages are being sent to an entry at a rate faster than TIMEOUT it will never be removed from the cache
            if(!entry.getValue().isStale(sendTime))
                entry.getValue().lastReceived = sendTime;
            else
                iter.remove();
        }
        return getUUIDs();
    }
    synchronized void onRecv(String uuid, InetAddress source) {
        entries.put(uuid, new UUIDEntry(uuid, 0L, source));
    }
    public List<InetAddress> getAddresses() {
        ArrayList<InetAddress> output = new ArrayList<>();
        for(UUIDEntry entry : entries.values()) {
            if(entry.address != null)
                output.add(entry.address);
        }
        return output;
    }
    synchronized Collection<UUIDEntry> getUUIDs() {
        return new ArrayList<UUIDEntry>(entries.values());
    }
}
