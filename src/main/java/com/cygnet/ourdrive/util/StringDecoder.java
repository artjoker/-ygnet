package com.cygnet.ourdrive.util;


import org.apache.commons.codec.binary.Base64;

/**
 * Created by casten on 5/13/16.
 */
public class StringDecoder {

    public static final String ENCODE_BASE64 = "base64";
    public static final String DECODE_BASE64 = "base64";

    public StringDecoder() {
    }

    public static String encode(String string, String encode) {

        String encoded = "";

        switch (encode) {
            case StringDecoder.ENCODE_BASE64:


                encoded = Base64.encodeBase64String(string.getBytes());
//                System.out.println(encoded);

//                encoded = Base64.encodeBase64String(string.getBytes());

//                byte[] authBytes = string.getBytes(StandardCharsets.UTF_8);
//                encoded = encodedBytes;
                break;

            default:
                break;
        }

        return encoded;
    }

    public static byte[] encode(byte[] byteArray, String encode) {

        byte[] encoded = null;

        switch (encode) {
            case StringDecoder.ENCODE_BASE64:
                encoded = Base64.encodeBase64(byteArray);
//                System.out.println(encoded.length);
                break;

            default:
                break;
        }

        return encoded;
    }

    public static String decode(String string, String encode) {
        return "";
    }
}
