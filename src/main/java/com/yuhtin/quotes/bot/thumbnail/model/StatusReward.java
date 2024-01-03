package com.yuhtin.quotes.bot.thumbnail.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class StatusReward {

  public String id;
  public int minutesWithStatus;
  public String rewardDesc;

}
