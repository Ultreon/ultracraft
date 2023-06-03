package com.ultreon.craft.uri;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.resources.v0.Resource;
import com.ultreon.libs.resources.v0.ResourceManager;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;
import java.util.Base64;

public class MemoryUrlHandler extends URLStreamHandlerProvider {
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (!protocol.equals("mem")) {
            return null;
        }
        return new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) {
                String path = u.getPath();
                Base64.Decoder decoder = Base64.getDecoder();
                return new URLConnection(u) {
                    private InputStream inputStream;
                    @Override
                    public void connect() throws IOException {
                        try {
                            this.inputStream = new ByteArrayInputStream(decoder.decode(path));
                        } catch (Exception e) {
                            throw new IOException("Failed to decode memory", e);
                        }
                    }

                    @Override
                    public boolean getDoInput() {
                        return true;
                    }

                    @Override
                    public boolean getDoOutput() {
                        return false;
                    }

                    @Override
                    public InputStream getInputStream() {
                        return inputStream;
                    }

                    @Override
                    public OutputStream getOutputStream() throws IOException {
                        throw new IOException("Resource urls are read-only.");
                    }
                };
            }
        };
    }
}
