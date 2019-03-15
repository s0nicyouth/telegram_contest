package com.syouth.telegramapp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

public class Streams {

    public static String readFull(InputStream in) throws IOException {
        try (InputStreamReader inputStreamReader =
                     new InputStreamReader(in, Charset.forName("UTF-8"));
             StringWriter result = new StringWriter()) {
            char[] buffer = new char[512];
            int read;
            while ((read = inputStreamReader.read(buffer)) != -1) {
                result.write(buffer, 0, read);
            }
            return result.toString();
        }
    }
}
