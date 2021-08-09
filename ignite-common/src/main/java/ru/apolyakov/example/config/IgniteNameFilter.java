package ru.apolyakov.example.config;

import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.lang.IgnitePredicate;

public class IgniteNameFilter implements IgnitePredicate<ClusterNode> {
    private static final long serialVersionUID = 6026245313662239804L;
    private final String serviceType;

    public IgniteNameFilter(String serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public boolean apply(ClusterNode clusterNode) {
        String instanceName = clusterNode.attribute("org.apache.ignite.ignite.name");
        return serviceType.equals(instanceName);
    }
}
