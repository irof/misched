package perfunctory.secretary.calendar;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;

/**
 * @see <a href="https://tools.ietf.org/html/rfc5545">Internet Calendaring and Scheduling Core Object Specification</a>
 */
public class Events {

    List<Event> events;

    public Events(List<Event> events) {
        this.events = events;
    }

    public String iCalendarText() {
        StringJoiner calendar =
                new StringJoiner("\n", "BEGIN:VCALENDAR\n", "\nEND:VCALENDAR")
                        .add("VERSION:2.0")
                        .add("PRODID:-//irof//perfunctory-secretary//EN")
                        .add("METHOD:PUBLISH")
                        .add("X-WR-CALNAME:ふわっと予定")
                        .add("X-WR-TIMEZONE:UTC");
        for (Event event : events) {
            calendar.add(event.iCalendarText());
        }
        return calendar.toString();
    }

    static class Event {

        static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmssX");

        String uid;
        OffsetDateTime updateDateTime;
        OffsetDateTime startDateTime;
        OffsetDateTime endDateTime;
        String summary;
        String location;
        String url;

        public Event(String uid, OffsetDateTime updateDateTime, OffsetDateTime startDateTime, OffsetDateTime endDateTime, String summary, String location, String url) {
            this.uid = uid;
            this.updateDateTime = updateDateTime;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.summary = summary;
            this.location = location;
            this.url = url;
        }

        public String iCalendarText() {
            return new StringJoiner("\n", "BEGIN:VEVENT\n", "\nEND:VEVENT")
                    .add("DTSTAMP:" + format(updateDateTime))
                    .add("UID:" + uid)
                    .add("DTSTART:" + format(startDateTime))
                    .add("DTEND:" + format(endDateTime))
                    .add("SUMMARY:" + summary)
                    .add("LOCATION:" + location)
                    .add("URL:" + url)
                    .toString();
        }

        private String format(OffsetDateTime offsetDateTime) {
            return offsetDateTime.atZoneSameInstant(ZoneOffset.UTC).format(dateTimeFormatter);
        }
    }
}
