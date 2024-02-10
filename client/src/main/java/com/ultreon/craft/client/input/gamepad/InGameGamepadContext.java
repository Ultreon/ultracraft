package com.ultreon.craft.client.input.gamepad;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.text.TextObject;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.ultreon.craft.client.input.gamepad.GamepadMapping.*;

public class InGameGamepadContext extends GamepadContext {
    public static final InGameGamepadContext INSTANCE = new InGameGamepadContext();

    public final GamepadMapping<?> jump;
    public final GamepadMapping<?> run;
    public final GamepadMapping<?> sneak;
    public final GamepadMapping<?> use;
    public final GamepadMapping<?> inventory;
    public final GamepadMapping<?> swapHands;
    public final GamepadMapping<?> movePlayer;
    public final GamepadMapping<?> lookPlayer;
    public final GamepadMapping<?> gameMenu;
    public final GamepadMapping<?> pickItem;
    public final GamepadMapping<?> drop;
    public final GamepadMapping<?> playerList;
    public final GamepadMapping<?> chat;
    public final GamepadMapping<GamepadButton> itemLeft;
    public final GamepadMapping<GamepadButton> itemRight;

    protected InGameGamepadContext() {
        super();

        this.jump = mappings.register(new GamepadMapping<>(GamepadActions.A, Side.LEFT, TextObject.translation("ultracraft.gamepad.action.inGame.jump")));
        this.run = mappings.register(new GamepadMapping<>(GamepadActions.PRESS_LEFT_STICK, Side.RIGHT, TextObject.translation("ultracraft.gamepad.action.inGame.run")));
        this.sneak = mappings.register(new GamepadMapping<>(GamepadActions.PRESS_RIGHT_STICK, Side.RIGHT, TextObject.translation("ultracraft.gamepad.action.inGame.sneak")));
        this.use = mappings.register(new GamepadMapping<>(GamepadActions.LEFT_TRIGGER, Side.LEFT, TextObject.translation("ultracraft.gamepad.action.inGame.use")));
        this.inventory = mappings.register(new GamepadMapping<>(GamepadActions.Y, Side.RIGHT, TextObject.translation("ultracraft.gamepad.action.inGame.inventory")));
        this.swapHands = mappings.register(new GamepadMapping<>(GamepadActions.X, Side.RIGHT, TextObject.translation("ultracraft.gamepad.action.inGame.swapHands")));
        this.lookPlayer = mappings.register(new GamepadMapping<>(GamepadActions.MOVE_RIGHT_STICK, Side.LEFT, TextObject.translation("ultracraft.gamepad.action.inGame.lookPlayer")));
        this.movePlayer = mappings.register(new GamepadMapping<>(GamepadActions.MOVE_LEFT_STICK, Side.LEFT, TextObject.translation("ultracraft.gamepad.action.inGame.movePlayer")));
        this.gameMenu = mappings.register(new GamepadMapping<>(GamepadActions.BACK, Side.RIGHT, TextObject.translation("ultracraft.gamepad.action.inGame.gameMenu")));
        this.pickItem = mappings.register(new GamepadMapping<>(GamepadActions.DPAD_UP, Side.LEFT, TextObject.translation("ultracraft.gamepad.action.inGame.pickItem"), false));
        this.drop = mappings.register(new GamepadMapping<>(GamepadActions.DPAD_DOWN, Side.LEFT, TextObject.translation("ultracraft.gamepad.action.inGame.drop")));
        this.playerList = mappings.register(new GamepadMapping<>(GamepadActions.DPAD_LEFT, Side.LEFT, TextObject.translation("ultracraft.gamepad.action.inGame.playerList"), false));
        this.chat = mappings.register(new GamepadMapping<>(GamepadActions.DPAD_RIGHT, Side.LEFT, TextObject.translation("ultracraft.gamepad.action.inGame.chat"), false));
        this.itemLeft = mappings.register(new GamepadMapping<>(GamepadActions.LEFT_SHOULDER, Side.LEFT, TextObject.translation("ultracraft.gamepad.action.inGame.itemLeft"), false));
        this.itemRight = mappings.register(new GamepadMapping<>(GamepadActions.RIGHT_SHOULDER, Side.LEFT, TextObject.translation("ultracraft.gamepad.action.inGame.itemRight"), false));
    }

    @Override
    public int getYOffset() {
        return super.getYOffset();
    }

    public @NotNull LocalPlayer player() {
        return Objects.requireNonNull(UltracraftClient.get().player);
    }

    public @NotNull ClientWorld level() {
        return Objects.requireNonNull(UltracraftClient.get().world);
    }
}
