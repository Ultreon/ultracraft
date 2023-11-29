package com.ultreon.craft.client.model.entity.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.model.entity.PlayerModel;
import com.ultreon.craft.client.player.ClientPlayer;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.client.render.EntityTextures;
import com.ultreon.craft.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerRenderer extends LivingEntityRenderer<PlayerModel<@NotNull Player>, @NotNull Player> {
    public PlayerRenderer(PlayerModel<@NotNull Player> model) {
        super(model);

    }

    @Override
    public void animate(ModelInstance instance, Player player) {
        if (!(player instanceof ClientPlayer clientPlayer)) return;

        LocalPlayer localPlayer = this.client.player;
        if (localPlayer == null) return;

        float xRot = clientPlayer.xRot;
        float yRot = clientPlayer.yRot;
        instance.getNode("LeftArm").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, -(clientPlayer.bopZ + 3) / 3 + (clientPlayer.walkAnim * 2000 * localPlayer.getWalkingSpeed())).rotate(Vector3.Z, -(clientPlayer.bop + 3) / 3));
        instance.getNode("RightArm").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (clientPlayer.bopZ + 3) / 3 + (-clientPlayer.walkAnim * 2000 * localPlayer.getWalkingSpeed())).rotate(Vector3.Z, (clientPlayer.bop + 3) / 3));
        instance.getNode("LeftLeg").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, -clientPlayer.walkAnim * 4000 * localPlayer.getWalkingSpeed()));
        instance.getNode("RightLeg").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, clientPlayer.walkAnim * 4000 * localPlayer.getWalkingSpeed()));

        float duration = 0.1f;
        var walkAnim = clientPlayer.walkAnim;
        float delta = Gdx.graphics.getDeltaTime();

        if (clientPlayer.isWalking()) clientPlayer.walking = true;
        if (!clientPlayer.walking) clientPlayer.walkAnim = 0;
        else PlayerRenderer.updateWalkAnim(clientPlayer, walkAnim, delta, duration);

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

        instance.getNode("Head").rotation.setFromMatrix(this.tmp.idt().rotate(Vector3.Y, player.xHeadRot - xRot).rotate(Vector3.X, yRot));
        EntityRenderer.tmp0.set(localPlayer.getPosition());
        EntityRenderer.tmp0.sub(player.getPosition());
        float generalScale = 1 / 4096f;
        instance.transform.idt()
                .setToTranslationAndScaling(0, -1.6f, 0, generalScale, generalScale, generalScale)
//                .scale(1, 1, -1)
                .scale(1.15f, 1.15f, 1.15f)
//                .setToTranslationAndScaling(
//                        (float) EntityRenderer.tmp0.x, (float) EntityRenderer.tmp0.y, (float) EntityRenderer.tmp0.z,
//                        1, 1, 1)
                .rotate(Vector3.Y, clientPlayer.xRot - 180);
        instance.calculateTransforms();
    }

    private static void updateWalkAnim(ClientPlayer player, float walkAnim, float delta, float duration) {
        player.walking = true;
        float old = walkAnim;
        walkAnim -= player.inverseAnim ? delta : -delta;

        if (walkAnim > duration) {
            float overflow = duration - walkAnim;
            walkAnim = duration - overflow;

            player.inverseAnim = true;
        } else if (walkAnim < -duration) {
            float overflow = duration + walkAnim;
            walkAnim = -duration - overflow;
            player.inverseAnim = false;
        }

        if (!player.isWalking() && (old >= 0 && walkAnim < 0 || old <= 0 && walkAnim > 0)) {
            player.walking = false;
        }

        player.walkAnim = walkAnim;
    }

    @Override
    public EntityTextures getTextures() {
        return new EntityTextures().set(TextureAttribute.Diffuse, UltracraftClient.id("textures/entity/player"));
    }
}
