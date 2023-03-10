package red.ponder.hazelcast.nacos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.config.properties.PropertyTypeConverter;
import com.hazelcast.config.properties.SimplePropertyDefinition;

/**
 * @author ponder
 * Hazelcast连接Nacos配置
 * 当不配置时获取Nacos配置
 */
@Configuration
public class NacosDiscoveryProperties {

    private final Environment environment;

    /**
     * 最大缓存数量
     */
    @Value("${spring.cloud.nacos.hazelcast.size}")
    private Integer size;

    /**
     * 最大缓存时间
     */
    @Value("${spring.cloud.nacos.hazelcast.time.to.live}")
    private Integer timeToLive;

    /**
     * 缓存策略
     */
    @Value("${spring.cloud.nacos.hazelcast.policy}")
    private EvictionPolicy policy;

    @Value("${spring.cloud.nacos.hazelcast.max.size.policy}")
    private MaxSizePolicy maxSizePolicy;

    /**
     * Connection string to your Nacos server.
     * Default: There is no default, this is a required property.
     * Example: 127.0.0.1:8848
     */
    @Value("${spring.cloud.nacos.hazelcast.server.address}")
    private String serverAddr;

    /**
     * instance.setServiceName(applicationName)
     */
    @Value("${spring.cloud.nacos.hazelcast.server.name}")
    private String applicationName;

    /**
     * Namespace in Nacos Hazelcast will use
     * Default: discovery-hazelcast
     */
    private String namespace;

    /**
     * Name of this Hazelcast cluster. You can have multiple distinct clusters to use the
     * same Nacos installation.
     */
    @Value("${spring.cloud.nacos.hazelcast.server.cluster.name}")
    private String clusterName;

    @Value("${spring.cloud.nacos.hazelcast.server.username}")
    private String username;

    @Value("${spring.cloud.nacos.hazelcast.server.password}")
    private String password;

    @Value("${spring.cloud.nacos.hazelcast.server.access.key}")
    private String accessKey;

    @Value("${spring.cloud.nacos.hazelcast.secret.key}")
    private String secretKey;

    public NacosDiscoveryProperties(final Environment environment) {
        this.environment = environment;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NacosDiscoveryProperties.class.getSimpleName() + "[", "]")
            .add("environment=" + environment)
            .add("size=" + size)
            .add("timeToLive=" + timeToLive)
            .add("policy=" + policy)
            .add("maxSizePolicy=" + maxSizePolicy)
            .add("serverAddr='" + serverAddr + "'")
            .add("applicationName='" + applicationName + "'")
            .add("namespace='" + namespace + "'")
            .add("clusterName='" + clusterName + "'")
            .add("username='" + username + "'")
            .add("password='" + password + "'")
            .add("accessKey='" + accessKey + "'")
            .add("secretKey='" + secretKey + "'")
            .toString();
    }

    public final List<PropertyDefinition> getPropertyDefinition() {
        List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
        propertyDefinitions.add(property(getServerAddrOrDefault()));
        propertyDefinitions.add(property(getNamespaceOrDefault()));
        propertyDefinitions.add(property(getClusterNameOrDefault()));
        propertyDefinitions.add(property(getApplicationnameOrDefault()));
        propertyDefinitions.add(property(getUsernameOrDefault()));
        propertyDefinitions.add(property(getPasswordOrDefault()));
        propertyDefinitions.add(property(getAccessKeyOrDefault()));
        propertyDefinitions.add(property(getSecretKeyOrDefault()));
        return propertyDefinitions;
    }

    private PropertyDefinition property(String key) {
        return new SimplePropertyDefinition(key, true, PropertyTypeConverter.STRING, null);
    }

    public final EvictionPolicy getEvictionPolicyOrDefault() {
        return Optional.ofNullable(policy).orElse(EvictionPolicy.LRU);
    }

    public final MaxSizePolicy getMaxSizePolicyOrDefault() {
        return Optional.ofNullable(maxSizePolicy).orElse(MaxSizePolicy.PER_NODE);
    }

    public final Integer getSizeOrDefault() {
        return Optional.ofNullable(size).orElse(5000);
    }

    public final Integer getTimeToLiveSecondOrDefault() {
        return Optional.ofNullable(timeToLive).orElse(300);
    }

    public final String getServerAddrOrDefault() {
        return Optional.ofNullable(serverAddr).orElseGet(() -> environment.getProperty("spring.cloud.nacos.discovery.server-addr"));
    }

    public final String getApplicationnameOrDefault() {
        return Optional.ofNullable(applicationName).orElseGet(() -> environment.getProperty("spring.application.name") + "-hazelcast-instance");
    }

    public final String getNamespaceOrDefault() {
        return Optional.ofNullable(environment.getProperty("spring.cloud.nacos.discovery.namespace")).orElseGet(() -> "discovery-hazelcast");
    }

    public final String getClusterNameOrDefault() {
        return Optional.ofNullable(clusterName).orElseGet(() -> "hazelcast");
    }

    public final String getUsernameOrDefault() {
        return Optional.ofNullable(username).orElseGet(() -> environment.getProperty("spring.cloud.nacos.discovery.username"));
    }

    public final String getPasswordOrDefault() {
        return Optional.ofNullable(password).orElseGet(() -> environment.getProperty("spring.cloud.nacos.discovery.password"));
    }

    public final String getAccessKeyOrDefault() {
        return Optional.ofNullable(accessKey).orElseGet(() -> System.getenv("NACOS_REGISTRY_ACCESS_KEY"));
    }

    public final String getSecretKeyOrDefault() {
        return Optional.ofNullable(secretKey).orElseGet(() -> System.getenv("NACOS_REGISTRY_SECRET_KEY"));
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(final Integer size) {
        this.size = size;
    }

    public Integer getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(final Integer timeToLive) {
        this.timeToLive = timeToLive;
    }

    public EvictionPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(final EvictionPolicy policy) {
        this.policy = policy;
    }

    public MaxSizePolicy getMaxSizePolicy() {
        return maxSizePolicy;
    }

    public void setMaxSizePolicy(final MaxSizePolicy maxSizePolicy) {
        this.maxSizePolicy = maxSizePolicy;
    }

    public final String getServerAddr() {
        return serverAddr;
    }

    public final void setServerAddr(final String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public final String getApplicationName() {
        return applicationName;
    }

    public final void setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
    }

    public final String getNamespace() {
        return namespace;
    }

    public final void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public final String getClusterName() {
        return clusterName;
    }

    public final void setClusterName(final String clusterName) {
        this.clusterName = clusterName;
    }

    public final String getUsername() {
        return username;
    }

    public final void setUsername(final String username) {
        this.username = username;
    }

    public final String getPassword() {
        return password;
    }

    public final void setPassword(final String password) {
        this.password = password;
    }

    public final String getAccessKey() {
        return accessKey;
    }

    public final void setAccessKey(final String accessKey) {
        this.accessKey = accessKey;
    }

    public final String getSecretKey() {
        return secretKey;
    }

    public final void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NacosDiscoveryProperties that = (NacosDiscoveryProperties) o;
        return Objects.equals(environment, that.environment) && Objects.equals(size, that.size) && Objects.equals(
            timeToLive, that.timeToLive) && policy == that.policy && maxSizePolicy == that.maxSizePolicy && Objects.equals(serverAddr,
            that.serverAddr) && Objects.equals(applicationName, that.applicationName) && Objects.equals(namespace, that.namespace)
            && Objects.equals(clusterName, that.clusterName) && Objects.equals(username, that.username) && Objects.equals(
            password, that.password) && Objects.equals(accessKey, that.accessKey) && Objects.equals(secretKey, that.secretKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(environment, size, timeToLive, policy, maxSizePolicy, serverAddr, applicationName, namespace, clusterName, username,
            password,
            accessKey, secretKey);
    }
}
