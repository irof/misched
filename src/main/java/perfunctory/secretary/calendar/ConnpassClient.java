package perfunctory.secretary.calendar;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Singleton
public class ConnpassClient {

    private static final Logger logger = LoggerFactory.getLogger(ConnpassClient.class);

    HttpClient connpassClient;
    CalendarRepository calendarRepository;
    DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("uuuuMM");

    ConnpassClient(@Client("https://connpass.com/api/v1/") HttpClient connpassClient, CalendarRepository calendarRepository) {
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

        HttpRequest<?> request = HttpRequest.GET("event/?owner_nickname=" + name + "&count=100&ym=" + yearMonths)
                .header("User-Agent", "Micronaut/1.2.0");
        return connpassClient.toBlocking()
                .retrieve(request);
    }
}
