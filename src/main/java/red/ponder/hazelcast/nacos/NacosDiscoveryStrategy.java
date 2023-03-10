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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.utils.InitUtils;
import com.hazelcast.cluster.Address;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;

import red.ponder.hazelcast.utils.SpringUtil;

/**
 * Implementation for Nacos Discovery Strategy
 *
 * @author Ponder
 */
public class NacosDiscoveryStrategy extends AbstractDiscoveryStrategy {
    private final DiscoveryNode thisNode;

    private final ILogger logger;

    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    private List<String> clusters = new ArrayList<>();

    private NamingService namingService;

    private Instance instance;

    public NacosDiscoveryStrategy(final DiscoveryNode discoveryNode, final ILogger logger, final Map<String, Comparable> properties,
                                  final NacosDiscoveryProperties nacosDiscoveryProperties) {
        super(logger, properties);
        this.thisNode = discoveryNode;
        this.logger = logger;
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    }

    private boolean isMember() {
        return thisNode != null;
    }

    @Override
    public void start() {
        try {
            startCuratorClient();
        } catch (NacosException e) {
            logger.severe("服务启动失败", e);
        }

        try {
            instance = new Instance();
            if (isMember()) {
                //register members only into nacos
                //there no need to register clients
                prepareServiceInstance();
                namingService.registerInstance(nacosDiscoveryProperties.getApplicationnameOrDefault(), instance);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error while talking to Nacos. ", e);
        }
    }

    private void prepareServiceInstance() {
        final Address privateAddress = thisNode.getPrivateAddress();
        instance.setIp(privateAddress.getHost());
        instance.setPort(privateAddress.getPort());
        instance.setClusterName(nacosDiscoveryProperties.getClusterNameOrDefault());
        final Map<String, String> instanceMeta = new HashMap<>(1);
        instanceMeta.put("cluster", nacosDiscoveryProperties.getClusterNameOrDefault());
        instance.setMetadata(instanceMeta);
    }

    private void startCuratorClient() throws NacosException {
        final NacosDiscoveryProperties discoveryProperties = SpringUtil.getBean(NacosDiscoveryProperties.class);

        clusters.add(discoveryProperties.getClusterNameOrDefault());

        if (discoveryProperties.getServerAddrOrDefault() == null) {
            throw new IllegalStateException("Nacos ServerAddr cannot be null.");
        }
        logger.finest(String.format("Using %s as Nacos URL, namespace is %s", discoveryProperties.getServerAddrOrDefault(),
            discoveryProperties.getNamespaceOrDefault()));

        final Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, discoveryProperties.getServerAddrOrDefault());
        properties.setProperty(PropertyKeyConst.NAMESPACE, discoveryProperties.getNamespaceOrDefault());
        properties.setProperty(PropertyKeyConst.USERNAME, discoveryProperties.getUsernameOrDefault());
        properties.setProperty(PropertyKeyConst.PASSWORD, discoveryProperties.getPasswordOrDefault());
        properties.setProperty(PropertyKeyConst.ACCESS_KEY, discoveryProperties.getAccessKeyOrDefault());
        properties.setProperty(PropertyKeyConst.SECRET_KEY, discoveryProperties.getSecretKeyOrDefault());

        InitUtils.initEndpoint(properties);

        namingService = NamingFactory.createNamingService(properties);
    }

    @Override
    public Iterable<DiscoveryNode> discoverNodes() {
        try {
            final List<Instance> members = namingService.getAllInstances(nacosDiscoveryProperties.getApplicationnameOrDefault(), clusters, true);
            final List<DiscoveryNode> nodes = new ArrayList<>(members.size());
            for (Instance member : members) {
                Address address = new Address(member.getIp(), member.getPort());
                nodes.add(new SimpleDiscoveryNode(address));
            }
            return nodes;
        } catch (Exception e) {
            throw new IllegalStateException("Error while talking to Nacos", e);
        }
    }

    @Override
    public void destroy() {
        try {
            if (isMember() && namingService != null) {
                namingService.deregisterInstance(nacosDiscoveryProperties.getApplicationnameOrDefault(), instance);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error while talking to Nacos", e);
        }
    }
}
