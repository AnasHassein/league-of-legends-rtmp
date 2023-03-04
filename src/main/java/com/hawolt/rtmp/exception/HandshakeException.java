package com.hawolt.rtmp.exception;

import java.io.IOException;

/**
 * thrown when the RTMP handshake does not behave as expected
 *
 * @see com.hawolt.rtmp.handshake.Handshake
 */
public class HandshakeException extends IOException {
    public HandshakeException(String message) {
        super(message);
    }
}
