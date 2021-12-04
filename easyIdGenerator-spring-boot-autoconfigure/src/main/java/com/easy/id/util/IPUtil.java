package com.easy.id.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @Description ip工具类
 * @createTime 2020年06月02日
 */
public enum IPUtil {
    ;

    public static String getHostAddress() throws SocketException {
        return getHostAddress(null).get(0);
    }

    /**
     * 获取已激活网卡的ip地址
     *
     * @param interfaceName 网卡地址，null则获取所有
     * @return List<IP>
     */
    public static List<String> getHostAddress(String interfaceName) throws SocketException {
        List<String> ips = new ArrayList<>(5);
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> allAddress = networkInterface.getInetAddresses();
            while (allAddress.hasMoreElements()) {
                InetAddress address = allAddress.nextElement();
                if (address.isLoopbackAddress()) {
                    continue;
                }
                if (address instanceof Inet6Address) {
                    continue;
                }
                String hostAddress = address.getHostAddress();
                if (null == interfaceName) {
                    ips.add(hostAddress);
                } else if (interfaceName.equals(networkInterface.getDisplayName())) {
                    ips.add(hostAddress);
                }
            }
        }
        return ips;
    }
}
