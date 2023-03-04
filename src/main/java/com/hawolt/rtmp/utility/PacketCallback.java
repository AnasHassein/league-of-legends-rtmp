package com.hawolt.rtmp.utility;

import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.io.RtmpPacket;

/**
 * Notifies us when a Packet is received
 *
 * @author Hawolt
 */
public interface PacketCallback {
    void onPacket(RtmpPacket packet, TypedObject object);
}
