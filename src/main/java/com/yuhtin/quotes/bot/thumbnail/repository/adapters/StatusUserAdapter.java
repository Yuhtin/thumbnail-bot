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

        boolean isStatusSet = false;
        try {
            int statusSet = resultSet.get("isStatusSet");
            isStatusSet = statusSet == 1;
        } catch (Exception ignored) {
            String isStatusSet1 = resultSet.get("isStatusSet");
            isStatusSet = Boolean.parseBoolean(isStatusSet1);
        }

        int disabledByGoingOffline = resultSet.get("disabledByGoingOffline");
        return new StatusUser(
                resultSet.get("userId"),
                receivedRewardsIds,
                isStatusSet,
                Long.parseLong(statusSetInMillis),
                disabledByGoingOffline == 1
        );
    }

}
