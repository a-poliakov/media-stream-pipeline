package ru.apolyakov.example.config.properties;

import lombok.Data;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.zk.ZookeeperDiscoverySpi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import ru.apolyakov.example.config.DiscoveryType;

import java.util.Collection;


@Data
@Configuration
@ConfigurationProperties(prefix = "ignite.discovery")
public class DiscoveryProperties {
    private DiscoveryType type = DiscoveryType.Tcp;

    @NestedConfigurationProperty
    private Zookeeper Zookeeper;

    @NestedConfigurationProperty
    private Tcp Tcp;

    @Data
    public static class Zookeeper {

        private String address;

        private String rootPath = ZookeeperDiscoverySpi.DFLT_ROOT_PATH;

        private long sessionTimeout = 30000;

        private long joinTimeout = 10000;
    }

    @Data
    public static class Tcp {

        private Collection<String> address;

        private int port = 47500;

        private int portRange = 20;

        private long joinTimeout = TcpDiscoverySpi.DFLT_JOIN_TIMEOUT;

        private long networkTimeout = TcpDiscoverySpi.DFLT_NETWORK_TIMEOUT;

        private int threadPriority = TcpDiscoverySpi.DFLT_THREAD_PRI;

        private int topologyHistorySize = TcpDiscoverySpi.DFLT_TOP_HISTORY_SIZE;

        private long socketTimeout = TcpDiscoverySpi.DFLT_SOCK_TIMEOUT;

        private long ackTimeout = TcpDiscoverySpi.DFLT_ACK_TIMEOUT;

        private long socketTimeoutClient = TcpDiscoverySpi.DFLT_SOCK_TIMEOUT_CLIENT;

        private long ackTimeoutClient = TcpDiscoverySpi.DFLT_ACK_TIMEOUT_CLIENT;

        private int reconnectCount = TcpDiscoverySpi.DFLT_RECONNECT_CNT;

        private int reconnectDelay = (int)TcpDiscoverySpi.DFLT_RECONNECT_DELAY;

        private long ipFinderFrequency = TcpDiscoverySpi.DFLT_IP_FINDER_CLEAN_FREQ;

        private long statsPrintFrequency = TcpDiscoverySpi.DFLT_STATS_PRINT_FREQ;

        private long maxAckTimeout = TcpDiscoverySpi.DFLT_MAX_ACK_TIMEOUT;

        private Multicast multicast;

        @Data
        public static class Multicast {

            private String group;

            private int port = TcpDiscoveryMulticastIpFinder.DFLT_MCAST_PORT;

            private int responseWaitTime = TcpDiscoveryMulticastIpFinder.DFLT_RES_WAIT_TIME;

            private int addressRequestAttempts = TcpDiscoveryMulticastIpFinder.DFLT_ADDR_REQ_ATTEMPTS;
        }
    }
}

