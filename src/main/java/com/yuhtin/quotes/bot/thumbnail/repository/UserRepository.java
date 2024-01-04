package com.yuhtin.quotes.bot.thumbnail.repository;

import com.henryfabio.sqlprovider.executor.SQLExecutor;
import com.yuhtin.quotes.bot.thumbnail.model.StatusUser;
import com.yuhtin.quotes.bot.thumbnail.repository.adapters.StatusUserAdapter;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
@NoArgsConstructor
public final class UserRepository {


    private static final UserRepository INSTANCE = new UserRepository();
    private static final String TABLE = "status_users";

    private SQLExecutor sqlExecutor;

    public void init(SQLExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
        createTable();
    }

    public void createTable() {
        sqlExecutor.updateQuery("CREATE TABLE IF NOT EXISTS " + TABLE + "(" +
                "userId LONG NOT NULL PRIMARY KEY," +
                "receivedRewardsIds LONGTEXT NOT NULL," +
                "isStatusSet BOOLEAN NOT NULL DEFAULT FALSE," +
                "statusSetInMillis LONGTEXT" +
                ");"
        );
    }

    private StatusUser selectOneQuery(String query) {
        return sqlExecutor.resultOneQuery(
                "SELECT * FROM " + TABLE + " " + query,
                statement -> {
                },
                StatusUserAdapter.class
        );
    }

    public StatusUser findByDiscordId(long discordId) {
        StatusUser statusUser = selectOneQuery("WHERE userId = " + discordId);
        if (statusUser == null) {
            statusUser = new StatusUser(discordId);
            insert(statusUser);
        }

        return statusUser;
    }

    public Set<StatusUser> selectAll(String query) {
        return sqlExecutor.resultManyQuery(
                "SELECT * FROM " + TABLE + " " + query,
                k -> {
                },
                StatusUserAdapter.class
        );
    }

    public void insert(StatusUser data) {
        this.sqlExecutor.updateQuery(
                String.format("REPLACE INTO %s VALUES(?, ?, ?, ?)", TABLE),
                statement -> {
                    statement.set(1, data.getUserId());
                    statement.set(2, String.join(",", data.getReceivedRewardsIds()));
                    statement.set(3, data.isStatusSet());
                    statement.set(4, String.valueOf(data.getStatusSetInMillis()));
                }
        );
    }

    public static UserRepository instance() {
        return INSTANCE;
    }

}
