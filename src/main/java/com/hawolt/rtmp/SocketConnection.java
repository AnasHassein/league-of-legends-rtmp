package com.hawolt.rtmp;

import java.io.IOException;
import java.net.Socket;


/**
 * Socket connection for rtmp
 *
 * @author Hawolt
 */
public interface SocketConnection {
    boolean isConnected();

    void onConnectionFailure(IOException e);

    void onConnect(String DSId);

    void onDisconnect();

    void write(byte... b) throws IOException;

    byte read() throws IOException;

    int read(byte[] b) throws IOException;

    int read(byte[] b, int offset, int length) throws IOException;

    Socket getSocket();
}
