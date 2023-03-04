package com.hawolt.rtmp.handshake.impl;

import com.hawolt.rtmp.SocketConnection;
import com.hawolt.rtmp.exception.HandshakeException;
import com.hawolt.rtmp.handshake.Handshake;

import java.io.IOException;
import java.util.Random;

/**
 * The current Handshake being used by the client
 *
 * @author Hawolt
 */

public class UpdatedHandshake implements Handshake {
    private final byte RTMP_VERSION = 0x03;
    private final int PAYLOAD_SIZE = 1536;

    @Override
    public void perform(SocketConnection connection) throws IOException {
        Random random = new Random();

        connection.write(RTMP_VERSION);
        byte[] c1Payload = new byte[PAYLOAD_SIZE];
        random.nextBytes(c1Payload);

        int serverVersion;
        for (serverVersion = 0; serverVersion < 8; ++serverVersion) {
            c1Payload[serverVersion] = 0;
        }

        connection.write(c1Payload);
        serverVersion = connection.read();
        if (serverVersion != 3) {
            throw new HandshakeException("Encountered invalid RTMP Version during Handshake");
        } else {
            byte[] buffer = new byte[PAYLOAD_SIZE];
            int read = 0;
            do {
                read += connection.read(buffer, read, buffer.length - read);
            } while (read != buffer.length);

            long zeroTime = buffer[0] << 24 | buffer[1] << 16 | buffer[2] << 8 | buffer[3];
            int c2TimeStamp = getTimeDelta(System.currentTimeMillis() - zeroTime);
            buffer[4] = (byte) (c2TimeStamp >> 24);
            buffer[5] = (byte) (c2TimeStamp >> 16);
            buffer[6] = (byte) (c2TimeStamp >> 8);
            buffer[7] = (byte) c2TimeStamp;
            connection.write(buffer);
            read = 0;

            do {
                read += connection.read(buffer, read, buffer.length - read);
            } while (read != buffer.length);

            for (int i = 8; i < buffer.length; ++i) {
                if (buffer[i] != c1Payload[i]) {
                    throw new HandshakeException("Handshake payload mismatch at " + i);
                }
            }
        }
    }

    public int getTimeDelta(long timeOffset) {
        return (int) (System.currentTimeMillis() - timeOffset & 4294967295L);
    }
}
