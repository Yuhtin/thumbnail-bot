package com.yuhtin.quotes.bot.thumbnail.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HTTPRequest {

    private final String url;
    private final String webToken;
    private final String body;

    @Getter private String response;

    public static HTTPRequest to(String url, String webToken) {
        return new HTTPRequest(url, webToken, null);
    }

    public static HTTPRequest to(String url, String webToken, String body) {
        return new HTTPRequest(url, webToken, body);
    }

    public HTTPRequest send() {
        StringBuilder responseContent = new StringBuilder();

        try {
            URL url = new URL(this.url);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Authorization", "Bearer " + webToken);

            if (body != null) {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.getOutputStream().write(body.getBytes());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) responseContent.append(line);
            reader.close();
        } catch (IOException exception) {
            exception.printStackTrace();
            return this;
        }

        response = responseContent.toString();
        return this;
    }

}