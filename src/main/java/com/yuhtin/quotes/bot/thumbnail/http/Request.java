package com.yuhtin.quotes.bot.thumbnail.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

public interface Request {

    String getBody();

    default String getUrl() {
        return null;
    }

    default String getMethod() {
        return "GET";
    }

    default CompletableFuture<RequestResponse> promise() {
        requireNonNull(getUrl());

        return CompletableFuture.supplyAsync(() -> {
            int statusCode;
            StringBuilder responseContent = new StringBuilder();

            try {
                URL url = new URI(getUrl()).toURL();

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(getMethod());
                connection.setConnectTimeout(7000);
                connection.setReadTimeout(7000);

                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                if (getBody() != null) {
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.getOutputStream().write(getBody().getBytes());
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                statusCode = connection.getResponseCode();

                String line;

                while ((line = reader.readLine()) != null) responseContent.append(line);
                reader.close();
            } catch (IOException | URISyntaxException exception) {
                if (exception.getMessage().contains("500 for URL")) {
                    return new RequestResponse(500, "Internal server error");
                } else {
                    exception.printStackTrace();
                    return new RequestResponse(404, exception.getMessage());
                }
            }

            return new RequestResponse(statusCode, responseContent.toString());
        });
    }

    default CompletableFuture<RequestResponse> task() {
        return promise();
    }

}
