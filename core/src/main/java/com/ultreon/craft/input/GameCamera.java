package com.ultreon.craft.input;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.entity.Player;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3f;

public class GameCamera extends PerspectiveCamera {
    private Vec3d pos;

    public GameCamera(float fieldOfViewY, float viewportWidth, float viewportHeight) {
        super(fieldOfViewY, viewportWidth, viewportHeight);
    }

    public void update(Player player) {
        Vec3d lookVector = player.getLookVector();
        this.position.set(new Vector3());
        this.pos = player.getPosition().add(0, player.getEyeHeight(), 0);
        this.direction.set((float) lookVector.x, (float) lookVector.y, (float) lookVector.z);
        super.update();
    }

    public Vec3d getPos() {
        return this.pos;
    }

    public Vec3f getOffsetPos(Vec3d pos, Player player) {
        return pos.sub(player.getPosition().add(0, player.getEyeHeight(), 0)).f();
    }
}
