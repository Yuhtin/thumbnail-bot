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
                int rank = -1;

                while (jsonReader.hasNext()) {
                    try {
                        String name = jsonReader.nextName();

                        switch (name) {
                            case "display_name":
                                displayName = jsonReader.nextString();
                                break;
                            case "user_uuid":
                                userUuid = jsonReader.nextString();
                                break;
                            case "rank":
                                rank = jsonReader.nextInt();
                                break;
                            default:
                                jsonReader.skipValue();
                                break;
                        }

                    } catch (Exception e) {
                        jsonReader.skipValue();
                    }
                }

                jsonReader.endObject();

                if (displayName == null || userUuid == null) continue;

                ProfileRankData profileRankData = new ProfileRankData(displayName, userUuid, rank);
                profileRankDataList.add(profileRankData);
            }

            jsonReader.endArray();

            return profileRankDataList;
        }
    };

    private final String display_name;
    private final String user_uuid;
    private final int rank;

}
