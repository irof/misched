package perfunctory.secretary;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.StringJoiner;

@Controller("/calendar")
public class CalendarController {

    ConnpassClient connpassClient;
    RedisRepository redisRepository;

    CalendarController(ConnpassClient connpassClient, RedisRepository redisRepository) {
        this.connpassClient = connpassClient;
        this.redisRepository = redisRepository;
    }

    @Get(uri = "{name}", produces = "text/calendar")
    public String index(String name) throws IOException {
        StringJoiner calendar =
                new StringJoiner("\n", "BEGIN:VCALENDAR\n", "\nEND:VCALENDAR")
                        .add("VERSION:2.0")
                        .add("PRODID:-//irof//perfunctory-secretary//EN")
                        .add("METHOD:PUBLISH")
                        .add("X-WR-CALNAME:ふわっと予定")
                        .add("X-WR-TIMEZONE:UTC");

        String eventsString =
                redisRepository.findEvent(name)
                        .orElseGet(() -> {
                            String events = connpassClient.events(name);
                            redisRepository.record(name, events);
                            return events;
                        });

        ObjectMapper objectMapper = new ObjectMapper()
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        JsonNode response = objectMapper.readTree(eventsString);

        JsonNode events = response.findValue("events");
        Iterator<JsonNode> elements = events.elements();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmssX");

        while (elements.hasNext()) {
            JsonNode event = elements.next();

            calendar.add(
                    new StringJoiner("\n", "BEGIN:VEVENT\n", "\nEND:VEVENT")
                            .add("DTSTAMP:" + format(event, "updated_at", dateTimeFormatter))
                            .add("UID:" + "perfunctory-secretary@" + event.get("event_id").numberValue())
                            .add("DTSTART:" + format(event, "started_at", dateTimeFormatter))
                            .add("DTEND:" + format(event, "ended_at", dateTimeFormatter))
                            .add("SUMMARY:" + event.get("title").textValue())
                            // 長いんで一旦外しておく
                            //.add("DESCRIPTION:" + event.get("description").textValue().replaceAll("\n", "\\n"))
                            .add("LOCATION:" + event.get("address").textValue() + " (" + event.get("place").textValue() + ")")
                            .add("URL:" + event.findPath("event_url").textValue())
                            .add("GEO:" + event.findPath("lat").textValue() + ";" + event.findPath("lon").textValue())
                            .toString()
            );
        }

        return calendar.toString();
    }

    String format(JsonNode event, String path, DateTimeFormatter dateTimeFormatter) {
        String dateTimeText = event.get(path).textValue();
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTimeText);
        return offsetDateTime.atZoneSameInstant(ZoneOffset.UTC).format(dateTimeFormatter);
    }
}
