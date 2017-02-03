package net.vector57.android.mrpc;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;

/**
 * Created by peter on 2/3/17.
 */

public class Util {
    public static InetAddress getBroadcastAddress(Context ctx) throws IOException {
        WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();

        InetAddress inetAddress = InetAddress.getByAddress(extractBytes(dhcp.ipAddress));
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
        if(networkInterface != null) {
            for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                if (address.getAddress().equals(inetAddress)) {
                    return address.getBroadcast();
                }
            }
        }

        return null;
    }
    private static byte[] extractBytes(int addr) {
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) (addr >> (k * 8));
        return quads;
    }
}
