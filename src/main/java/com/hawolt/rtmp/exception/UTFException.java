package com.hawolt.rtmp.exception;

/**
 * thrown when we fail to encode an UTF8-String from a AMF3 object
 *
 * @author Hawolt
 * @see com.hawolt.rtmp.amf.decoder.AMFDecoder
 */

public class UTFException extends RuntimeException {
    public UTFException(String message) {
        super(message);
    }
}
