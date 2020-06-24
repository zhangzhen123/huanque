package com.julun.huanque.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nirack on 16-2-19.
 */
public class IoHelper {
    public static byte[] toByteArray (InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream ();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read (buffer))) {
            output.write (buffer, 0, n);
        }
        byte[] bytes = output.toByteArray ();
        output.close ();
        return bytes;
    }
}
