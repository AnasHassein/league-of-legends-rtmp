package com.hawolt.rtmp.amf.encoder;

import com.hawolt.rtmp.amf.AMF0Type;
import com.hawolt.rtmp.amf.AMF3Type;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.exception.EncodingException;
import com.hawolt.rtmp.exception.NotImplementedException;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;


/**
 * AMF encoder supporting both AMF0 and AMF3
 *
 * @author Hawolt
 * @see AMF0Encoder
 * @see AMF3Encoder
 */

public class AMFEncoder implements AMF0Encoder, AMF3Encoder {
    private final long timestamp = System.currentTimeMillis();
    private final Random random = new SecureRandom();

    private byte[] buffer = new byte[0];

    @SuppressWarnings("all")
    public void encode(Object o) throws EncodingException {
        if (o == null) {
            writeByte(AMF3Type.NULL.getTypeIndicator());
        } else if (o instanceof Boolean) {
            AMF3Type type = ((Boolean) o) ? AMF3Type.BOOLEAN_TRUE : AMF3Type.BOOLEAN_FALSE;
            writeByte(type.getTypeIndicator());
        } else if (o instanceof Integer) {
            writeByte(AMF3Type.INTEGER.getTypeIndicator());
            writeAMF3Integer((Integer) o);
        } else if (o instanceof Long) {
            writeByte(AMF3Type.DOUBLE.getTypeIndicator());
            writeAMF3Double((Double) o);
        } else if (o instanceof Double) {
            writeByte(AMF3Type.DOUBLE.getTypeIndicator());
            writeAMF3Double((Double) o);
        } else if (o instanceof String) {
            writeByte(AMF3Type.STRING.getTypeIndicator());
            writeAMF3StringUTF8((String) o);
        } else if (o instanceof Date) {
            writeByte(AMF3Type.DATE.getTypeIndicator());
            writeAMF3Date((LocalDateTime) o);
        } else if (o instanceof Byte[]) {
            writeByte(AMF3Type.BYTEARRAY.getTypeIndicator());
            writeAMF3ByteArray((byte[]) o);
        } else if (o instanceof Object[]) {
            writeByte(AMF3Type.ARRAY.getTypeIndicator());
            writeAMF3Array((Object[]) o);
        } else if (o instanceof TypedObject) {
            writeByte(AMF3Type.OBJECT.getTypeIndicator());
            writeAMF3TypedObject((TypedObject) o);
        } else if (o instanceof Map) {
            writeByte(AMF3Type.ARRAY.getTypeIndicator());
            writeAMF3AssociativeArray((Map<String, Object>) o);
        } else {
            throw new EncodingException("Unexpected type: " + o.getClass().getName());
        }
    }

    public void writeByte(byte b) {
        writeBytes(b);
    }

    public void writeBytes(byte... bytes) {
        byte[] b = new byte[buffer.length + bytes.length];
        System.arraycopy(buffer, 0, b, 0, buffer.length);
        System.arraycopy(bytes, 0, b, buffer.length, bytes.length);
        this.buffer = b;
    }

    public String generateUUID() {
        byte[] b = new byte[16];
        random.nextBytes(b);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            if (i == 4 || i == 6 || i == 8 || i == 10) builder.append('-');
            builder.append(String.format("%02X", b[i]));
        }
        return builder.toString().toLowerCase();
    }

    public void addHeader() {
        byte[] header = new byte[12];
        header[0] = (byte) 0x03;
        long timeOffset = System.currentTimeMillis() - timestamp;
        header[1] = (byte) ((timeOffset & 0xFF0000) >> 16);
        header[2] = (byte) ((timeOffset & 0x00FF00) >> 8);
        header[3] = (byte) ((timeOffset & 0x0000FF));
        header[4] = (byte) ((buffer.length & 0xFF0000) >> 16);
        header[5] = (byte) ((buffer.length & 0x00FF00) >> 8);
        header[6] = (byte) ((buffer.length & 0x0000FF));
        header[7] = (byte) 0x11;
        header[8] = (byte) 0x00;
        header[9] = (byte) 0x00;
        header[10] = (byte) 0x00;
        header[11] = (byte) 0x00;
        List<Byte> list = new LinkedList<>();
        for (int i = 0; i < buffer.length; i++) {
            list.add(buffer[i]);
            if (i % 128 == 127 && i != buffer.length - 1) {
                list.add((byte) 0xC3);
            }
        }

        byte[] bytes = new byte[header.length + list.size()];
        System.arraycopy(header, 0, bytes, 0, header.length);
        for (int i = 0; i < list.size(); i++) {
            bytes[header.length + i] = list.get(i);
        }
        this.buffer = bytes;
    }

    @Override
    public byte[] encodeInvoke(int id, Object data) throws EncodingException, NotImplementedException {
        this.buffer = new byte[0];
        writeByte((byte) 0x00);
        writeByte((byte) 0x05);
        writeAMF0Integer(id);
        writeByte((byte) 0x05);
        writeByte(AMF0Type.AMF3.getTypeIndicator());
        encode(data);
        addHeader();
        return buffer;
    }

    @Override
    public byte[] encodeConnect(Map<String, Object> map) throws EncodingException {
        this.buffer = new byte[0];
        writeAMF0UTF8String("connect");
        writeAMF0Integer(1);
        writeByte(AMF0Type.AMF3.getTypeIndicator());
        writeByte(AMF3Type.ARRAY.getTypeIndicator());
        writeAMF3AssociativeArray(map);
        writeBytes((byte) 0x01, (byte) 0x00);
        writeAMF0UTF8String("nil");
        writeAMF0UTF8String("");
        TypedObject commandMessage = new TypedObject("flex.messaging.messages.CommandMessage");
        commandMessage.put("messageRefType", null);
        commandMessage.put("operation", 5);
        commandMessage.put("correlationId", "");
        commandMessage.put("clientId", null);
        commandMessage.put("destination", "");
        commandMessage.put("messageId", generateUUID());
        commandMessage.put("timestamp", 0D);
        commandMessage.put("timeToLive", 0D);
        commandMessage.put("body", new TypedObject());
        Map<String, Object> headers = new HashMap<>();
        headers.put("DSMessagingVersion", 1D);
        headers.put("DSId", "my-rtmp");
        commandMessage.put("headers", headers);
        writeByte((byte) 0x11);
        encode(commandMessage);
        addHeader();
        buffer[7] = (byte) 0x14;
        return buffer;
    }

    @Override
    public void writeAMF0Integer(int number) {
        byte[] b = new byte[buffer.length + 9];
        System.arraycopy(buffer, 0, b, 0, buffer.length);
        byte[] tmp = new byte[8];
        b[buffer.length] = AMF0Type.NUMBER.getTypeIndicator();
        ByteBuffer.wrap(tmp).putDouble(number);
        System.arraycopy(tmp, 0, b, buffer.length + 1, tmp.length);
        this.buffer = b;
    }

    @Override
    public void writeAMF0UTF8String(String text) {
        int required = 3 + text.length();
        byte[] b = new byte[buffer.length + required];
        System.arraycopy(buffer, 0, b, 0, buffer.length);
        b[buffer.length] = AMF0Type.STRING.getTypeIndicator();
        b[buffer.length + 1] = (byte) ((text.length() & 0xFF00) >> 8);
        b[buffer.length + 2] = (byte) ((text.length() & 0x00FF));
        byte[] bytes = text.getBytes();
        System.arraycopy(bytes, 0, b, buffer.length + 3, text.length());
        this.buffer = b;
    }

    @Override
    public void writeAMF3ByteArray(byte[] bytes) {
        throw new NotImplementedException("Encoding byte arrays is not implemented");
    }

    @Override
    public void writeAMF3Date(LocalDateTime date) {
        writeByte((byte) 0x01);
        writeAMF3Double(date.toEpochSecond(ZoneOffset.UTC) * 1000L);
    }

    @Override
    public void writeAMF3StringUTF8(String text) {
        byte[] bytes = text.getBytes();
        writeAMF3Integer(bytes.length << 1 | 1);
        byte[] b = new byte[buffer.length + bytes.length];
        System.arraycopy(buffer, 0, b, 0, buffer.length);
        System.arraycopy(bytes, 0, b, buffer.length, bytes.length);
        this.buffer = b;
    }

    @Override
    public void writeAMF3Double(double value) {
        if (Double.isNaN(value)) {
            writeBytes((byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xE0, (byte) 0x00, (byte) 0x00, (byte) 0x00);
        } else {
            byte[] tmp = new byte[8];
            byte[] b = new byte[buffer.length + tmp.length];
            System.arraycopy(buffer, 0, b, 0, buffer.length);
            ByteBuffer.wrap(tmp).putDouble(value);
            System.arraycopy(tmp, 0, b, buffer.length, tmp.length);
            this.buffer = b;
        }
    }

    @Override
    public void writeAMF3Integer(int value) {
        if (value >= 0x200000 || value < 0) {
            writeBytes(
                    (byte) (((value >> 22) & 0x7F) | 0x80),
                    (byte) (((value >> 15) & 0x7F) | 0x80),
                    (byte) (((value >> 8) & 0x7F) | 0x80),
                    (byte) (value & 0xFF)
            );
        } else {
            if (value >= 0x4000) {
                writeByte((byte) (((value >> 14) & 0x7F) | 0x80));
            }
            if (value >= 0x80) {
                writeByte((byte) (((value >> 7) & 0x7F) | 0x80));
            }
            writeByte((byte) (value & 0x7F));
        }
    }

    @Override
    public void writeAMF3Array(Object[] arr) throws EncodingException {
        writeAMF3Integer((arr.length << 1) | 1);
        writeByte((byte) 0x01);
        for (Object o : arr) encode(o);
    }

    @Override
    public void writeAMF3AssociativeArray(Map<String, Object> map) throws EncodingException {
        writeByte((byte) 0x01);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            writeAMF3StringUTF8(entry.getKey());
            encode(entry.getValue());
        }
        writeByte((byte) 0x01);
    }

    @Override
    public void writeAMF3TypedObject(TypedObject typedObject) throws EncodingException {
        String type = typedObject.getType();
        if (type == null || type.equals("")) {
            writeBytes((byte) 0x0B, (byte) 0x01);
            for (String key : typedObject.keySet()) {
                writeAMF3StringUTF8(key);
                encode(typedObject.get(key));
            }
            writeByte((byte) 0x01);
        } else if (type.equals("flex.messaging.io.ArrayCollection")) {
            writeByte((byte) 0x07);
            writeAMF3StringUTF8(type);
            encode(typedObject.get("array"));
        } else {
            writeAMF3Integer((typedObject.size() << 4 | 3));
            writeAMF3StringUTF8(type);
            List<String> list = new ArrayList<>();
            for (String key : typedObject.keySet()) {
                writeAMF3StringUTF8(key);
                list.add(key);
            }
            for (String key : list) encode(typedObject.get(key));
        }
    }
}
