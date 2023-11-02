package com.ultreon.craft.client.input;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.util.Ray;
import com.ultreon.libs.commons.v0.vector.Vec3d;

/**
 * The camera used for the game.
 * Originates at 0,0,0. The world is rendered relative to the camera.
 *
 * @author XyperCode
 * @since 0.1.0
 */
public class GameCamera extends PerspectiveCamera {
    private final UltracraftClient client = UltracraftClient.get();
    private Vec3d eyePosition;

    public GameCamera(float fieldOfViewY, float viewportWidth, float viewportHeight) {
        super(fieldOfViewY, viewportWidth, viewportHeight);
    }

    /**
     * Updates the camera's position and direction based on the player's position and look vector.
     *
     * @param player the player to update the camera for.
     */
    public void update(Player player) {
        var lookVector = player.getLookVector();
        this.eyePosition = player.getPosition().add(0, player.getEyeHeight(), 0);

        if (this.client.isInThirdPerson()) {
            // Move camera backwards when player is in third person.
            var ray = new Ray(this.eyePosition, lookVector.cpy().nor());
            var world = this.client.world;
            if (world != null) {
                var hitResult = world.rayCast(ray, 5);
                var hitPosition = hitResult.getPosition();
                this.position.set((float) (hitPosition.x - this.eyePosition.x), (float) (hitPosition.y - this.eyePosition.y), (float) (hitPosition.z - this.eyePosition.z));
                this.direction.set((float) lookVector.x, (float) lookVector.y, (float) lookVector.z);
            }
        } else {
            // Set the camera's position to zero, and set the camera's direction to the player's look vector.'
            this.position.set(new Vector3());
            this.direction.set((float) lookVector.x, (float) lookVector.y, (float) lookVector.z);
        }

        super.update(true);
    }

    /**
     * @return the eye position in world-coordinates.
     */
    public Vec3d getEyePosition() {
        return this.eyePosition;
    }
}
