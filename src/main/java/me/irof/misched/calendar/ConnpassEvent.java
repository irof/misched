package me.irof.misched.calendar;

import java.time.OffsetDateTime;

public class ConnpassEvent {
    OffsetDateTime updated_at;
    OffsetDateTime started_at;
    OffsetDateTime ended_at;

    String event_id;
    String title;
    String description;
    String address;
    String place;
    String event_url;

    Events.Event toEvent() {
        return new Events.Event(
                "misched@connpass:" + event_id,
                updated_at,
                started_at,
                ended_at,
                title,
                location(),
                event_url
        );
    }

    private String location() {
        if (address != null) {
            if (place != null) {
                return address + " (" + place + ")";
            }
            return address;
        }
        if (place != null) {
            return place;
        }
        return "未設定";
    }
}
