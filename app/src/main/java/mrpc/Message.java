package mrpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * Created by Vector on 11/12/2016.
 */

public abstract class Message {
    private static Gson _gson;
    public int id;
    public String src;
    public String dst;

    public static Gson gson() {
        if(_gson == null) {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Message.class, new MessageTypeAdapter());
            _gson = builder.create();
        }
        return _gson;
    }

    public static class Request extends Message {
        public JsonElement value;
        public Request(Object value) {
            this.value = gson().toJsonTree(value);
        }
    }
    public static class Response extends Message {
        public String result;
        public String error;
    }
    public static Message FromJson(String string) {
        return gson().fromJson(string, Message.class);
    }

    public String toJSON() {
        return gson().toJson(this);
    }

    public Boolean isValid() {
        return id != 0 && src != null && dst != null;
    }
}
