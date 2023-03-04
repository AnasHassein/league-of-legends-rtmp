package com.hawolt.rtmp.exception;


/**
 * thrown when AMF is unable to be decoded or encoded
 *
 * @author Hawolt
 * @see com.hawolt.rtmp.amf.decoder.AMFDecoder
 * @see com.hawolt.rtmp.amf.encoder.AMFEncoder
 */
public class NotImplementedException extends RuntimeException {
    public NotImplementedException(String message) {
        super(message);
    }
}
