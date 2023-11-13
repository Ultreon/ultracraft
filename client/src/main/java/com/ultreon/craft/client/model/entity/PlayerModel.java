package com.ultreon.craft.client.model.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.ultreon.craft.client.render.EntityTextures;
import com.ultreon.craft.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerModel<T extends Player> extends LivingEntityModel<T> {
    public static final PlayerModel<@NotNull Player> INSTANCE = new PlayerModel<>();

    @Override
    protected void build(ModelBuilder builder, EntityTextures textures) {
        Material material = textures.createMaterial();

        Texture tex = textures.get(TextureAttribute.Diffuse);
        builder.node("left_leg", this.box(4, 0, 2, 4, 12, 4).uv(0, 0).build(tex, material));
        builder.node("right_leg", this.box(8, 0, 2, 4, 12, 4).uv(0, 32).build(tex, material));
        builder.node("left_arm", this.box(0, 12, 2, 4, 12, 4).uv(0, 64).build(tex, material));
        builder.node("right_arm", this.box(12, 12, 2, 4, 12, 4).uv(0, 96).build(tex, material));
        builder.node("body", this.box(0, 12, 2, 8, 12, 4).uv(0, 128).build(tex, material));
        builder.node("head", this.box(0, 24, 0, 6, 6, 6).uv(0, 160).build(tex, material));
    }
}
