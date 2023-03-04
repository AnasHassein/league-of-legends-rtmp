package com.hawolt.rtmp.amf;

/**
 * All existing AMF0 types
 *
 * @author Hawolt
 */

public enum AMF0Type {
    NUMBER(0x00),
    BOOLEAN(0x01),
    STRING(0x02),
    OBJECT(0x03),
    MOVIECLIP(0x04),
    NULL(0x05),
    UNDEFINED(0x06),
    REFERENCE(0x07),
    MIXEDARRAY(0x08),
    OBJECTTERM(0x09),
    ARRAY(0x0A),
    DATE(0x0B),
    LONGSTRONG(0x0C),
    UNSUPPORTED(0x0D),
    RECORDSET(0x0E),
    XML(0x0F),
    TYPEDOBJECT(0x10),
    AMF3(0x11);

    private final byte typeIndicator;

    AMF0Type(int typeIndicator) {
        this.typeIndicator = (byte) (typeIndicator & 0xFF);
    }

    public byte getTypeIndicator() {
        return typeIndicator;
    }

    private static AMF0Type[] VALUES = AMF0Type.values();

    public static AMF0Type find(byte b) {
        for (AMF0Type type : VALUES) {
            if (type.getTypeIndicator() == b) return type;
        }
        return null;
    }

    @Override
    public String toString() {
        return "AMF0Type{" +
                "typeIndicator=" + typeIndicator +
                '}';
    }
}
