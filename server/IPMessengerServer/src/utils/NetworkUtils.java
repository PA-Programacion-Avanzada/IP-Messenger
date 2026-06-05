package utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public final class NetworkUtils {
    private NetworkUtils() {
    }

    public static List<String> getLocalIPv4Addresses() {
        List<String> addresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress address = inetAddresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        addresses.add(address.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            Logger.log("No se pudieron detectar IPs locales: " + e.getMessage());
        }
        return addresses;
    }

    public static String formatLocalAccess(int port) {
        List<String> ips = getLocalIPv4Addresses();
        if (ips.isEmpty()) {
            return "Sin IP de red detectada. Usa la IP del hotspot o adaptador Wi-Fi.";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ips.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(ips.get(i)).append(':').append(port);
        }
        return builder.toString();
    }
}
