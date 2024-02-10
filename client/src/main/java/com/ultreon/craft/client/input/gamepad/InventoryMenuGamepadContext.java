package com.ultreon.craft.client.input.gamepad;

import com.ultreon.craft.text.TextObject;

public class InventoryMenuGamepadContext extends InGameMenuGamepadContext {
    public final GamepadMapping<?> closeInventory;

    public InventoryMenuGamepadContext() {
        super();

        this.closeInventory = mappings.register(new GamepadMapping<>(GamepadActions.Y, GamepadMapping.Side.RIGHT, TextObject.translation("ultracraft.gamepad.action.inventory.closeInventory")));
    }
}
