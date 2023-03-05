package com.hawolt.rtmp.utility;

/**
 * Utility for byte arrays
 *
 * @author Hawolt
 */

public class ByteMagic {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String toHex(byte... b) {
        char[] hex = new char[b.length * 2];
        for (int j = 0; j < b.length; j++) {
            int v = b[j] & 0xFF;
            hex[j * 2] = HEX_ARRAY[v >>> 4];
            hex[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hex);
    }

    public static int indexOf(byte[] sequence, byte[] target) {
        return indexOf(sequence, target, 0);
    }

    public static int indexOf(byte[] sequence, byte[] target, int startIndex) {
        int lastPossibleOccurrence = sequence.length - target.length;
        for (int i = Math.max(0, startIndex); i < lastPossibleOccurrence; i++) {
            boolean match = true;
            for (int j = 0; j < target.length; j++) {
                if (sequence[i + j] != target[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }
}
