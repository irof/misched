package perfunctory.secretary.calendar;

import java.util.List;
import java.util.stream.Collectors;

public class ConnpassResponse {

    int results_returned;
    int results_available;
    List<ConnpassEvent> events;

    public Events toEvents() {
        List<Events.Event> events = this.events.stream()
                .map(ConnpassEvent::toEvent)
                .collect(Collectors.toList());
        return new Events(events);
    }
}
