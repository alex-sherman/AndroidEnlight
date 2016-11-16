package net.vector57.mrpc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Vector on 11/14/2016.
 */

public class PathCacheEntry {
    public static final long TIMEOUT = 1000;
    private HashMap<String, Long> entries = new HashMap<>();
    public synchronized Set<String> onSend() {
        Long sendTime = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry: entries.entrySet()) {
            //A value of 0 denotes a response was previously received in time
            if(entry.getValue() == 0)
                entries.put(entry.getKey(), sendTime);
            else if(sendTime - entry.getValue() > TIMEOUT)
                entries.remove(entry.getValue());
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
