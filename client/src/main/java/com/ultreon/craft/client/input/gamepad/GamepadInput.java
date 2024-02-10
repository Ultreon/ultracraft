package com.ultreon.craft.client.input.gamepad;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.studiohartman.jamepad.*;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.NavDirection;
import com.ultreon.craft.client.gui.screens.ChatScreen;
import com.ultreon.craft.client.gui.screens.PauseScreen;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.TextEntry;
import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.client.input.GameInput;
import com.ultreon.craft.client.input.keyboard.KeyboardLayout;
import com.ultreon.craft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public class GamepadInput implements Disposable {
    private final Vector2 leftStick = new Vector2();
    private final Vector2 rightStick = new Vector2();

    private ControllerIndex controller;
    private Gamepad gamepad;
    private final float[] oldAxes = new float[GamepadAxis.values().length];
    private final float[] axes = new float[GamepadAxis.values().length];
    private float delay;
    private long nextTick;
    private final UltracraftClient client;
    private KeyboardLayout layout;
    private String virtualKeyboardValue = "";
    private boolean virtualKeyboardOpen;
    private boolean dPadDisabled;
    private boolean screenWasOpen;
    private final ControllerManager controllerManager = new ControllerManager();
    private final BitSet oldButtons = new BitSet(GamepadButton.values().length);
    private final BitSet buttons = new BitSet(GamepadButton.values().length);

    public GamepadInput(UltracraftClient client) {
        this.client = client;

        controllerManager.initSDLGamepad();
    }

    @ApiStatus.Internal
    public void update() {
        try {
            if (pollEvents()) return;
        } catch (ControllerUnpluggedException e) {
            unsetGamepad(0);
        }

        if (this.client.gamepadInput.isVirtualKeyboardOpen()) {
            this.handleScreen(client.player, this.client.virtualKeyboard.getScreen());
            return;
        }

        if (screenWasOpen) {
            screenWasOpen = false;
            return;
        }

        GameInput input = UltracraftClient.get().input;
        this.leftStick.set(this.getJoystick(GamepadJoystick.Left));
        this.rightStick.set(this.getJoystick(GamepadJoystick.Right));
        if ((GamepadContext.get()) instanceof InGameGamepadContext context) {
            LocalPlayer player = context.player();

            client.playerInput.moveX = -context.movePlayer.action().get2DValue().x;
            client.playerInput.moveY = -context.movePlayer.action().get2DValue().y;

            player.setXRot(player.getXRot() + context.lookPlayer.action().get2DValue().y * client.config.get().accessibility.sensitivity * Gdx.graphics.getDeltaTime() * 10);
            player.setYRot(player.getYRot() + context.lookPlayer.action().get2DValue().x * client.config.get().accessibility.sensitivity * Gdx.graphics.getDeltaTime() * 10);

            if (client.player != null) {
                client.player.jumping = context.jump.action().isPressed();
                client.player.setCrouching(context.sneak.action().isPressed());
            }

            if (context.use.action().isPressed() && client.useItemCooldown == 0) {
                input.useItem(player, client.world, UltracraftClient.get().hitResult);
            }

            if (context instanceof BlockTargetGamepadContext blockCtx) {
                if (blockCtx.destroyBlock.action().isJustPressed()) {
                    UltracraftClient.get().startBreaking();
                }
                if (blockCtx.destroyBlock.action().isPressed()) {
                    if (!player.abilities.blockBreak) {
                        return;
                    }
                }
            }
            if (context instanceof EntityTargetGamepadContext entityCtx) {
                if (entityCtx.attack.action().isJustPressed()) {
                    input.attack();
                }
            }

            if (context.gameMenu.action().isJustPressed()) {
                client.showScreen(new PauseScreen());
            }
        } else {
            this.leftStick.set(0, 0);
            this.rightStick.set(0, 0);
        }
    }

    private boolean pollEvents() throws ControllerUnpluggedException {
        controllerManager.update();

        this.setGamepad(0);
        if (this.controller == null) {
            return true;
        }

        if (!controller.isConnected()) {
            unsetGamepad(0);
            return true;
        }

        for (int idx = 0; idx < ControllerButton.values().length; idx++) {
            ControllerButton button = ControllerButton.values()[idx];
            boolean pressed = controller.isButtonPressed(button);
            this.oldButtons.set(idx, this.buttons.get(idx));
            this.buttons.set(idx, pressed);

            if (pressed) {
                UltracraftClient.get().setInputType(InputType.GAMEPAD);
            }
        }

        for (int i = 0; i < GamepadAxis.values().length; i++) {
            GamepadAxis axis = GamepadAxis.values()[i];
            float axisValue = getAxis(axis);
            this.oldAxes[i] = this.axes[i];
            this.axes[i] = axisValue;

            if (axisValue != 0) {
                UltracraftClient.get().setInputType(InputType.GAMEPAD);
            }
        }
        return false;
    }

    private void handleScreen(LocalPlayer player, Screen screen) {
        GamepadContext context = GamepadContext.get();

        if (context instanceof ChatGamepadContext chatContext) {
            this.handleChat(screen, chatContext);
        }

        if (context instanceof InventoryMenuGamepadContext inventoryContext && inventoryContext.closeInventory.action().isJustPressed()) {
            player.closeMenu();
            return;
        }

        if (!(context instanceof MenuGamepadContext menuContext)) {
            return;
        }

        if (isVirtualKeyboardOpen()) {
            if (isButtonJustPressed(GamepadButton.B)) {
                this.closeVirtualKeyboard();
            }
        }

        if (menuContext.activate.action().isJustPressed()) {
            if (screen.focused instanceof TextEntry textEntry && !(screen instanceof ChatScreen)) {
                this.openVirtualKeyboard(textEntry.getValue(), input -> {
                    if (input == null) {
                        throw new IllegalArgumentException("Input cannot be null");
                    }

                    textEntry.value(input);
                });
            } else {
                Widget focused = screen.focused;
                if (focused != null) {
                    focused.click();
                }
            }
        }

        float axisValue = menuContext.scrollY.action().getAxisValue();
        if (axisValue != 0) {
            axisValue = -axisValue;
            Widget focused = screen.focused;
            if (focused != null) {
                screen.mouseWheel(focused.getX(), focused.getY(), axisValue);
            }
        }

        if (menuContext.dpadMove.action().getValue() == 0) dPadDisabled = false;
        if (!dPadDisabled) {
            if (menuContext instanceof CloseableMenuGamepadContext closeableMenuContext && closeableMenuContext.back.action().isJustPressed()) {
                screen.keyPress(Input.Keys.ESCAPE);
                screen.keyRelease(Input.Keys.ESCAPE);
                dPadDisabled = true;
            } else if (menuContext.dpadMove.action().get2DValue().y > 0) {
                screen.navigate(NavDirection.UP);
                dPadDisabled = true;
            } else if (menuContext.dpadMove.action().get2DValue().x < 0) {
                screen.navigate(NavDirection.LEFT);
                dPadDisabled = true;
            } else if (menuContext.dpadMove.action().get2DValue().y < 0) {
                screen.navigate(NavDirection.DOWN);
                dPadDisabled = true;
            } else if (menuContext.dpadMove.action().get2DValue().x > 0) {
                screen.navigate(NavDirection.RIGHT);
                dPadDisabled = true;
            }
        }


        if (nextTick < System.currentTimeMillis()) {
            if (delay > 0) {
                delay--;
                return;
            }
            nextTick = System.currentTimeMillis() + 20;
        } else {
            return;
        }

//        if (menuContext.joystickMove.action().get2DValue().y < 0) {
//            screen.keyPress(Input.Keys.UP);
//            screen.keyRelease(Input.Keys.UP);
//            delay = 10;
//        } else if (menuContext.joystickMove.action().get2DValue().x < 0) {
//            screen.keyPress(Input.Keys.LEFT);
//            screen.keyRelease(Input.Keys.LEFT);
//            delay = 10;
//        } else if (menuContext.joystickMove.action().get2DValue().y > 0) {
//            screen.keyPress(Input.Keys.DOWN);
//            screen.keyRelease(Input.Keys.DOWN);
//            delay = 10;
//        } else if (menuContext.joystickMove.action().get2DValue().x > 0) {
//            screen.keyPress(Input.Keys.RIGHT);
//            screen.keyRelease(Input.Keys.RIGHT);
//            delay = 10;
//        }
    }

    private void handleChat(Screen screen, ChatGamepadContext chatContext) {
        if (!(screen instanceof ChatScreen)) return;

        TextEntry val = screen.children().stream().filter(TextEntry.class::isInstance).map(TextEntry.class::cast).findAny().orElse(null);
        if (chatContext.openKeyboard.action().isJustPressed()) {
            if (val != null) {
                this.openVirtualKeyboard(val.getValue(), input -> {
                    if (input == null) {
                        throw new IllegalArgumentException("Input cannot be null");
                    }

                    val.value(input);
                }, () -> {
                    if (UltracraftClient.get().screen != null) {
                        UltracraftClient.get().screen.keyPress(Input.Keys.ENTER);
                    }
                });
            } else {
                UltracraftClient.LOGGER.warn("Chat screen does not contain any edit boxes.");
            }
        } else if (chatContext.send.action().isJustPressed()) {
            screen.keyPress(Input.Keys.ENTER);
            screen.keyRelease(Input.Keys.ENTER);
        } else if (chatContext.close.action().isJustPressed()) {
            screen.keyPress(Input.Keys.ENTER);
            screen.keyRelease(Input.Keys.ENTER);
        }
    }

    public void closeVirtualKeyboard() {
        this.virtualKeyboardValue = "";
        this.virtualKeyboardOpen = false;
        UltracraftClient.get().virtualKeyboard.close();
    }

    public void openVirtualKeyboard(VirtualKeyboardEditCallback callback) {
        openVirtualKeyboard("", callback);
    }

    public void openVirtualKeyboard(@NotNull String value, VirtualKeyboardEditCallback callback) {
        this.virtualKeyboardValue = value;
        this.virtualKeyboardOpen = true;

        UltracraftClient.get().virtualKeyboard.open(callback, () -> callback.onInput(this.client.virtualKeyboard.getScreen().getInput()));
    }

    public void openVirtualKeyboard(@NotNull String value, VirtualKeyboardEditCallback callback, VirtualKeyboardSubmitCallback submitCallback) {
        this.virtualKeyboardValue = value;
        this.virtualKeyboardOpen = true;

        UltracraftClient.get().virtualKeyboard.open(callback, submitCallback);
    }

    public @NotNull String getVirtualKeyboardValue() {
        return virtualKeyboardValue;
    }

    public boolean isVirtualKeyboardOpen() {
        return virtualKeyboardOpen;
    }

    public boolean isJoystickRight() {
        return leftStick.x > 0 && isXAxis();
    }

    public boolean isJoystickDown() {
        return leftStick.y > 0 && isYAxis();
    }

    public boolean isJoystickLeft() {
        return leftStick.x < 0 && isXAxis();
    }

    public boolean isJoystickUp() {
        return leftStick.y < 0 && isYAxis();
    }

    private boolean isYAxis() {
        return Math.abs(leftStick.x) <= Math.abs(leftStick.y);
    }

    private boolean isXAxis() {
        return Math.abs(leftStick.x) > Math.abs(leftStick.y);
    }

    public float getAxis(GamepadAxis gamepadAxis) {
        if (gamepadAxis == GamepadAxis.DpadX) {
            return isButtonPressed(GamepadButton.DPAD_LEFT) ? -1 : (isButtonPressed(GamepadButton.DPAD_RIGHT) ? 1 : 0);
        } else if (gamepadAxis == GamepadAxis.DpadY) {
            return isButtonPressed(GamepadButton.DPAD_DOWN) ? -1 : (isButtonPressed(GamepadButton.DPAD_UP) ? 1 : 0);
        }

        try {
            ControllerAxis axis = gamepadAxis.sdlAxis();
            if (axis == null) return 0f;
            float v;
            v = controller.getAxisState(axis);

            float deadZone = UltracraftClient.get().config.get().axisDeadZone;
            int signum = v > 0 ? 1 : -1;
            v = Math.abs(v);
            if (v < deadZone) {
                v = Math.max(0, (v - deadZone) / (1 - deadZone)) * signum;
            } else {
                v *= signum;
            }

            if (v != 0) {
                UltracraftClient.get().setInputType(InputType.GAMEPAD);
            }

            if (UltracraftClient.get().getInputType() == InputType.GAMEPAD) {
                return v;
            }

        } catch (ControllerUnpluggedException ignored) {

        }
        return 0;
    }

    private float getOldAxis(GamepadAxis gamepadAxis) {
        return oldAxes[gamepadAxis.sdlAxis().ordinal()];
    }

    public Vector2 getJoystick(GamepadJoystick joystick) {
        return joystick.getValue();
    }

    public float getTrigger(GamepadTrigger trigger) {
        return trigger.getValue();
    }

    @SuppressWarnings("SameParameterValue")
    private void setGamepad(int deviceIndex) throws ControllerUnpluggedException {
        if (this.controller != null && this.controller.isConnected()) return;
        this.controller = controllerManager.getControllerIndex(deviceIndex);
        if (controller == null) return;

        String name = controller.getName();

        this.gamepad = new Gamepad(controller, deviceIndex, name);

        GamepadEvent.GAMEPAD_CONNECTED.factory().onConnectionStatus(this.gamepad);

        UltracraftClient.LOGGER.info("Gamepad {} connected", name);
    }

    private void unsetGamepad(int deviceIndex) {
        if (this.gamepad == null || deviceIndex != this.gamepad.deviceIndex()) return;

        this.controller = null;
        this.gamepad = null;
        GamepadEvent.GAMEPAD_DISCONNECTED.factory().onConnectionStatus(this.gamepad);

        UltracraftClient.get().forceSetInputType(InputType.KEYBOARD_AND_MOUSE, 10);
        UltracraftClient.LOGGER.info("Gamepad disconnected");
    }

    public @Nullable Gamepad getGamepad() {
        return gamepad;
    }

    public ControllerIndex getSDLGamepad() {
        return controller;
    }

    public boolean isButtonPressed(GamepadButton button) {
        ControllerButton idx = button.sdlButton();
        boolean pressed;
        pressed = buttons.get(idx.ordinal());

        if (pressed) UltracraftClient.get().setInputType(InputType.GAMEPAD);
        if (UltracraftClient.get().getInputType() == InputType.GAMEPAD) return pressed;
        return false;
    }

    public boolean isButtonJustPressed(GamepadButton button) {
        ControllerButton btn = button.sdlButton();
        if (!isConnected()) {
            return false;
        }

        boolean pressed = false;
        try {
            pressed = controller.isButtonPressed(btn);
        } catch (ControllerUnpluggedException e) {
            return false;
        }
        if (pressed) UltracraftClient.get().setInputType(InputType.GAMEPAD);
        if (UltracraftClient.get().getInputType() == InputType.GAMEPAD) {
            return pressed;
        }
        return false;
    }

    private boolean wasButtonPressed(ControllerButton btn) {
        return this.oldButtons.get(btn.ordinal());
    }

    public boolean isConnected() {
        return gamepad != null && controller.isConnected();
    }

    public boolean isAvailable() {
        return isConnected() && UltracraftClient.get().getInputType() == InputType.GAMEPAD;
    }

    public boolean isAxisPressed(GamepadAxis axis) {
        return switch (axis) {
            case LeftStickX, LeftStickY -> isButtonPressed(GamepadButton.LEFT_STICK);
            case RightStickX, RightStickY -> isButtonPressed(GamepadButton.RIGHT_STICK);
            case LeftTrigger -> getAxis(GamepadAxis.LeftTrigger) > 0;
            case RightTrigger -> getAxis(GamepadAxis.RightTrigger) > 0;
            default -> false;
        };
    }

    public Vector2 tryGetAxis(GamepadAxis axis) {
        return switch (axis) {
            case LeftStickX, LeftStickY -> getJoystick(GamepadJoystick.Left);
            case RightStickX, RightStickY -> getJoystick(GamepadJoystick.Right);
            default -> new Vector2(0, 0);
        };
    }

    public void updateScreen(Screen screen) {
        this.screenWasOpen = screen != null;

        try {
            if (pollEvents()) return;
        } catch (ControllerUnpluggedException e) {
            unsetGamepad(0);
        }

        UltracraftClient client = UltracraftClient.get();
        LocalPlayer player = client.player;

        if (screen != null) {
            if (virtualKeyboardOpen) {
                handleScreen(null, this.client.virtualKeyboard.getScreen());
                return;
            }
            handleScreen(player, screen);
        }
    }

    public UltracraftClient getClient() {
        return client;
    }

    public void setLayout(KeyboardLayout layout) {
        this.layout = layout;
    }

    public KeyboardLayout getLayout() {
        return layout;
    }

    public void handleVirtualKeyboardClosed(String value) {
        this.virtualKeyboardValue = value;
        this.virtualKeyboardOpen = false;
    }

    public boolean isTriggerJustPressed(GamepadAxis axis) {
        return getAxis(axis) > 0 && getOldAxis(axis) == 0;
    }

    @Override
    public void dispose() {
        controllerManager.quitSDLGamepad();
    }
}
