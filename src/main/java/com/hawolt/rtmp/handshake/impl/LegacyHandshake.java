package com.hawolt.rtmp.handshake.impl;

import com.hawolt.rtmp.SocketConnection;
import com.hawolt.rtmp.handshake.Handshake;

import java.io.IOException;
import java.util.Random;

/**
 * The first handshake implementation I came across
 *
 * @author Hawolt
 */

public class LegacyHandshake implements Handshake {
    @Override
    public void perform(SocketConnection connection) throws IOException {
        // C0
        byte versionC0 = 0x03;
        connection.write(versionC0);

        // C1
        byte[] bytesC1 = new byte[1536];
        Random random = new Random();
        random.nextBytes(bytesC1);
        for (int i = 0; i < 8; i++) bytesC1[i] = 0;
        connection.write(bytesC1);

        // S0
        byte serverVersionS0 = connection.read();
        if (serverVersionS0 != 0x03) throw new IOException("Version mismatch");

        // S1
        byte[] buffer = new byte[1536];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = connection.read();
        }

        long zeroTime = buffer[0] << 24 | buffer[1] << 16 | buffer[2] << 8 | buffer[3];
        long offset = System.currentTimeMillis() - zeroTime;
        int delta = (int) ((System.currentTimeMillis() - offset) & 0xFFFFFFFFL);
        buffer[4] = (byte) (delta >> 24);
        buffer[5] = (byte) (delta >> 16);
        buffer[6] = (byte) (delta >> 8);
        buffer[7] = (byte) (delta);
        connection.write(buffer);

        // S2
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = connection.read();
        }

        for (int i = 8; i < buffer.length; i++) {
            if (buffer[i] != bytesC1[i]) {
                throw new IOException("Handshake payload mismatch");
            }
        }
    }
}
