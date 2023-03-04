package com.hawolt.rtmp.amf.encoder;

import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.exception.EncodingException;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Encoder capabilities for AMF3
 *
 * @author Hawolt
 */

public interface AMF3Encoder {

    void writeAMF3AssociativeArray(Map<String, Object> map) throws EncodingException;

    void writeAMF3Array(Object[] arr) throws EncodingException;

    void writeAMF3TypedObject(TypedObject typedObject) throws EncodingException;

    void writeAMF3Date(LocalDateTime date);

    void writeAMF3StringUTF8(String text);

    void writeAMF3ByteArray(byte[] bytes);

    void writeAMF3Double(double value);

    void writeAMF3Integer(int value);

    byte[] encodeInvoke(int id, Object data) throws EncodingException;

    byte[] encodeConnect(Map<String, Object> map) throws EncodingException;
}
