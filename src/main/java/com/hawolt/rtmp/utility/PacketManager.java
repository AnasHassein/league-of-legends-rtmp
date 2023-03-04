package com.hawolt.rtmp.utility;

import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.io.RtmpPacket;


/**
 * Manager for Packets and their Callbacks
 *
 * @author Hawolt
 * @see PacketCallback
 */

public interface PacketManager {
    void cache(Object id, RtmpPacket packet, TypedObject object);

    void register(Object id, PacketCallback callback);

    boolean isPacketCallbackRegistered(Object o);

    PacketCallback getPacketCallback(Object o);

    void unregister(Object o);
}
