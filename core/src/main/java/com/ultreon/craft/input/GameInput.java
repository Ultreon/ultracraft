package com.ultreon.craft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.ControllerPowerLevel;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.ultreon.craft.Constants;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.debug.Debugger;
import com.ultreon.craft.render.gui.screens.Screen;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.ints.IntArraySet;

public abstract class GameInput implements InputProcessor, ControllerListener {
    protected static final float DEG_PER_PIXEL = 0.6384300433839F;
    private static final IntArraySet keys = new IntArraySet();
    protected final UltreonCraft game;
    protected final Camera camera;
    private final Set<Controller> controllers = new HashSet<>();
    private final Map<JoystickType, Joystick> joysticks = new EnumMap<>(JoystickType.class);

    public GameInput(UltreonCraft game, Camera camera) {
        this.game = game;
        this.camera = camera;

        for (JoystickType type : JoystickType.values()) {
            this.joysticks.put(type, new Joystick());
        }

        Controllers.addListener(this);
        this.controllers.addAll(Arrays.stream((Object[]) Controllers.getControllers().items).map(o -> (Controller) o).collect(Collectors.toList()));
    }

    @Override
    public void connected(Controller controller) {
        Debugger.log("Controller connected: " + controller.getName()); // Print the name of the connected controller
        this.controllers.add(controller);
    }

    @Override
    public void disconnected(Controller controller) {
        Debugger.log("Controller disconnected: " + controller.getName()); // Print the name of the disconnected controller
        this.controllers.remove(controller);
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        // Check if the absolute value of the value is less than the deadzone
        if (Math.abs(value) < Constants.CONTROLLER_DEADZONE) {
            value = 0; // Set the value to 0 if it's within the deadzone
        }

        ControllerMapping mapping = controller.getMapping();
        if (axisCode == mapping.axisLeftX) this.joysticks.get(JoystickType.LEFT).x = value;
        if (axisCode == mapping.axisLeftY) this.joysticks.get(JoystickType.LEFT).y = -value;

        if (axisCode == mapping.axisRightX) this.joysticks.get(JoystickType.RIGHT).x = value;
        if (axisCode == mapping.axisRightY) this.joysticks.get(JoystickType.RIGHT).y = -value;

        return true;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        System.out.println("Button down: " + buttonCode); // Print the button code of the button that was pressed
//        if (controller.getMapping().buttonX == buttonCode) {
//            controller.cancelVibration();
//            controller.startVibration(1000, 0.02F);
//        }
//        if (controller.getMapping().buttonA == buttonCode) {
//            controller.cancelVibration();
//            controller.startVibration(1000, 0.1F);
//        }
//        if (controller.getMapping().buttonB == buttonCode) {
//            controller.cancelVibration();
//            controller.startVibration(1000, 1.0F);
//        }

        Screen currentScreen = this.game.currentScreen;
        if (controller.getMapping().buttonA == buttonCode) {
            ControllerPowerLevel powerLevel = controller.getPowerLevel();
            System.out.println("powerLevel = " + powerLevel);

            Debugger.log("CONTROLLER INPUT -> Button A has been pressed.");
        }

        if (controller.getMapping().buttonB == buttonCode) {
            if (currentScreen != null) {
                currentScreen.back();
            }
        }

        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        keys.add(keycode);

        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        keys.remove(keycode);
        return true;
    }

    public static boolean isKeyDown(int keycode) {
        return keys.contains(keycode);
    }
    
    public void update() {
        this.update(Gdx.graphics.getDeltaTime());
    }

    public void update(float deltaTime) {

    }

    public boolean isControllerConnected() {
        return !this.controllers.isEmpty();
    }

    public Vector2 getJoystick(JoystickType joystick) {
        return this.joysticks.get(joystick).cpy();
    }
}
