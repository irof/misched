package perfunctory.secretary.calendar;

import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
class CalendarRepository {

    StatefulRedisConnection<String, String> connection;

    CalendarRepository(StatefulRedisConnection<String, String> connection) {
        this.connection = connection;
    }

    void record(String key, String value) {
        RedisCommands<String, String> commands = connection.sync();
        commands.set(key, value, SetArgs.Builder.ex(300));
    }

    Optional<String> findEvent(String key) {
        RedisCommands<String, String> commands = connection.sync();
        String value = commands.get(key);
        return Optional.ofNullable(value);
    }
}
