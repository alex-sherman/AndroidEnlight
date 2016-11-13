package mrpc;

import com.google.gson.JsonElement;

/**
 * Created by Vector on 11/12/2016.
 */

public class Result {
    public static abstract class Callback {
        public void onSuccess(JsonElement value) {};
        public void onFailure(JsonElement value) {};
    }
    public boolean success;
    public JsonElement value;
    public Callback callback;
    public Result(Callback callback) {
        this.callback = callback;
    }
    public void resolve(JsonElement value, boolean success) {
        if(success)
            this.callback.onSuccess(value);
        else
            this.callback.onFailure(value);
    }
}
