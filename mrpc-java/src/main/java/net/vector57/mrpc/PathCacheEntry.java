package net.vector57.mrpc;

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
    public static final long TIMEOUT = 1000;
    private HashMap<String, Long> entries = new HashMap<>();
    public PathCacheEntry() { }
    public PathCacheEntry(List<String> entries) {
        for (String uuid : entries) {
            this.entries.put(uuid, 0L);
        }
    }
    public synchronized Set<String> onSend() {
        Long sendTime = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iter = entries.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Long> entry = iter.next();
            //A value of 0 denotes a response was previously received in time
            if(entry.getValue() == 0)
                entries.put(entry.getKey(), sendTime);
            else if(sendTime - entry.getValue() > TIMEOUT)
                iter.remove();
        }
        return getUUIDs();
    }
    public synchronized void onRecv(String uuid) {
        entries.put(uuid, 0L);
    }
    public synchronized Set<String> getUUIDs() {
        return new HashSet<String>(entries.keySet());
    }
}
