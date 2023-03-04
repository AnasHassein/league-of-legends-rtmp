package com.hawolt.rtmp.amf.decoder;

/**
 * A reader for Binary data
 *
 * @author Hawolt
 */

public interface BinaryReader {
    byte read();

    byte[] read(int l);

    String readUTF8String(int l);

    boolean readBoolean();

    int readByteAsInteger();
}
