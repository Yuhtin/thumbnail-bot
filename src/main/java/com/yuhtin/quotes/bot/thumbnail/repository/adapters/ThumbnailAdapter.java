package com.yuhtin.quotes.bot.thumbnail.repository.adapters;

import com.henryfabio.sqlprovider.executor.adapter.SQLResultAdapter;
import com.henryfabio.sqlprovider.executor.result.SimpleResultSet;
import com.yuhtin.quotes.bot.thumbnail.model.Thumbnail;

import java.io.File;

public class ThumbnailAdapter implements SQLResultAdapter<Thumbnail> {

    @Override
    public Thumbnail adaptResult(SimpleResultSet resultSet) {
        File file = new File((String) resultSet.get("file"));

        Thumbnail thumbnail = new Thumbnail(
                resultSet.get("id"),
                resultSet.get("name"),
                file
        );

        thumbnail.setViews(resultSet.get("views"));
        thumbnail.setVotes(resultSet.get("votes"));

        return thumbnail;
    }

}
