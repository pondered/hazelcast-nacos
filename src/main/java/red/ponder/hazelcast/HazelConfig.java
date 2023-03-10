package red.ponder.hazelcast;


import com.alibaba.nacos.shaded.com.google.common.collect.ImmutableList;
import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryConfig;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.properties.ClusterProperty;

import red.ponder.hazelcast.nacos.NacosDiscoveryProperties;
import red.ponder.hazelcast.nacos.NacosDiscoveryStrategyFactory;

/**
 * @author ponder
 */
public final class HazelConfig {

    private final Config config;

    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    public HazelConfig(NacosDiscoveryProperties discoveryProperties) {
        this.nacosDiscoveryProperties = discoveryProperties;
        this.config = this.hazelCastConfiguration();
    }

    private Config hazelCastConfiguration() {
        final Config config = new Config();

        config.setInstanceName(nacosDiscoveryProperties.getApplicationnameOrDefault());
        config.setProperty(ClusterProperty.DISCOVERY_SPI_ENABLED.toString(), "true");
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

        final DiscoveryConfig discoveryConfig = new DiscoveryConfig();
        final NacosDiscoveryStrategyFactory factory = new NacosDiscoveryStrategyFactory();
        final DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig(factory);

        discoveryConfig.setDiscoveryStrategyConfigs(ImmutableList.of(discoveryStrategyConfig));
        config.getNetworkConfig().getJoin().setDiscoveryConfig(discoveryConfig);
        return config;
    }

    public HazelcastInstance getHazelcastInstance(final String id) {
        if (config.getMapConfig(id) == null) {
            synchronized (HazelConfig.class) {
                if (config.getMapConfig(id) == null) {
                    final EvictionConfig evictionConfig = new EvictionConfig();
                    evictionConfig.setEvictionPolicy(nacosDiscoveryProperties.getEvictionPolicyOrDefault());
                    evictionConfig.setMaxSizePolicy(nacosDiscoveryProperties.getMaxSizePolicyOrDefault());
                    evictionConfig.setSize(nacosDiscoveryProperties.getSizeOrDefault());

                    final MapConfig mapConfig = new MapConfig();
                    mapConfig.setName(id);
                    mapConfig.setEvictionConfig(evictionConfig);
                    mapConfig.setTimeToLiveSeconds(nacosDiscoveryProperties.getTimeToLiveSecondOrDefault());
                    config.addMapConfig(mapConfig);
                }
            }
        }
        return Hazelcast.getOrCreateHazelcastInstance(config);
    }
}
