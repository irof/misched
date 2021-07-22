package me.irof.misched.calendar;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/calendar")
public class CalendarController {

    EventQueryService eventQueryService;

    CalendarController(EventQueryService eventQueryService) {
        this.eventQueryService = eventQueryService;
    }

    @Get(uri = "{name}", produces = "text/calendar")
    public String index(String name) {
        Events events = eventQueryService.events(name);
        return events.iCalendarText();
    }
}
