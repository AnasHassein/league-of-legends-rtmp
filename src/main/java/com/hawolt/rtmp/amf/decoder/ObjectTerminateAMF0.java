package com.hawolt.rtmp.amf.decoder;

import com.hawolt.rtmp.amf.AMF0Type;

/**
 * Helper class to represent AMF0 OBJECTTERM
 *
 * @author Hawolt
 */
public class ObjectTerminateAMF0 {
    public static ObjectTerminateAMF0 INSTANCE = new ObjectTerminateAMF0();

    static AMF0Type internal = AMF0Type.OBJECTTERM;

    @Override
    public String toString() {
        return "ObjectTerminate{" +
                "internal=" + internal +
                '}';
    }
}
