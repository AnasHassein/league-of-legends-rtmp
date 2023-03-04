package com.hawolt.rtmp.exception;

import java.io.IOException;

/**
 * thrown when we encounter an unknown error during AMF encoding
 *
 * @author Hawolt
 */
public class EncodingException extends IOException {
    public EncodingException(String message) {
        super(message);
    }
}
