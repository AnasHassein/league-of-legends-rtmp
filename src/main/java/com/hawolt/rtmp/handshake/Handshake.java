package com.hawolt.rtmp.handshake;

import com.hawolt.rtmp.SocketConnection;

import java.io.IOException;


/**
 * Handshake protocol for rtmp
 *
 * @author Hawolt
 */

public interface Handshake {
    void perform(SocketConnection connection) throws IOException;
}
