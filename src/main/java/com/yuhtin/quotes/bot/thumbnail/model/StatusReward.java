package com.yuhtin.quotes.bot.thumbnail.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class StatusReward {

    public String id;
    public int hoursWithStatus;
    public String rewardDesc;

    public String getFormattedHours() {
        if (hoursWithStatus % 24 == 0) return hoursWithStatus / 24 + "d";
        return hoursWithStatus + "h";
    }

}
