package com.ultreon.craft.client.input.gamepad;

import com.studiohartman.jamepad.ControllerIndex;

public record Gamepad(ControllerIndex gdxController, int deviceIndex,
                      String name) {
}
