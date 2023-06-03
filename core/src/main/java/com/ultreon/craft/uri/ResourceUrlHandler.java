package com.ultreon.craft.uri;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.resources.v0.Resource;
import com.ultreon.libs.resources.v0.ResourceManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

public class ResourceUrlHandler extends URLStreamHandlerProvider {
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (!protocol.equals("res")) {
            return null;
        }
        return new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) {
                String location = u.getHost();
                String path = u.getPath();
                Identifier identifier = new Identifier(location, path);
                UltreonCraft ultreonCraft = UltreonCraft.get();
                return new URLConnection(u) {
                    private ResourceManager resourceManager;

                    private InputStream inputStream;
                    @Override
                    public void connect() throws IOException {
                        if (ultreonCraft == null)
                            throw new IOException("Connection opened before game initialization");

                        this.resourceManager = ultreonCraft.getResourceManager();

                        if (this.resourceManager == null)
                            throw new IOException("Connection opened before game initialization");

                        var resource = this.resourceManager.getResource(identifier);

                        if (resource == null)
                            throw new FileNotFoundException("Resource not found: " + identifier);

                        var str = resource.loadOrOpenStream();

                        if (str == null)
                            throw new IOException("Failed to load or open the resource stream");

                        this.inputStream = str;
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

                    public ResourceManager getResourceManager() {
                        return resourceManager;
                    }
                };
            }
        };
    }
}
