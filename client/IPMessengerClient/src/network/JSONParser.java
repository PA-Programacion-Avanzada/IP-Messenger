package network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JSONParser {
    private static final Gson GSON = new GsonBuilder().create();

    private JSONParser() {
    }

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return GSON.fromJson(json, type);
    }
}
