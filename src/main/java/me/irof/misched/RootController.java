package me.irof.misched;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.session.annotation.SessionValue;
import io.micronaut.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

@Controller
public class RootController {

    static Logger logger = LoggerFactory.getLogger(RootController.class);

    @View("index")
    @Get
    Map<?, ?> index(@SessionValue("twitterScreenName") Optional<String> loginName) {
        logger.info("login");
        return Map.of("loginName", loginName);
    }
}
