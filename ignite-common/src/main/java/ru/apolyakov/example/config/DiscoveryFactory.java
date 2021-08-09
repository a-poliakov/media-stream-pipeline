package ru.apolyakov.example.config;

import com.google.common.base.Strings;
import org.apache.ignite.internal.managers.discovery.IgniteDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.spi.discovery.zk.ZookeeperDiscoverySpi;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.apolyakov.example.config.properties.DiscoveryProperties;

@Component
public class DiscoveryFactory {

    private DiscoveryProperties properties;

    public DiscoveryFactory(DiscoveryProperties properties) {
        this.properties = properties;
    }

    public IgniteDiscoverySpi create() {
        switch (properties.getType()) {
            case Tcp:
                return getTcpDiscoverySpi();
            case Zookeeper:
                return getZookeeperDiscoverySpi();
        }

        throw new IllegalStateException();
    }

    @NotNull
    private TcpDiscoverySpi getTcpDiscoverySpi() {

        DiscoveryProperties.Tcp tcp = properties.getTcp();

        TcpDiscoverySpi spi = new TcpDiscoverySpi();

        spi.setLocalPort(tcp.getPort());
        spi.setLocalPortRange(tcp.getPortRange());

        spi.setJoinTimeout(tcp.getJoinTimeout());
        spi.setNetworkTimeout(tcp.getNetworkTimeout());
        spi.setThreadPriority(tcp.getThreadPriority());
        spi.setTopHistorySize(tcp.getTopologyHistorySize());
        spi.setSocketTimeout(tcp.getSocketTimeout());
        spi.setAckTimeout(tcp.getAckTimeout());
        spi.setReconnectCount(tcp.getReconnectCount());
        spi.setReconnectDelay(tcp.getReconnectDelay());
        spi.setIpFinderCleanFrequency(tcp.getIpFinderFrequency());
        spi.setStatisticsPrintFrequency(tcp.getStatsPrintFrequency());
        spi.setMaxAckTimeout(tcp.getMaxAckTimeout());

        if (properties.getTcp().getMulticast() == null
                || Strings.isNullOrEmpty(properties.getTcp().getMulticast().getGroup())) {

            TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();

            ipFinder.setAddresses(properties.getTcp().getAddress());

            spi.setIpFinder(ipFinder);

        } else {

            DiscoveryProperties.Tcp.Multicast multicast = properties.getTcp().getMulticast();

            TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();

            ipFinder.setMulticastGroup(multicast.getGroup());
            ipFinder.setMulticastPort(multicast.getPort());
            ipFinder.setResponseWaitTime(multicast.getResponseWaitTime());
            ipFinder.setAddressRequestAttempts(multicast.getAddressRequestAttempts());
            ipFinder.setAddresses(properties.getTcp().getAddress());

            spi.setIpFinder(ipFinder);
        }

        return spi;
    }

    @NotNull
    private ZookeeperDiscoverySpi getZookeeperDiscoverySpi() {
        DiscoveryProperties.Zookeeper zookeeper = properties.getZookeeper();

        ZookeeperDiscoverySpi spi = new ZookeeperDiscoverySpi();

        spi.setZkConnectionString(zookeeper.getAddress());
        spi.setZkRootPath(zookeeper.getRootPath());
        spi.setJoinTimeout(zookeeper.getJoinTimeout());
        spi.setSessionTimeout(zookeeper.getSessionTimeout());

        return spi;
    }
}
