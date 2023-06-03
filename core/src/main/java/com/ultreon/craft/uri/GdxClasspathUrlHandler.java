package com.ultreon.craft.uri;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

public class GdxClasspathUrlHandler extends URLStreamHandlerProvider {
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (!protocol.equals("gdx-classpath")) {
            return null;
        }
        return new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) {
                FileHandle classpath = Gdx.files.classpath(u.getPath());
                return new URLConnection(u) {
                    private InputStream inputStream;

                    @Override
                    public void connect() {
                        this.inputStream = classpath.read();
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
                        throw new IOException("GDX Internal files are read-only.");
                    }
                };
            }
        };
    }
}
