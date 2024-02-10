package com.ultreon.craft.client.input.gamepad;

import com.ultreon.craft.text.TextObject;

public class EntityTargetGamepadContext extends InGameGamepadContext {
    public static final EntityTargetGamepadContext INSTANCE = new EntityTargetGamepadContext();
    public final GamepadMapping<?> attack;

    protected EntityTargetGamepadContext() {
        super();

        this.attack = mappings.register(new GamepadMapping<>(GamepadActions.RIGHT_TRIGGER, GamepadMapping.Side.RIGHT, TextObject.translation("ultracraft.gamepad.action.inGame.attack")));
    }
}
