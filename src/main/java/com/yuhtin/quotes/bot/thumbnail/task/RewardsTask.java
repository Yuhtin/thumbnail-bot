package com.yuhtin.quotes.bot.thumbnail.task;

import com.yuhtin.quotes.bot.thumbnail.ThumbnailBot;
import com.yuhtin.quotes.bot.thumbnail.model.StatusReward;
import com.yuhtin.quotes.bot.thumbnail.model.StatusUser;
import com.yuhtin.quotes.bot.thumbnail.repository.UserRepository;
import lombok.AllArgsConstructor;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
@AllArgsConstructor
public class RewardsTask extends TimerTask {

    private static final int TASK_INTERVAL_IN_SECONDS = 5;
    private final ThumbnailBot bot;

    @Override
    public void run() {
        for (StatusUser statusUser : UserRepository.instance().selectAll("WHERE statusSet = true")) {
            statusUser.setStatusSetInMillis(statusUser.getStatusSetInMillis() + TimeUnit.SECONDS.toMillis(TASK_INTERVAL_IN_SECONDS));
            testRewards(statusUser);

            UserRepository.instance().insert(statusUser);
        }
    }

    private void testRewards(StatusUser statusUser) {
        for (StatusReward reward : bot.getRewardsManager().getStatusRewardMap().values()) {
            if (reward.getMinutesWithStatus() > TimeUnit.MILLISECONDS.toMinutes(statusUser.getStatusSetInMillis())) continue;
            if (statusUser.getReceivedRewardsIds().contains(reward.getId())) continue;

            statusUser.getReceivedRewardsIds().add(reward.getId());
            bot.getRewardsManager().sendRewardMessage(statusUser.getUserId(), reward);
        }
    }

    public int getIntervalIn(TimeUnit timeUnit) {
        return (int) timeUnit.convert(TASK_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
    }
}
