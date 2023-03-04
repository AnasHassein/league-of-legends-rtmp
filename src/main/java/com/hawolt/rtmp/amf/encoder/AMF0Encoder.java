package com.hawolt.rtmp.amf.encoder;

/**
 * Encoder capabilities for AMF0
 *
 * @author Hawolt
 */

public interface AMF0Encoder {

    void writeAMF0Integer(int number);

    void writeAMF0UTF8String(String text);
}
