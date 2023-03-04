package com.hawolt.rtmp.io;

/**
 * Object representing a rtmp packet
 *
 * @author Hawolt
 */

public class RtmpPacket {
    private final byte initialHeader;
    private int messageType, headerPosition, headerSize, bodyPosition, bodySize;
    private byte[] header, body;

    public RtmpPacket(byte initialHeader) {
        this.initialHeader = initialHeader;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public void setHeaderSize(int headerSize) {
        this.header = new byte[this.headerSize = headerSize];
    }

    public void addToHeader(byte b) {
        this.header[headerPosition++] = b;
    }

    public void setBodySize(int bodySize) {
        this.body = new byte[this.bodySize = bodySize];
    }

    public void addToBody(byte b) {
        this.body[bodyPosition++] = b;
    }

    public byte getInitialHeader() {
        return initialHeader;
    }

    public int getMessageType() {
        return messageType;
    }

    public int getHeaderPosition() {
        return headerPosition;
    }

    public int getHeaderSize() {
        return headerSize;
    }

    public int getBodyPosition() {
        return bodyPosition;
    }

    public int getBodySize() {
        return bodySize;
    }

    public byte[] getHeader() {
        return header;
    }

    public byte[] getBody() {
        return body;
    }

    public boolean isComplete() {
        return this.headerSize == this.headerPosition && this.bodySize == this.bodyPosition;
    }

    @Override
    public String toString() {
        return "RtmpPacket{" +
                "complete=" + isComplete() +
                ", initialHeader=" + initialHeader +
                ", headerPosition=" + headerPosition +
                ", headerSize=" + headerSize +
                ", bodyPosition=" + bodyPosition +
                ", bodySize=" + bodySize +
                '}';
    }
}
