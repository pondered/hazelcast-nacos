/*
 * Copyright 2020 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package red.ponder.hazelcast.nacos;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;

import red.ponder.hazelcast.utils.SpringUtil;

/**
 * Factory class for NacosDiscoveryStrategy
 *
 * @author ponder
 */
public class NacosDiscoveryStrategyFactory implements DiscoveryStrategyFactory {

    private volatile Collection<PropertyDefinition> PROPERTY_DEFINITIONS = Collections.emptyList();

    private NacosDiscoveryProperties discoveryProperties;

    @Override
    public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
        return NacosDiscoveryStrategy.class;
    }

    @Override
    public DiscoveryStrategy newDiscoveryStrategy(final DiscoveryNode discoveryNode, final ILogger logger,
                                                  final Map<String, Comparable> properties) {
        return new NacosDiscoveryStrategy(discoveryNode, logger, properties, discoveryProperties);
    }

    @Override
    public Collection<PropertyDefinition> getConfigurationProperties() {
        if (PROPERTY_DEFINITIONS == null || PROPERTY_DEFINITIONS.isEmpty()) {
            synchronized (NacosDiscoveryStrategyFactory.class) {
                if (PROPERTY_DEFINITIONS == null || PROPERTY_DEFINITIONS.isEmpty()) {
                    discoveryProperties = SpringUtil.getBean(NacosDiscoveryProperties.class);
                    PROPERTY_DEFINITIONS = Collections.unmodifiableCollection(discoveryProperties.getPropertyDefinition());
                }
            }
        }
        return PROPERTY_DEFINITIONS;
    }
}
