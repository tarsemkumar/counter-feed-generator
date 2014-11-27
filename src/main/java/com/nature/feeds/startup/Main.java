package com.nature.feeds.startup;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.nature.feeds.guice.FeedModule;

public class Main {

    static Injector injector = Guice.createInjector(new FeedModule());

    /**
     * The execution of this application will start from here.
     */
    public static void main(String[] args) {
        FeedGenerator feedGenerator = injector.getInstance(FeedGenerator.class);
        feedGenerator.fetchFeedData();
    }
}
