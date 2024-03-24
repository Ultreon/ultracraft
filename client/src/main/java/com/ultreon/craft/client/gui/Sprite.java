package com.ultreon.craft.client.gui;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.resources.ResourceFileHandle;
import com.ultreon.craft.util.Identifier;
import org.fusionyaml.library.FusionYAML;
import org.jetbrains.annotations.ApiStatus;

public class Sprite {
    private static final FusionYAML YAML = new FusionYAML.Builder().allowUnicode(true).build();
    private final ResourceFileHandle handle;
    private final Texture texture;
    private final int width;
    private final int height;
    private final Meta meta;

    public Sprite(Identifier id) {
        Identifier mappedId = id.mapPath(path -> "textures/" + path + ".png");
        this.handle = new ResourceFileHandle(mappedId);
        Identifier mappedSpriteId = id.mapPath(path -> "textures/" + path + ".sprite.yml");
        var spriteRes = new ResourceFileHandle(mappedSpriteId);
        this.texture = new Texture(handle);
        this.width = this.texture.getWidth();
        this.height = this.texture.getHeight();

        this.meta = YAML.deserialize(spriteRes.readString(), Meta.class);
    }

    @ApiStatus.Internal
    public void render(Renderer renderer, int x, int y, int width, int height) {
        renderer.blit(this.texture, x, y, width, height, 0, 0, this.width, this.height, this.width, this.height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static final class Meta {
        public final int borderTop;
        public final int borderBottom;
        public final int borderLeft;
        public final int borderRight;
        public final boolean fullImage;


        public Meta(int borderTop, int borderBottom, int borderLeft, int borderRight) {
            this(borderTop, borderBottom, borderLeft, borderRight, true);
        }

        public Meta(int borderTop, int borderBottom, int borderLeft, int borderRight, boolean fullImage) {
            this.borderTop = borderTop;
            this.borderBottom = borderBottom;
            this.borderLeft = borderLeft;
            this.borderRight = borderRight;
            this.fullImage = fullImage;
        }
    }
}
