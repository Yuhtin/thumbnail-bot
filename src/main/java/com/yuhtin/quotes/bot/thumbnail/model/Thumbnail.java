package com.yuhtin.quotes.bot.thumbnail.model;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
@Data
@RequiredArgsConstructor
public class Thumbnail {

    private final String id;
    private final String name;
    private final File file;

    private int views;
    private int votes;

    public double retention() {
        return (double) votes / views;
    }

}
