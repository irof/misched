package me.irof.misched.calendar;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;

@Client(value = "https://connpass.com/api/v1/")
public interface ConnpassClient {

    // nicknameとowner_nicknameがOR条件っぽいのでこれでよさげ
    @Get("event/?nickname={name}&owner_nickname={name}&count={count}&ym={yearMonths}")
    String events(@Header("User-Agent") String userAgent,
                  @QueryValue("name") String name,
                  @QueryValue("count") int count,
                  @QueryValue("yearMonths") String yearMonths);
}

