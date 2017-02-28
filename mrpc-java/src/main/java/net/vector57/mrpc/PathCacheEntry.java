package net.vector57.mrpc;

import java.net.InetAddress;
import java.util.ArrayList;
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
    // If only Java had built in Tuples...
    private static class UUIDEntry {
        Long lastReceived;
        InetAddress address;
        UUIDEntry() {
            this(0L, null);
        }
        UUIDEntry(Long lastReceived, InetAddress address) {
            this.lastReceived = lastReceived;
            this.address = address;
        }
        public boolean isStale(Long sendTime) {
            return lastReceived != 0 && sendTime - lastReceived >= TIMEOUT;
        }
    }
    private HashMap<String, UUIDEntry> entries = new HashMap<>();
    PathCacheEntry() { }
    PathCacheEntry(List<String> entries) {
        for (String uuid : entries) {
            this.entries.put(uuid, new UUIDEntry());
        }
    }
    synchronized Set<String> onSend() {
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
        entries.put(uuid, new UUIDEntry(0L, source));
    }
    public List<InetAddress> getAddresses() {
        ArrayList<InetAddress> output = new ArrayList<>();
        for(UUIDEntry entry : entries.values()) {
            if(entry.address != null)
                output.add(entry.address);
        }
        return output;
    }
    synchronized Set<String> getUUIDs() {
        return new HashSet<String>(entries.keySet());
    }
}
