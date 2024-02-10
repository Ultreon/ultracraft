package com.ultreon.craft.client.model.entity.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.model.EntityModelInstance;
import com.ultreon.craft.client.model.WorldRenderContext;
import com.ultreon.craft.client.model.entity.PlayerModel;
import com.ultreon.craft.client.player.ClientPlayer;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.client.render.EntityTextures;
import com.ultreon.craft.client.texture.TextureManager;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.util.ElementID;
import org.jetbrains.annotations.NotNull;

public class PlayerRenderer extends LivingEntityRenderer<@NotNull Player> {
    public PlayerRenderer(PlayerModel<@NotNull Player> playerModel, Model model) {
        super(playerModel, model);
    }

    @Override
    public void animate(EntityModelInstance<@NotNull Player> instance, WorldRenderContext<@NotNull Player> context) {
        Player player = instance.getEntity();
        if (!(player instanceof ClientPlayer clientPlayer)) return;

        LocalPlayer localPlayer = this.client.player;
        if (localPlayer == null) return;

        float xRot = clientPlayer.xRot;
        float yRot = clientPlayer.yRot;
//        instance.getNode("left_arm").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, -(clientPlayer.bopZ + 3) / 3 + (clientPlayer.walkAnim * 2000 * localPlayer.getWalkingSpeed())).rotate(Vector3.Z, -(clientPlayer.bop + 3) / 3));
//        instance.getNode("right_arm").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (clientPlayer.bopZ + 3) / 3 + (-clientPlayer.walkAnim * 2000 * localPlayer.getWalkingSpeed())).rotate(Vector3.Z, (clientPlayer.bop + 3) / 3));
//        instance.getNode("left_sleve").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, -(clientPlayer.bopZ + 3) / 3 + (clientPlayer.walkAnim * 2000 * localPlayer.getWalkingSpeed())).rotate(Vector3.Z, -(clientPlayer.bop + 3) / 3));
//        instance.getNode("right_sleve").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (clientPlayer.bopZ + 3) / 3 + (-clientPlayer.walkAnim * 2000 * localPlayer.getWalkingSpeed())).rotate(Vector3.Z, (clientPlayer.bop + 3) / 3));
//        instance.getNode("left_leg").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, -clientPlayer.walkAnim * 3000 * localPlayer.getWalkingSpeed()));
//        instance.getNode("right_leg").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, clientPlayer.walkAnim * 3000 * localPlayer.getWalkingSpeed()));
//        instance.getNode("left_pants").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, -clientPlayer.walkAnim * 3000 * localPlayer.getWalkingSpeed()));
//        instance.getNode("right_pants").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, clientPlayer.walkAnim * 3000 * localPlayer.getWalkingSpeed()));

        instance.getNode("leg1").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (float) (clientPlayer.walkAnim * 3000 * clientPlayer.getSpeed())));
        instance.getNode("leg2").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (float) (-clientPlayer.walkAnim * 3000 * clientPlayer.getSpeed())));
        instance.getNode("leg3").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (float) (-clientPlayer.walkAnim * 3000 * clientPlayer.getSpeed())));
        instance.getNode("leg4").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (float) (clientPlayer.walkAnim * 3000 * clientPlayer.getSpeed())));

        float duration = 0.15f;
        var walkAnim = clientPlayer.walkAnim;
        float delta = Gdx.graphics.getDeltaTime();

        if (clientPlayer.isWalking()) clientPlayer.walking = true;
        if (!clientPlayer.walking) clientPlayer.walkAnim = 0;
        else LivingEntityRenderer.updateWalkAnim(clientPlayer, walkAnim, delta, duration);

        float bopDuration = 3.4f;
        var bop = clientPlayer.bop;
        bop -= clientPlayer.inverseBop ? delta : -delta;

        if (bop > bopDuration) {
            float overflow = bopDuration - bop;
            bop = bopDuration - overflow;
            clientPlayer.inverseBop = true;
        } else if (bop < -bopDuration) {
            float overflow = bopDuration + bop;
            bop = -bopDuration - overflow;
            clientPlayer.inverseBop = false;
        }

        clientPlayer.bop = bop;

        float bopZDuration = 2.7f;
        var bopZ = clientPlayer.bopZ;
        bopZ -= clientPlayer.inverseBopZ ? delta : -delta;

        if (bopZ > bopZDuration) {
            float overflow = bopZDuration - bopZ;
            bopZ = bopZDuration - overflow;
            clientPlayer.inverseBopZ = true;
        } else if (bopZ < -bopZDuration) {
            float overflow = bopZDuration + bopZ;
            bopZ = -bopZDuration - overflow;
            clientPlayer.inverseBopZ = false;
        }

        clientPlayer.bopZ = bopZ;

        instance.getNode("head").rotation.setFromMatrix(this.tmp.idt().rotate(Vector3.Y, player.xHeadRot - xRot).rotate(Vector3.X, yRot));
//        instance.getNode("headwear").rotation.setFromMatrix(this.tmp.idt().rotate(Vector3.Y, player.xHeadRot - xRot).rotate(Vector3.X, yRot));
        EntityRenderer.tmp0.set(localPlayer.getPosition());
        EntityRenderer.tmp0.sub(player.getPosition());
        instance.translate(0, -1.625, 0);
        instance.scale(1 / 125.0, 1 / 125.0, 1 / 125.0);
        instance.rotateY(xRot - 180);

        TextureManager textureManager = client.getTextureManager();
        ElementID id = ElementID.parse("dynamic/player_skin/" + this.client.player.getUuid().toString().replace("-", ""));
        if (!textureManager.isTextureLoaded(id)) {
            Texture localSkin = client.getSkinManager().getLocalSkin();
            if (localSkin != null) {
                textureManager.registerTexture(id, localSkin);
                instance.setTextures(id);
            }
        } else {
            instance.setTextures(id);
        }
    }

    @Override
    public EntityTextures getTextures() {
        return new EntityTextures().set(TextureAttribute.Diffuse, UltracraftClient.id("textures/entity/player"));
    }
}
