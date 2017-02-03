package net.vector57.mrpc;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by Vector on 11/12/2016.
 */

public class MessageTypeAdapter implements JsonDeserializer<Message> {
    private static boolean isMessage(JsonObject j) {
        return j.has("id") && j.has("src") && j.has("dst");
    }
    private static boolean isResponse(JsonObject j) {
        return j.has("result") || j.has("error");
    }

    @Override
    public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(!(json instanceof JsonObject) || !isMessage((JsonObject)json)) return null;
        if(isResponse((JsonObject)json))
            return context.deserialize(json, Message.Response.class);
        return context.deserialize(json, Message.Response.class);
    }
}
