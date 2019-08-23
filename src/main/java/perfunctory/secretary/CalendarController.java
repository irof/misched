package perfunctory.secretary;

import com.fasterxml.jackson.databind.JsonNode;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.StringJoiner;

@Controller("/calendar")
public class CalendarController {

    @Inject
    @Client("https://connpass.com/api/v1/")
    HttpClient connpassClient;

    @Get(uri = "{name}", produces = "text/calendar")
    public String index(String name) {
        StringJoiner calendar =
                new StringJoiner("\n", "BEGIN:VCALENDAR\n", "\nEND:VCALENDAR")
                        .add("VERSION:2.0")
                        .add("PRODID:-//irof//perfunctory-secretary//EN")
                        .add("METHOD:PUBLISH")
                        .add("X-WR-CALNAME:ふわっと予定")
                        .add("X-WR-TIMEZONE:UTC");

        DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("uuuuMM");
        String thisMonth = YearMonth.now().format(yearMonthFormatter);
        String nextMonth = YearMonth.now().plusMonths(1).format(yearMonthFormatter);

        HttpRequest<?> request = HttpRequest.GET("event/?owner_nickname=" + name + "&count=100&ym=" + thisMonth + "," + nextMonth)
                .header("User-Agent", "Micronaut/1.2.0");

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmssX");

        JsonNode response = connpassClient.toBlocking()
                .retrieve(request, JsonNode.class);

        JsonNode events = response.findValue("events");
        Iterator<JsonNode> elements = events.elements();

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
                            .add("LOCATION:" + event.get("place").textValue())
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
