package com.ultreon.craft.client.uri;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;
import java.util.Base64;

public class DataUrlHandler extends URLStreamHandlerProvider {
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (!protocol.equals("data")) {
            return null;
        }
        return new Handler();
    }

    private static class Handler extends URLStreamHandler {
        @Override
        protected URLConnection openConnection(URL url) {
            return new DataURLConnection(url);
        }

    }
    private static class DataURLConnection extends URLConnection {
        private ByteArrayOutputStream output;

        protected DataURLConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
            if (connected) {
                return;
            }

            String s = url.toString().substring(5); // strip off "data:"
            boolean base64 = false;
            String charset = "US-ASCII";

            int comma = s.indexOf(',');
            if (comma > 0) {
                String before = s.substring(0, comma);
                s = s.substring(comma + 1);

                if (before.endsWith(";base64")) {
                    base64 = true;
                    before = before.substring(0, before.length() - 7);
                }

                int charsetStart = before.indexOf(";charset=");
                if (charsetStart > 0) {
                    charset = before.substring(charsetStart + 9);
                }
            }

            byte[] bytes;
            if (base64) {
                bytes = Base64.getDecoder().decode(s);
            } else {
                bytes = URLDecoder.decode(s, charset).getBytes(charset);
            }

            output = new ByteArrayOutputStream();
            output.write(bytes);
            output.flush();

            connected = true;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            connect();
            return new ByteArrayInputStream(output.toByteArray());
        }
    }
}
