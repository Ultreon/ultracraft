package com.ultreon.craft.resources;

import com.ultreon.libs.commons.v0.util.IOUtils;
import com.ultreon.libs.functions.v0.misc.ThrowingSupplier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Resource {
    protected ThrowingSupplier<InputStream, IOException> opener;
    private byte[] data;
    private BufferedImage image;

    public Resource(ThrowingSupplier<InputStream, IOException> opener) {
        this.opener = opener;
    }

    public void load() {
        try (InputStream inputStream = this.opener.get()) {
            this.data = IOUtils.readAllBytes(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] loadOrGet() {
        if (this.data == null) {
            this.load();
        }

        return this.getData();
    }

    public InputStream loadOrOpenStream() {
        return new ByteArrayInputStream(this.loadOrGet());
    }

    protected Image loadImage() {
        try (InputStream inputStream = this.opener.get()) {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getData() {
        return this.data;
    }

    public ByteArrayInputStream openStream() {
        return new ByteArrayInputStream(this.loadOrGet());
    }

    public Font loadFont() throws FontFormatException {
        try (InputStream inputStream = this.opener.get()) {
            return Font.createFont(Font.TRUETYPE_FONT, inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BufferedImage readImage() throws IOException {
        if (this.image != null) {
            return this.image;
        }

        return this.image = ImageIO.read(this.openStream());
    }
}
