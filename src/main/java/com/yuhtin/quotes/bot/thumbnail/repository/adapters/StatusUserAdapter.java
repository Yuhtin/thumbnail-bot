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
        String[] rewardsIds = ((String) resultSet.get("receivedRewardsIds")).split(",");
        List<String> receivedRewardsIds = new ArrayList<>(Arrays.asList(rewardsIds));

        return new StatusUser(
                resultSet.get("userId"),
                receivedRewardsIds,
                resultSet.get("isStatusSet"),
                resultSet.get("statusSetInMillis")
        );
    }

}
