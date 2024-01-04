package com.yuhtin.quotes.bot.thumbnail.repository;

import com.henryfabio.sqlprovider.executor.SQLExecutor;
import com.yuhtin.quotes.bot.thumbnail.ThumbnailBot;
import com.yuhtin.quotes.bot.thumbnail.model.Thumbnail;
import com.yuhtin.quotes.bot.thumbnail.repository.adapters.ThumbnailAdapter;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.io.File;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
@NoArgsConstructor
public final class ThumbnailRepository {


    private static final ThumbnailRepository INSTANCE = new ThumbnailRepository();
    private static final String TABLE = "thumbnail_statistics";

    private SQLExecutor sqlExecutor;

    public static ThumbnailRepository instance() {
        return INSTANCE;
    }

    public void init(SQLExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
        createTable();
    }

    public void createTable() {
        sqlExecutor.updateQuery("CREATE TABLE IF NOT EXISTS " + TABLE + "(" +
                "id VARCHAR(16) NOT NULL PRIMARY KEY," +
                "name VARCHAR(32) NOT NULL," +
                "file LONGTEXT NOT NULL," +
                "views INTEGER NOT NULL DEFAULT 0," +
                "votes INTEGER NOT NULL DEFAULT 0" +
                ");"
        );
    }

    private Thumbnail find(String query) {
        return sqlExecutor.resultOneQuery(
                "SELECT * FROM " + TABLE + " " + query,
                statement -> {
                },
                ThumbnailAdapter.class
        );
    }

    public Thumbnail findById(String id) {
        return find("WHERE id = '" + id + "'");
    }

    public Pair<Thumbnail, Thumbnail> selectRandom() {
        Thumbnail first = find("ORDER BY RANDOM() LIMIT 1");
        Thumbnail second = find("WHERE id != '" + first.getId() + "' ORDER BY RANDOM() LIMIT 1");

        return Pair.of(first, second);
    }

    public void insert(Thumbnail data) {
        this.sqlExecutor.updateQuery(
                String.format("REPLACE INTO %s VALUES(?, ?, ?, ?, ?)", TABLE),
                statement -> {
                    statement.set(1, data.getId());
                    statement.set(2, data.getName());
                    statement.set(3, data.getFile().getAbsolutePath());
                    statement.set(4, data.getViews());
                    statement.set(5, data.getVotes());
                }
        );
    }

    public void loadThumbnail(File file) {
        String name = file.getName().replace(".png", "").replace(".jpg", "");
        ThumbnailBot.getInstance().getLogger().info("Loaded thumbnail " + name);

        if (findById(name) != null) return;

        Thumbnail thumbnail = new Thumbnail(name, name, file);
        insert(thumbnail);

        ThumbnailBot.getInstance().getLogger().info("Thumbnail " + name + " registered in database!");
    }
}
