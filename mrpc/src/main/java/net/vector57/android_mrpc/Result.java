package net.vector57.android_mrpc;

import android.os.Handler;

import com.google.gson.JsonElement;

/**
 * Created by Vector on 11/12/2016.
 */

public class Result implements Runnable{
    public static abstract class Callback {
        public void onResult(Message.Response response) {};
    }
    public static abstract class JSONCallback extends Callback {
        public void onResult(Message.Response response) {
            if(response.error == null)
                this.onSuccess(response.result);
            else
                this.onFailure(response.error);
        };
        public void onSuccess(JsonElement value) {};
        public void onFailure(JsonElement value) {};
    }
    private Message.Response message;
    public Callback callback;
    public Result(Callback callback) {
        this.callback = callback;
    }
    public void resolve(Handler handler, Message.Response message) {
        this.message = message;
        handler.post(this);
    }

    @Override
    public void run() {
        callback.onResult(message);
    }
}
