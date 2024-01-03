package com.yuhtin.quotes.bot.thumbnail.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class StatusUser {

    private final long userId;
    private List<String> receivedRewardsIds = new ArrayList<>();

    private boolean statusSet;
    private long statusSetInMillis;

}
