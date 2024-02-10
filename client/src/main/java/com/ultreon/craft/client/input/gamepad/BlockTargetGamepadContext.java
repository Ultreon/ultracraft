package com.ultreon.craft.client.input.gamepad;

import com.ultreon.craft.text.TextObject;

public class BlockTargetGamepadContext extends InGameGamepadContext {
    public static final BlockTargetGamepadContext INSTANCE = new BlockTargetGamepadContext();
    public final GamepadMapping<?> destroyBlock;

    public BlockTargetGamepadContext() {
        super();

        this.destroyBlock = mappings.register(new GamepadMapping<>(GamepadActions.RIGHT_TRIGGER, GamepadMapping.Side.RIGHT, TextObject.translation("ultracraft.gamepad.action.inGame.destroyBlock")));
    }
}
