package red.ponder.hazelcast.nacos.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.nacos.api.utils.StringUtils;


/**
 * @author tanghuang@sunline.cn create on 2019/9/21
 */
public final class NetUtils {
    private static final Logger log = LoggerFactory.getLogger(NetUtils.class);

    private static final String LO_NETWORK_INTERFACE = "lo";

    private static final InetAddress ANY_IPV4_ADDRESS = getAddress("0.0.0.0");

    private static final InetAddress ANY_IPV6_ADDRESS = getAddress("::");

    private NetUtils() {
    }

    /**
     * 根据主机名获得地址
     */
    public static InetAddress getAddress(final String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unknown host: " + host, e);
        }
    }

    /**
     * 判断是否是0:0:0:0
     */
    public static boolean isAnyAddress(final String host) {
        try {
            final InetAddress address = InetAddress.getByName(host);
            return Objects.equals(address, ANY_IPV4_ADDRESS)
                || Objects.equals(address, ANY_IPV6_ADDRESS);
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 是否回环地址
     */
    public static boolean isLOAddress(final String host) {
        try {
            return NetUtils.hasText(host)
                && isLOAddress(InetAddress.getByName(host));
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 是否回环地址
     */
    public static boolean isLOAddress(final InetAddress address) {
        try {
            final NetworkInterface network = NetworkInterface.getByInetAddress(address);
            return network != null && Objects.equals(LO_NETWORK_INTERFACE, network.getName());
        } catch (SocketException e) {
            return false;
        }
    }

    public static boolean hasText(final String str) {
        return (str != null && !str.isEmpty() && NetUtils.containsText(str));
    }

    private static boolean containsText(final CharSequence str) {
        final int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获得可用的地址
     */
    public static InetAddress getAvailableAddress(final String host, final int port, final int timeoutMillis) throws IOException {
        return getAvailableAddress(new InetSocketAddress(host, port), timeoutMillis);
    }

    /**
     * 获得可用的地址
     */
    public static InetAddress getAvailableAddress(final InetSocketAddress targetAddress, final int timeoutMillis) throws IOException {
        try (final Socket socket = new Socket()) {
            socket.connect(targetAddress, timeoutMillis);
            return socket.getLocalAddress();
        }
    }

    /**
     * 测试本机端口是否被使用
     */
    public static boolean isLocalPortUsing(final int port) {
        return isPortUsing("127.0.0.1", port);
    }

    /**
     * 测试主机Host的port端口是否被使用
     */
    public static boolean isPortUsing(final String host, final int port) {
        boolean flag = false;
        try (final Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 3000);
            flag = true;
        } catch (IOException e) {
            log.debug("获取端口异常", e);
        }
        return flag;
    }

    /**
     * 获得可用的端口
     */
    public static int getAvailablePort(final Integer startPort, final String portStr) {
        int port = startPort;
        if (!StringUtils.isEmpty(portStr)) {
            port = Integer.parseInt(portStr);
        }
        while (true) {
            if (!NetUtils.isLocalPortUsing(port)) {
                return port;
            }
            port++;
        }
    }

}
