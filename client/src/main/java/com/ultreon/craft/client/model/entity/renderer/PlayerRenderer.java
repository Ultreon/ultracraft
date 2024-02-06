package com.ultreon.craft.client.model.entity.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.model.entity.PlayerModel;
import com.ultreon.craft.client.player.ClientPlayer;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.client.render.EntityTextures;
import com.ultreon.craft.entity.Player;
import com.ultreon.libs.commons.v0.Mth;
import org.jetbrains.annotations.NotNull;

public class PlayerRenderer extends LivingEntityRenderer<@NotNull Player> {
    public PlayerRenderer(PlayerModel<@NotNull Player> playerModel, Model model) {
        super(playerModel, model);
    }

    @Override
    public void animate(ModelInstance instance, Player player) {
        if (!(player instanceof ClientPlayer clientPlayer)) return;

        LocalPlayer localPlayer = this.client.player;
        if (localPlayer == null) return;

        float xRot = clientPlayer.xRot;
        float yRot = clientPlayer.yRot;
        instance.getNode("LeftArm").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, -(clientPlayer.bopZ + 3) / 3 + (clientPlayer.walkAnim * 2000)).rotate(Vector3.Z, -(clientPlayer.bop + 3) / 3));
        instance.getNode("RightArm").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (clientPlayer.bopZ + 3) / 3 + (-clientPlayer.walkAnim * 2000)).rotate(Vector3.Z, (clientPlayer.bop + 3) / 3));
        instance.getNode("LeftLeg").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (-clientPlayer.walkAnim * 2000)));
        instance.getNode("RightLeg").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (clientPlayer.walkAnim * 2000)));

        float swingAmount = 0.15f;
        var walkAnim = clientPlayer.walkAnim;
        float delta = Gdx.graphics.getDeltaTime();

        PlayerRenderer.updateWalkAnim(clientPlayer, walkAnim, delta * Mth.clamp(localPlayer.getActualHorizontalSpeed() * 50f, 5f, 30f), 30f);

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
        float generalScale = 1 / 128f;
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

    private static void updateWalkAnim(ClientPlayer player, float walkAnim, float swingSpeed, float swingAmount) {
        player.walking = player.isWalking();
        float old = player.walkAnim0;

        // We here do a swing animation, based on the delta. Use sin() to make it smooth
        if (!player.walking) {
            swingSpeed = swingSpeed * 1.8f;
            player.walkSignum = player.walkAnim > 0 ? 1 : player.walkSignum == 0 ? 0 : -1;
            if (player.walkSignum != player.walkDir) {
                player.walkDir = player.walkSignum;
            }
            if (player.walkSignum == 0) return;
            walkAnim = (float) Math.sin(((player.walkAnim0 += swingSpeed * player.walkDir) * swingAmount) * MathUtils.degRad) / swingAmount;
            int curSignum = walkAnim > 0 ? 1 : -1;
            if (player.walkSignum != curSignum) {
                player.walkAnim0 = 0;
                player.walkSignum = 0;
            }
        } else {
            player.walkSignum = player.walkAnim > 0 ? 1 : -1;
            float walkAnim1 = (float) Math.sin(((player.walkAnim0 += swingSpeed) * swingAmount) * MathUtils.degRad) / swingAmount;
            float v = walkAnim1 - walkAnim;
            player.walkDir = v > 0 ? 1 : v < 0 ? -1 : player.walkDir;
            walkAnim = walkAnim1;
        }


        player.walkAnim = walkAnim;
    }

    @Override
    public EntityTextures getTextures() {
        return new EntityTextures().set(TextureAttribute.Diffuse, UltracraftClient.id("textures/entity/player"));
    }
}
