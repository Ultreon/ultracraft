package com.ultreon.craft.client.input;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.debug.DebugFlags;
import com.ultreon.craft.debug.inspect.InspectionNode;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.util.Ray;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3f;

/**
 * The camera used for the game.
 * Originates at 0,0,0. The world is rendered relative to the camera.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class GameCamera extends PerspectiveCamera {
    private final UltracraftClient client = UltracraftClient.get();
    private final InspectionNode<GameCamera> node;
    private Vector3 hitPosition;
    private Vec3d eyePosition;
    private HitResult hitResult;
    private Player player;

    public GameCamera(float fieldOfViewY, float viewportWidth, float viewportHeight) {
        super(fieldOfViewY, viewportWidth, viewportHeight);

        if (DebugFlags.INSPECTION_ENABLED) {
            this.node = this.client.inspection.createNode("camera", () -> this);
            this.node.create("position", () -> this.position);
            this.node.create("direction", () -> this.direction);
            this.node.create("up", () -> this.up);
            this.node.create("near", () -> this.near);
            this.node.create("far", () -> this.far);
            this.node.create("viewportWidth", () -> this.viewportWidth);
            this.node.create("viewportHeight", () -> this.viewportHeight);
            this.node.create("fieldOfView", () -> this.fieldOfView);
            this.node.create("hitPosition", () -> this.hitResult.getPosition());
            this.node.create("relHitPosition", () -> this.hitPosition);
            this.node.create("eyePosition", () -> this.eyePosition);
            this.node.create("playerPosition", () -> this.player.getPosition());
        }
    }

    /**
     * Updates the camera's position and direction based on the player's position and look vector.
     *
     * @param player the player to update the camera for.
     */
    public void update(Player player) {
        var lookVec = player.getLookVector();
        this.eyePosition = player.getPosition().add(0, player.getEyeHeight(), 0);
        this.player = player;

        if (this.client.isInThirdPerson()) {
            this.updateThirdPerson(lookVec);
        } else {
            this.node.remove("hitPosition");
            this.node.remove("eyePosition");
            this.node.remove("playerPosition");
            // Set the camera's position to zero, and set the camera's direction to the player's look vector.
            this.position.set(new Vector3());
            this.direction.set((float) lookVec.x, (float) lookVec.y, (float) lookVec.z);
        }

        super.update(true);
    }

    private void updateThirdPerson(Vec3d lookVec) {
        // Move camera backwards when player is in third person.
        var ray = new Ray(this.eyePosition, lookVec.cpy().neg().nor());
        var world = this.client.world;
        if (world != null) {
            this.hitResult = world.rayCast(ray, 5.1f);
            Vector3 lookVector = new Vector3((float) lookVec.x, (float) lookVec.y, (float) lookVec.z);
            if (this.hitResult.isCollide()) {
                Vec3f normal = this.hitResult.getNormal().f();
                Vector3 gdxNormal = new Vector3(normal.x, normal.y, normal.z);
                Vector3 hitOffset = lookVector.cpy().nor()
                        .scl((float) -this.hitResult.distance)
                        .sub(gdxNormal.scl(-0.1f).rotate(lookVector, 360));
                this.hitPosition = new Vector3(0, 0, 0).add(hitOffset);
            } else {
                this.hitPosition = new Vector3(0, 0, 0).add(lookVector.cpy().nor().scl(-5));
            }
            this.position.set(this.hitPosition.x, this.hitPosition.y, this.hitPosition.z);
            this.direction.set(lookVector);
        }
    }

    /**
     * @return the eye position in world-coordinates.
     */
    public Vec3d getEyePosition() {
        return this.eyePosition;
    }
}
