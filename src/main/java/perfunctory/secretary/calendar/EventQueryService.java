package perfunctory.secretary.calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Singleton
public class EventQueryService {

    private static final Logger logger = LoggerFactory.getLogger(EventQueryService.class);

    ConnpassClient connpassClient;
    CalendarRepository calendarRepository;
    DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("uuuuMM");

    EventQueryService(ConnpassClient connpassClient, CalendarRepository calendarRepository) {
        this.connpassClient = connpassClient;
        this.calendarRepository = calendarRepository;
    }

    String events(String name) {
        return calendarRepository.findEvent(name)
                .orElseGet(() -> {
                    String events = searchConnpass(name);
                    calendarRepository.record(name, events);
                    return events;
                });
    }

    String searchConnpass(String name) {
        logger.info("get event from connpass");

        YearMonth now = YearMonth.now();
        String yearMonths = IntStream.rangeClosed(0, 2)
                .mapToObj(i -> now.plusMonths(i))
                .map(yearMonthFormatter::format)
                .collect(Collectors.joining(","));

        return connpassClient.events("Micronaut/1.2.0", name, 1000, yearMonths);
    }
}
