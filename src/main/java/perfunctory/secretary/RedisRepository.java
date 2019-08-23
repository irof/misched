package perfunctory.secretary;

import io.micronaut.context.annotation.Value;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class RedisRepository {

    Jedis jedis;

    public RedisRepository(@Value("${redis.url}") String redisUrl) {
        jedis = new Jedis(redisUrl);
    }

    public void record(String key, String value) {
        jedis.set(key, value, SetParams.setParams().ex(300));
    }

    Optional<String> findEvent(String key) {
        String value = jedis.get(key);
        return Optional.ofNullable(value);
    }
}
