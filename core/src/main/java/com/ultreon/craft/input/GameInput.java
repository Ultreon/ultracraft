package com.ultreon.craft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.Ray;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.debug.Debugger;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.world.World;

import org.jetbrains.annotations.ApiStatus;

import it.unimi.dsi.fastutil.ints.IntArraySet;

public abstract class GameInput implements InputProcessor, ControllerListener {
    protected static final float DEG_PER_PIXEL = 0.6384300433839F;
    private static final IntArraySet keys = new IntArraySet();
    private static final float DEADZONE = 0.1F;
    protected final UltreonCraft game;
    protected final Camera camera;

    public GameInput(UltreonCraft game, Camera camera) {
        this.game = game;
        this.camera = camera;

        this.testController(); // TODO: Use actual code
    }

    @ApiStatus.Experimental
    public void testController() {
        Controller controller = Controllers.getControllers().first(); // Get the first connected controller

        if (controller != null) {
            System.out.println("Controller connected: " + controller.getName()); // Print the name of the connected controller
            controller.addListener(new ControllerAdapter() { // Add a listener to the controller for button/axis events
                @Override
                public boolean buttonDown(Controller controller, int buttonCode) {
                    System.out.println("Button down: " + buttonCode); // Print the button code of the button that was pressed
                    return true;
                }

                @Override
                public boolean axisMoved(Controller controller, int axisCode, float value) {
                    // Check if the absolute value of the value is less than the deadzone
                    if (Math.abs(value) < DEADZONE) {
                        value = 0; // Set the value to 0 if it's within the deadzone
                    }

                    System.out.println("Axis moved: " + axisCode + " to value " + value); // Print the Axis code and value that was moved
                    return true;
                }
            });
        } else {
            System.out.println("No controller connected."); // Print a message if no controller is connected
        }
    }

    @Override
    public void connected(Controller controller) {

    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        keys.add(keycode);

        if (keycode == Input.Keys.BUTTON_A) {
            Debugger.log("CONTROLLER INPUT -> Button A has been pressed.");
        }

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
}
