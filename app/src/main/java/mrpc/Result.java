package mrpc;

import android.os.Handler;

import com.google.gson.JsonElement;

/**
 * Created by Vector on 11/12/2016.
 */

public class Result implements Runnable{
    public static abstract class Callback {
        public void onSuccess(JsonElement value) {};
        public void onFailure(JsonElement value) {};
    }
    private boolean success;
    private JsonElement value;
    public Callback callback;
    public Result(Callback callback) {
        this.callback = callback;
    }
    public void resolve(Handler handler, JsonElement value, boolean success) {
        this.success = success;
        this.value = value;
        handler.post(this);
    }

    @Override
    public void run() {
        if(success)
            this.callback.onSuccess(value);
        else
            this.callback.onFailure(value);
    }
}
