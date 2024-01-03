package com.yuhtin.quotes.bot.thumbnail.task;

import com.yuhtin.quotes.bot.thumbnail.ThumbnailBot;
import com.yuhtin.quotes.bot.thumbnail.model.StatusUser;
import com.yuhtin.quotes.bot.thumbnail.repository.UserRepository;
import lombok.AllArgsConstructor;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
@AllArgsConstructor
public class RewardsTask implements Runnable {

    private final ThumbnailBot bot;

    @Override
    public void run() {

        for (StatusUser statusUser : UserRepository.instance().selectAll("")) {
            statusUser.getStatusSetTimestamp() + (status)
        }

    }
}
