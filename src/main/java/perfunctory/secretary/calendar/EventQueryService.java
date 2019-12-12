package perfunctory.secretary.calendar;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
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
    ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JSR310Module())
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    EventQueryService(ConnpassClient connpassClient, CalendarRepository calendarRepository) {
        this.connpassClient = connpassClient;
        this.calendarRepository = calendarRepository;
    }

    Events events(String name) {
        try {
            ConnpassResponse connpassResponse = objectMapper.readValue(eventsString(name), ConnpassResponse.class);
            return connpassResponse.toEvents();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    String eventsString(String name) {
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
                .mapToObj(now::plusMonths)
                .map(yearMonthFormatter::format)
                .collect(Collectors.joining(","));

        return connpassClient.events("Micronaut/1.2.0", name, 1000, yearMonths);
    }
}
