package perfunctory.secretary;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Singleton
public class ConnpassClient {

    private static final Logger logger = LoggerFactory.getLogger(ConnpassClient.class);

    @Inject
    @Client("https://connpass.com/api/v1/")
    HttpClient connpassClient;

    DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("uuuuMM");

    String events(String name) {
        logger.info("get event from connpass");

        YearMonth now = YearMonth.now();
        String thisMonth = now.format(yearMonthFormatter);
        String nextMonth = now.plusMonths(1).format(yearMonthFormatter);

        HttpRequest<?> request = HttpRequest.GET("event/?owner_nickname=" + name + "&count=100&ym=" + thisMonth + "," + nextMonth)
                .header("User-Agent", "Micronaut/1.2.0");
        return connpassClient.toBlocking()
                .retrieve(request);
    }
}
