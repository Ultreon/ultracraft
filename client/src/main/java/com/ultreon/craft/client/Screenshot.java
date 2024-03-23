package com.ultreon.craft.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Represents a screenshot grabbed from the game.
 *
 * @since 0.1.0
 * @see Pixmap
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public class Screenshot {
    private final Pixmap pixmap;
    private boolean disposed = false;

    /**
     * Constructs a new Screenshot object with the given Pixmap.
     *
     * @param pixmap the Pixmap to be used for the screenshot
     */
    public Screenshot(Pixmap pixmap) {
        this.pixmap = pixmap;
    }

    /**
     * Gets the Pixmap associated with this screenshot.
     *
     * @return the Pixmap
     */
    public Pixmap getPixmap() {
        return pixmap;
    }

    /**
     * Disposes of the resources held by the object.
     */
    public void dispose() {
        // Marking the object as disposed
        this.disposed = true;

        // Disposing of the pixmap resource
        this.pixmap.dispose();
    }

    /**
     * Check if the object is disposed.
     *
     * @return true if the object is disposed, false otherwise
     */
    public boolean isDisposed() {
        return disposed;
    }

    /**
     * Saves the given filename to a file handle and returns the file handle.
     *
     * @param filename The name of the file to be saved.
     * @return The file handle of the saved file.
     */
    public FileHandle save(String filename) {
        // Get data from UltracraftClient based on the filename
        FileHandle data = UltracraftClient.data(filename);

        // Write the data to a PNG file
        PixmapIO.writePNG(data, pixmap);

        // Copy the screenshot file to clipboard
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
            Clipboard clipboard = UltracraftClient.get().clipboard;
            try(InputStream read = data.read()) {
                clipboard.setContents(new ImageSelection(ImageIO.read(read)), null);
            } catch (IOException e) {
                UltracraftClient.LOGGER.error("Failed to copy screenshot to clipboard", e);
            }
        }

        return data;
    }


    /**
     * Saves the data to a file with the given filename and disposes of any resources.
     *
     * @param filename The name of the file to save the data to
     * @return The FileHandle object representing the saved file
     */
    public FileHandle saveAndDispose(String filename) {
        FileHandle data = save(filename);
        dispose();

        return data;
    }
    /**
     * Takes a screenshot of the current frame with the specified width and height.
     *
     * @param width  The width of the screenshot
     * @param height The height of the screenshot
     * @return A Screenshot object representing the captured image
     */
    public static Screenshot grab(int width, int height) {
        // Set the pixel store alignment
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);

        // Calculate the length of the pixel data
        int dataLen = width * height * 3;
        final ByteBuffer pixels = BufferUtils.newByteBuffer(dataLen);

        // Read the pixel data from the frame buffer
        Gdx.gl.glReadPixels(0, 0, width, height, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);

        // Rearrange the pixel data to match the image format
        byte[] lines = new byte[dataLen];
        final int numBytesPerLine = width * 3;
        for (int i = 0; i < height; i++) {
            ((Buffer) pixels).position((height - i - 1) * numBytesPerLine);
            pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
        }

        // Create a Pixmap and copy the pixel data to it
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGB888);
        BufferUtils.copy(lines, 0, pixmap.getPixels(), lines.length);

        // Return the captured screenshot as a Screenshot object
        return new Screenshot(pixmap);
    }

    static class ImageSelection implements Transferable {
        private final Image image;

        public ImageSelection(Image image) {
            this.image = image;
        }

        // Returns supported flavors
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.imageFlavor };
        }

        // Returns true if flavor is supported
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        // Returns image
        public @NotNull Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }
}
