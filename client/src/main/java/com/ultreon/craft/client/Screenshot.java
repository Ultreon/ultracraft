package com.ultreon.craft.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class Screenshot {
    private final Pixmap pixmap;
    private boolean disposed = false;

    public Screenshot(Pixmap pixmap) {
        this.pixmap = pixmap;
    }

    public Pixmap getPixmap() {
        return pixmap;
    }

    public void dispose() {
        this.disposed = true;
        this.pixmap.dispose();
    }

    public boolean isDisposed() {
        return disposed;
    }

    public FileHandle save(String filename) {
        FileHandle data = UltracraftClient.data(filename);
        PixmapIO.writePNG(data, pixmap);

        return data;
    }

    public FileHandle saveAndDispose(String filename) {
        FileHandle data = save(filename);
        dispose();

        return data;
    }

    public static Screenshot grab(int width, int height) {
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);

        int dataLen = width * height * 3;
        final ByteBuffer pixels = BufferUtils.newByteBuffer(dataLen);

        Gdx.gl.glReadPixels(0, 0, width, height, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);

        byte[] lines = new byte[dataLen];
        final int numBytesPerLine = width * 3;
        for (int i = 0; i < height; i++) {
            ((Buffer)pixels).position((height - i - 1) * numBytesPerLine);
            pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
        }

        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGB888);
        BufferUtils.copy(lines, 0, pixmap.getPixels(), lines.length);

        return new Screenshot(pixmap);
    }
}
