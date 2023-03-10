# Hazelcast-nacos

hazelcast使用Nacos当注册中心

## 使用方法
**目前仅提供源码方式**

### 用来当MyBatis的二级缓存
1. 下载源码复制放入到包中 
2. 配置MyBatis二级缓存
```java
@Slf4j
public class MybatisCache implements Cache {
    private final HazelcastInstance hazelcastInstance;
    private final String id;

    public MybatisCache(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Hazelcast Cache instances require an ID");
        }
        this.hazelcastInstance = new HazelConfig().getHazelcastInstance(id);
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void putObject(Object key, Object value) {
        if (value != null) {
            hazelcastInstance.getMap(this.id).put(key, value, 600, TimeUnit.SECONDS);
        } else {
            if (this.hazelcastInstance.getMap(this.getId()).containsKey(key)) {
                this.hazelcastInstance.getMap(this.id).remove(key);
            }
        }

    }

    @Override
    public Object getObject(Object key) {
        return hazelcastInstance.getMap(this.id).get(key);
    }

    @Override
    public Object removeObject(Object key) {
        return hazelcastInstance.getMap(this.id).remove(key);
    }

    @Override
    public void clear() {
        hazelcastInstance.getMap(this.id).clear();
    }

    @Override
    public int getSize() {
        return hazelcastInstance.getMap(this.id).size();
    }
}
```
3. 在Mapper的Java类上添加
```java
@CacheNamespace(implementation = MybatisCache.class, eviction = MybatisCache.class)
```
4. 最后,如果在xml中添加
```xml
<cache-ref namespace="当前mapper.xml文件所对应的Mapper.java所在的文件"/>
```
