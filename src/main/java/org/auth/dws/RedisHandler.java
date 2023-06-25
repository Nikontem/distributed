package org.auth.dws;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class RedisHandler {

    private final JedisPool redis;

    public RedisHandler(String host, int port) {
        JedisClientConfig config = DefaultJedisClientConfig.builder()
                .socketTimeoutMillis(10000000)
                .timeoutMillis(10000000)
                .connectionTimeoutMillis(10000000)
                .build();
        HostAndPort address = new HostAndPort(host, port);


        redis = new JedisPool(address, config);
    }

    public void consolidate(Map<String, String> entries) {
        try (Jedis jedis = redis.getResource()) {

            entries.entrySet()
                    .stream()
                            .forEach(entry -> jedis.set(entry.getKey(), entry.getValue()));
            System.out.println("Consolidating Redis data");
        }
    }

    /**
     * Retrieves the date value for a specific key from the Redis server
     *
     * @param keyName The key for which to retrieve the date value
     * @return A map containing the key and the date value, or an empty map if the key was not found
     */
    public Map<String, LocalDate> retrieve(String keyName) {
        try (Jedis jedis = redis.getResource()) {
            return jedis.get(keyName) == null ? new HashMap<>() : Map.of(keyName, LocalDate.parse(jedis.get(keyName)));
        }
    }

    public void close() {
        redis.close();
        System.out.println("Closing Redis connection");
    }

    /**
     * Returns a Jedis instance for interacting with the Redis server
     *
     * @return A Jedis instance, or null if an error occurred
     */
    public Jedis getJedis() {
        try {
            return redis.getResource();
        }catch (Exception e){
            System.out.println("Error while getting jedis resource");
            return null;
        }
    }
}
