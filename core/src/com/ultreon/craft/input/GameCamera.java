package com.ultreon.craft.input;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.ultreon.craft.entity.Player;

public class GameCamera extends PerspectiveCamera {
    public GameCamera(float fieldOfViewY, float viewportWidth, float viewportHeight) {
        super(fieldOfViewY, viewportWidth, viewportHeight);
    }

    public void update(Player player) {
        this.position.set(player.getPosition().add(0, player.getEyeHeight(), 0));
        this.direction.set(player.getLookVector());
        super.update(true);
    }
}
