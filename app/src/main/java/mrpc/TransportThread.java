package mrpc;

import android.os.AsyncTask;

public abstract class TransportThread extends Thread {
    AsyncTask<String, Boolean, Void> sendTask() {
        return new AsyncTask<String, Boolean, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                send(params[0]);
                return null;
            }
        };
    }
    public TransportThread() {
        setDaemon(true);
    }

    @Override
    public void run() {
        while(true) {
            String recvd = poll();
            Message message = Message.FromJson(recvd);
            if(message != null) {
                onRecv(message);
            }
        }
    }
    protected abstract String poll();
    protected abstract Boolean send(String message);
    protected void onRecv(Message message) { }
    public void sendAsync(Message message) {
        sendTask().execute(message.toJSON());
    }
}
