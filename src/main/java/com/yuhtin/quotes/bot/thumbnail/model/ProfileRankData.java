package com.yuhtin.quotes.bot.thumbnail.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class ProfileRankData {

    public static final TypeAdapter<List<ProfileRankData>> ADAPTER = new TypeAdapter<List<ProfileRankData>>() {
        @Override
        public void write(JsonWriter jsonWriter, List<ProfileRankData> profileRankData) {
            throw new UnsupportedOperationException("This operation is not supported");
        }

        @Override
        public List<ProfileRankData> read(JsonReader jsonReader) throws IOException {
            List<ProfileRankData> profileRankDataList = new ArrayList<>();

            jsonReader.beginArray();

            while (jsonReader.hasNext()) {
                jsonReader.beginObject();

                String displayName = null;
                String userUuid = null;

                while (jsonReader.hasNext()) {
                    try {
                        String name = jsonReader.nextName();

                        if (name.equals("display_name")) {
                            displayName = jsonReader.nextString();
                        } else if (name.equals("user_uuid")) {
                            userUuid = jsonReader.nextString();
                        } else {
                            jsonReader.skipValue();
                        }
                    } catch (Exception e) {
                        jsonReader.skipValue();
                    }
                }

                jsonReader.endObject();

                if (displayName == null || userUuid == null) continue;

                ProfileRankData profileRankData = new ProfileRankData(displayName, userUuid);
                profileRankDataList.add(profileRankData);
            }

            jsonReader.endArray();

            return profileRankDataList;
        }
    };

    private final String display_name;
    private final String user_uuid;

}
