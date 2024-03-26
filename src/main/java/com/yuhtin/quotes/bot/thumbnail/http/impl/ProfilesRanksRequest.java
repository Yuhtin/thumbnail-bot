package com.yuhtin.quotes.bot.thumbnail.http.impl;

import com.yuhtin.quotes.bot.thumbnail.http.Request;
import lombok.Getter;

@Getter
public class ProfilesRanksRequest implements Request {

    private final String body;

    public ProfilesRanksRequest() {
        this.body = "{}";
    }

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public String getUrl() {
        return "https://europe-west1-soba-analytics.cloudfunctions.net/creatorprofiles-ranks";
    }

}
