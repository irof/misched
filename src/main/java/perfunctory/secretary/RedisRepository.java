package perfunctory.secretary;

import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class RedisRepository {

    StatefulRedisConnection<String, String> connection;

    public RedisRepository(StatefulRedisConnection<String, String> connection) {
        this.connection = connection;
    }

    public void record(String key, String value) {
        RedisCommands<String, String> commands = connection.sync();
        commands.set(key, value, SetArgs.Builder.ex(300));
    }

    Optional<String> findEvent(String key) {
        RedisCommands<String, String> commands = connection.sync();
        String value = commands.get(key);
        return Optional.ofNullable(value);
    }
}
