package me.irof.misched.login;

import io.micronaut.context.annotation.ConfigurationProperties;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

@ConfigurationProperties("misched.twitter")
record TwitterConfiguration(String key, String secret) {

    public Twitter newTwitterInstance() {
        TwitterFactory twitterFactory = new TwitterFactory();
        Twitter instance = twitterFactory.getInstance();
        instance.setOAuthConsumer(key(), secret());
        return instance;
    }
}
