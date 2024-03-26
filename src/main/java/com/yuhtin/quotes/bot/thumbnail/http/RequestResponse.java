package com.yuhtin.quotes.bot.thumbnail.http;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestResponse {

    private final int statusCode;
    private final String body;

}
