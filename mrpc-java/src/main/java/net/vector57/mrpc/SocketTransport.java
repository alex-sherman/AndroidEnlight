package net.vector57.mrpc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.List;

/**
 * Created by Vector on 11/12/2016.
 */

public class SocketTransport extends Thread {

    class MessageData {
        final Message message;
        final InetAddress source;
        public MessageData(Message message, InetAddress source) {
            this.message = message;
            this.source = source;
        }
    }

    int localPort;
    int remote_port;
    DatagramSocket socket;
    byte[] buffer = new byte[4096];
    SocketAddress broadcast;
    final MRPC mrpc;
    private volatile boolean running = true;

    SocketTransport(MRPC mrpc, InetAddress broadcastAddress, int local_port) throws SocketException {
        this.mrpc = mrpc;
        this.localPort = local_port;
        this.remote_port = local_port;
        socket = new DatagramSocket(local_port);
        broadcast = new InetSocketAddress(broadcastAddress, remote_port);
    }

    @Override
    public void run() {
        while(running) {
            MessageData recvd = poll();
            if(recvd != null)
                mrpc.onReceive(recvd.message, recvd.source);
        }
    }


    MessageData poll() {
        DatagramPacket pkt = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(pkt);
            String recvd = new String(buffer, 0, pkt.getLength(), "ASCII");
            Message message = Message.FromJson(recvd);
            if(message != null)
                return new MessageData(message, pkt.getAddress());
        } catch (IOException e) { }

        return null;
    }

    Boolean send(Message message, List<InetAddress> addresses) {
        String messageString = message.toJSON();
        if(broadcast == null)
            return false;
        byte[] messageBytes;
        try {
            messageBytes = messageString.getBytes("ASCII");
            DatagramPacket pkt = new DatagramPacket(messageBytes, messageBytes.length, broadcast);
            socket.send(pkt);
            if(addresses != null) {
                for (InetAddress address : addresses) {
                    pkt.setAddress(address);
                    socket.send(pkt);
                }
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    void close() throws InterruptedException {
        socket.close();
        this.running = false;
        this.join();
    }
}
