package com.yuhtin.quotes.bot.thumbnail.repository.adapters;

import com.henryfabio.sqlprovider.executor.adapter.SQLResultAdapter;
import com.henryfabio.sqlprovider.executor.result.SimpleResultSet;
import com.yuhtin.quotes.bot.thumbnail.model.StatusUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
public class StatusUserAdapter implements SQLResultAdapter<StatusUser> {

    @Override
    public StatusUser adaptResult(SimpleResultSet resultSet) {
        List<String> receivedRewardsIds = new ArrayList<>();
        String string = resultSet.get("receivedRewardsIds");

        if (string != null && !string.isEmpty()) {
            String[] rewardsIds = string.split(",");
            receivedRewardsIds.addAll(Arrays.asList(rewardsIds));
        }

        String statusSetInMillis = resultSet.get("statusSetInMillis");

        return new StatusUser(
                resultSet.get("userId"),
                receivedRewardsIds,
                (int) resultSet.get("isStatusSet") == 1,
                Long.parseLong(statusSetInMillis)
        );
    }

}
