package net.vector57.mrpc;

import android.os.AsyncTask;

public abstract class TransportThread extends Thread {
    protected MRPC mrpc;
    private volatile boolean running = true;
    AsyncTask<String, Boolean, Void> sendTask() {
        return new AsyncTask<String, Boolean, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                send(params[0]);
                return null;
            }
        };
    }
    public TransportThread(MRPC mrpc) {
        this.mrpc = mrpc;
        setDaemon(true);
    }

    @Override
    public void run() {
        while(running) {
            String recvd = poll();
            Message message = Message.FromJson(recvd);
            if(message != null) {
                onRecv(message);
            }
        }
    }
    protected abstract String poll();
    protected abstract Boolean send(String message);
    protected void onRecv(Message message) { mrpc.onReceive(message); }
    public void sendAsync(Message message) {
        sendTask().execute(message.toJSON());
    }
    public void close() { this.running = false; }
}
