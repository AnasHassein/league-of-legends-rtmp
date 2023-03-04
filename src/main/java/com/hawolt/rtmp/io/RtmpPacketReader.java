package com.hawolt.rtmp.io;

import com.hawolt.logger.Logger;
import com.hawolt.rtmp.RtmpClient;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.amf.decoder.AMFDecoder;
import com.hawolt.rtmp.utility.ByteMagic;
import com.hawolt.rtmp.utility.PacketCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser for incoming rtmp traffic
 *
 * @author Hawolt
 * @see RtmpPacket
 */

public class RtmpPacketReader implements Runnable {
    private final AMFDecoder decoder = new AMFDecoder();
    private final RtmpClient client;

    public RtmpPacketReader(RtmpClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            Map<Integer, RtmpPacket> map = new HashMap<>();
            while (client.isConnected()) {
                try {
                    byte initialHeader = client.read();
                    int channel = initialHeader & 0x2F;
                    int headerType = initialHeader & 0xC0;

                    int headerSize = 0;
                    if (headerType == 0x00) {
                        headerSize = 12;
                    } else if (headerType == 0x40) {
                        headerSize = 8;
                    } else if (headerType == 0x80) {
                        headerSize = 4;
                    } else if (headerType == 0xC0) {
                        headerSize = 1;
                    }

                    if (!map.containsKey(channel)) map.put(channel, new RtmpPacket(initialHeader));
                    RtmpPacket packet = map.get(channel);

                    if (headerSize > 1) {
                        byte[] header = new byte[headerSize - 1];
                        packet.setHeaderSize(header.length);
                        for (int i = 0; i < header.length; i++) {
                            header[i] = client.read();
                            packet.addToHeader(header[i]);
                        }

                        if (headerSize >= 8) {
                            int size = 0;
                            for (int i = 3; i < 6; i++) {
                                size = size * 256 + (header[i] & 0xFF);
                            }
                            packet.setBodySize(size);
                            packet.setMessageType(header[6]);
                        }
                    }

                    for (int i = 0; i < 128; i++) {
                        byte b = client.read();
                        packet.addToBody(b);
                        if (packet.isComplete()) break;
                    }

                    Logger.debug(packet);
                    if (!packet.isComplete()) continue;
                    map.remove(channel);

                    TypedObject result = null;
                    if (packet.getMessageType() == 0x14) { // CONNECT AMF0
                        result = decoder.decode(packet.getBody(), new TypedObject("Connect"));
                    } else if (packet.getMessageType() == 0x11) { // INVOKE AMF3
                        result = decoder.decode(packet.getBody(), new TypedObject("Invoke"));
                    } else if (packet.getMessageType() == 0x06) {
                        byte[] buffer = packet.getBody();
                        int windowSize = 0;
                        for (int i = 0; i < 4; i++) {
                            windowSize = windowSize * 256 + (buffer[i] & 0xFF);
                        }
                        int type = buffer[4];
                        client.setPeerBandwidth(windowSize);
                    } else if (packet.getMessageType() == 0x03) {
                        byte[] buffer = packet.getBody();
                        int acknowledgeSize = 0;
                        for (int i = 0; i < 4; i++) {
                            acknowledgeSize = acknowledgeSize * 256 + (buffer[i] & 0xFF);
                        }
                        Logger.info("ACK {}", acknowledgeSize);
                    } else {
                        // Unsupported Message Type, reference: https://en.wikipedia.org/wiki/Real-Time_Messaging_Protocol#Packet_structure
                        Logger.warn("Unknown Message Type {} with data {}", packet.getMessageType(), ByteMagic.toHex(packet.getBody()));
                    }
                    if (result == null) continue;
                    Integer invokeId = result.getInteger("invokeId");
                    boolean asynchronous = invokeId == null || invokeId == 0;
                    Object id = !asynchronous ? invokeId : result.getTypedObject("data").getTypedObject("body").getString("messageId");
                    boolean available = client.isPacketCallbackRegistered(id);
                    PacketCallback callback = available ? client.getPacketCallback(id) : client.getDefaultCallback();
                    if (available) client.unregister(id);
                    if (callback == null) client.cache(id, packet, result);
                    else callback.onPacket(packet, result);
                } catch (Exception e) {
                    Logger.error(e);
                    Logger.fatal("[rtmp-reader] {}", e.getMessage());
                    break;
                }
            }
            //TODO maybe do something here when exceptions are thrown
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
