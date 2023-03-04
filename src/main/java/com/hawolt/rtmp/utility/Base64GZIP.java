package com.hawolt.rtmp.utility;

import com.hawolt.io.Core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * Utility to handle compressed data
 *
 * @author Hawolt
 */

public class Base64GZIP {
    public static boolean isGzip(byte[] b) {
        return (b[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (b[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }

    public static String unzipBase64(String in) throws IOException {
        return new String(unzip(Base64.getDecoder().decode(in.getBytes())));
    }

    public static byte[] unzip(byte[] b) throws IOException {
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(b));
        return Core.read(gis).toByteArray();
    }

    public static String base64GZIP(String in) throws IOException {
        return Base64.getEncoder().encodeToString(gzip(in));
    }

    public static byte[] gzip(String in) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(outputStream);
        gos.write(in.getBytes());
        gos.flush();
        gos.close();
        return outputStream.toByteArray();
    }
}
