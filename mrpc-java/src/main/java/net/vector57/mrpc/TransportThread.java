package net.vector57.mrpc;

public abstract class TransportThread extends Thread {
    protected MRPC mrpc;
    private volatile boolean running = true;

    public TransportThread(MRPC mrpc) {
        this.mrpc = mrpc;
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
    public void send(Message message) {
        send(message.toJSON());
    }
    public void close() throws InterruptedException { this.running = false; this.join(); }
}
