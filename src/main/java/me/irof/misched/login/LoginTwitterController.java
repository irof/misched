package me.irof.misched.login;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.util.Objects;

@Controller("login/twitter")
public class LoginTwitterController {

    static Logger logger = LoggerFactory.getLogger(LoginTwitterController.class);
    TwitterConfiguration configuration;

    LoginTwitterController(TwitterConfiguration configuration) {
        this.configuration = configuration;
    }

    @Get
    MutableHttpResponse<?> requestTokenAndRedirect(Session session) {
        try {
            Twitter twitter = configuration.newTwitterInstance();
            RequestToken requestToken = twitter.getOAuthRequestToken("https://misched.herokuapp.com/login/twitter/callback");
            logger.info("SUCCESS /oauth/request_token.");

            session.put("oauth_token", requestToken.getToken());
            session.put("oauth_token_secret", requestToken.getTokenSecret());

            String authenticationURL = requestToken.getAuthenticationURL();

            logger.info(authenticationURL);
            return HttpResponse.temporaryRedirect(
                    HttpResponse.uri(authenticationURL));
        } catch (TwitterException e) {
            throw new RuntimeException(e);
        }
    }

    @Get("callback")
    MutableHttpResponse<Object> callbackAndGetAccessToken(Session session,
                                                          @QueryValue("oauth_token") String oauthToken,
                                                          @QueryValue("oauth_verifier") String oauthVerifier) {
        session.get("oauth_token")
                .filter(value -> Objects.equals(value, oauthToken))
                .orElseThrow(() -> new IllegalArgumentException("request_token session is expired or mismatch requested oauth_token"));
        String oauthTokenSecret = (String) session.get("oauth_token_secret").orElseThrow();

        try {
            Twitter twitter = configuration.newTwitterInstance();
            AccessToken oAuthAccessToken = twitter.getOAuthAccessToken(new RequestToken(oauthToken, oauthTokenSecret), oauthVerifier);
            // api呼び出ししないからaccessTokenは別にいらない感じ
            logger.info("SUCCESS /oauth/access_token. userId={}, screenName={}", oAuthAccessToken.getUserId(), oAuthAccessToken.getScreenName());

            User user = twitter.verifyCredentials();
            logger.info("SUCCESS /account/verify_credentials. userId={}, name={}, screenName={}", user.getId(), user.getName(), user.getScreenName());

            // session破棄
            session.clear();
            // アカウントを入れておく
            session.put("account", "twitter:" + user.getId());

            return HttpResponse.temporaryRedirect(HttpResponse.uri("/"));
        } catch (TwitterException e) {
            throw new RuntimeException(e);
        }
    }
}
