package me.irof.misched;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller
public class RootController {

    @Get
    String index() {
        return "welcome";
    }
}
