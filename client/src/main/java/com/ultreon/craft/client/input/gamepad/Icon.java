package com.ultreon.craft.client.input.gamepad;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.util.ElementID;

public enum Icon {
    ButtonA(128, 16),
    ButtonB(144, 16),
    ButtonX(160, 16),
    ButtonY(176, 16),
    AnyButton(192, 16),

    AnyJoyStick(0, 80),
    AnyJoyStickUp(144, 80),
    AnyJoyStickRight(160, 80),
    AnyJoyStickDown(176, 80),
    AnyJoyStickLeft(192, 80),
    AnyJoyStickPress(256, 80),
    AnyJoyStickMove(240, 80),

    LeftJoyStick(128, 112),
    LeftJoyStickUp(144, 112),
    LeftJoyStickRight(160, 112),
    LeftJoyStickDown(176, 112),
    LeftJoyStickLeft(192, 112),
    LeftJoyStickX(208, 112),
    LeftJoyStickY(224, 112),
    LeftJoyStickPress(256, 112),
    LeftJoyStickMove(240, 112),

    RightJoyStick(128, 144),
    RightJoyStickUp(144, 144),
    RightJoyStickRight(160, 144),
    RightJoyStickDown(176, 144),
    RightJoyStickLeft(192, 144),
    RightJoyStickX(208, 144),
    RightJoyStickY(224, 144),
    RightJoyStickPress(256, 144),
    RightJoyStickMove(240, 144),

    LeftTrigger(112, 272),
    RightTrigger(128, 272),

    LeftShoulder(112, 304),
    RightShoulder(128, 304),

    XboxGuide(64, 304),
    XboxMenu(80, 304),

    Dpad(0, 32),
    DpadUp(16, 32),
    DpadRight(32, 32),
    DpadDown(48, 32),
    DpadLeft(64, 32),
    DpadLeftRight(80, 32),
    DpadUpDown(96, 32),
    DpadAny(112, 32),

    Select(),
    Start(),

    MouseMove(128, 48),
    MouseLeft(144, 48),
    MouseRight(160, 48),
    MouseMiddle(176, 48),
    MouseScrollUp(192, 48),
    MouseScrollDown(208, 48),
    MouseScroll(224, 48),
    PS4TouchPad(400, 336);

    private static final ElementID TEXTURE = UltracraftClient.id("textures/gui/icons.png");
    public final int u;
    public final int v;

    Icon() {
        this(0, 0);
    }

    Icon(int u, int v) {
        this.u = u;
        this.v = v;
    }

    public void render(Renderer renderer, int x, int y) {
        renderer.blit(TEXTURE, x, y, 16, 16, u, v, 16, 16, 544, 384);
    }
}
