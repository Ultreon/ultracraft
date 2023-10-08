package com.ultreon.craft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Camera;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.util.Ray;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.Constants;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.debug.Debugger;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.input.util.ControllerButton;
import com.ultreon.craft.input.util.Joystick;
import com.ultreon.craft.input.util.JoystickType;
import com.ultreon.craft.input.util.Trigger;
import com.ultreon.craft.input.util.TriggerType;
import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.Mth;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.ints.Int2BooleanArrayMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;

public abstract class GameInput implements InputProcessor, ControllerListener {
    protected static final float DEG_PER_PIXEL = 0.6384300433839F;
    private static final IntArraySet keys = new IntArraySet();
    protected final UltreonCraft game;
    protected final Camera camera;
    private final Set<Controller> controllers = new HashSet<>();
    private static final Map<JoystickType, Joystick> JOYSTICKS = new EnumMap<>(JoystickType.class);
    private static final Map<TriggerType, Trigger> TRIGGERS = new EnumMap<>(TriggerType.class);
    private static final Int2BooleanMap CONTROLLER_BUTTONS = new Int2BooleanArrayMap();

    static {
        for (JoystickType type : JoystickType.values()) {
            JOYSTICKS.put(type, new Joystick());
        }
        for (TriggerType type : TriggerType.values()) {
            TRIGGERS.put(type, new Trigger());
        }
    }

    private long nextBreak;
    private long nextPlace;
    private final Vec3d vel = new Vec3d();

    public GameInput(UltreonCraft game, Camera camera) {
        this.game = game;
        this.camera = camera;

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
        // Check if the absolute value of the value is less than the dead zone
        if (Math.abs(value) < Constants.CONTROLLER_DEADZONE) {
            value = 0; // Set the value to 0 if it's within the dead zone
        }

        ControllerMapping mapping = controller.getMapping();
        if (axisCode == mapping.axisLeftX) JOYSTICKS.get(JoystickType.LEFT).x = -value;
        if (axisCode == mapping.axisLeftY) JOYSTICKS.get(JoystickType.LEFT).y = -value;

        if (axisCode == mapping.axisRightX) JOYSTICKS.get(JoystickType.RIGHT).x = -value;
        if (axisCode == mapping.axisRightY) JOYSTICKS.get(JoystickType.RIGHT).y = -value;

        if (axisCode == 4) TRIGGERS.get(TriggerType.LEFT).value = value;
        if (axisCode == 5) TRIGGERS.get(TriggerType.RIGHT).value = value;

        return true;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        CONTROLLER_BUTTONS.put(buttonCode, false);

        return false;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        Screen currentScreen = this.game.currentScreen;
        CONTROLLER_BUTTONS.put(buttonCode, true);

        if (this.game.isPlaying()) {
            Player player = this.game.player;
            if (player != null) {
                if (controller.getMapping().buttonL1 == buttonCode)
                    player.selectBlock(player.selected - 1);
                if (controller.getMapping().buttonR1 == buttonCode)
                    player.selectBlock(player.selected + 1);
            }
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
        if (this.game.isPlaying()) {
            Player player = this.game.player;
            if (player != null) {
                Joystick joystick = JOYSTICKS.get(JoystickType.RIGHT);

                float deltaX = joystick.x * deltaTime * Constants.CTRL_CAMERA_SPEED;
                float deltaY = joystick.y * deltaTime * Constants.CTRL_CAMERA_SPEED;

                Vector2 rotation = player.getRotation();
                rotation.add(deltaX * DEG_PER_PIXEL, deltaY * DEG_PER_PIXEL);
                player.setRotation(rotation);

                @Nullable World world = this.game.world;
                if (world != null) {
                    HitResult hitResult = world.rayCast(new Ray(player.getPosition().add(0, player.getEyeHeight(), 0), player.getLookVector()));
                    Vec3i pos = hitResult.pos;
                    Block block = world.get(pos);
                    Vec3i posNext = hitResult.next;
                    Block blockNext = world.get(posNext);
                    Block selectedBlock = this.game.player.getSelectedBlock();
                    if (hitResult.collide && !block.isAir()) {
                        if (TRIGGERS.get(TriggerType.RIGHT).value >= 0.3F && this.nextBreak < System.currentTimeMillis()) {
                            world.set(pos, Blocks.AIR);
                            System.out.println("Break Block");
                            this.nextBreak = System.currentTimeMillis() + 500;
                        } else if (TRIGGERS.get(TriggerType.LEFT).value >= 0.3F && this.nextPlace < System.currentTimeMillis() && blockNext.isAir() && !selectedBlock.getBoundingBox(posNext).intersectsExclusive(this.game.player.getBoundingBox())) {
                            world.set(posNext, selectedBlock);
                            System.out.println("Place Block");
                            this.nextPlace = System.currentTimeMillis() + 500;
                        }
                    }
                }

                player.setRunning(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isCursorCatched() || GameInput.isControllerButtonDown(ControllerButton.LEFT_STICK));

                if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                    boolean doNowFly = !player.isFlying();
                    player.noGravity = doNowFly;
                    player.setFlying(doNowFly);
                }

                if (!player.isFlying()) player.setCrouching(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || GameInput.isControllerButtonDown(ControllerButton.RIGHT_STICK));

                float speed;
                if (player.isFlying()) speed = player.getFlyingSpeed();
                else speed = player.getWalkingSpeed();

                if (player.isCrouching()) speed *= player.crouchModifier;
                else if (player.isRunning()) speed *= player.runModifier;

                if (!player.topView) {
                    Vec3d tmp = new Vec3d();
                    this.game.playerInput.tick(speed);
                    Vector3 velocity = this.game.playerInput.getVelocity();
                    vel.set(velocity.x, velocity.y, velocity.z);

                    if (player.isInWater() && this.game.playerInput.up) {
                        tmp.set(0, 1, 0).nor().mul(speed);
                        vel.add(tmp);
                    }
                    if (player.isFlying()) {
                        if (this.game.playerInput.up) {
                            tmp.set(0, 1, 0).nor().mul(speed);
                            vel.add(tmp);
                        }
                        if (this.game.playerInput.down) {
                            tmp.set(0, 1, 0).nor().mul(-speed);
                            vel.add(tmp);
                        }
                    }

                    vel.x *= deltaTime * UltreonCraft.TPS;
                    vel.y *= deltaTime * UltreonCraft.TPS;
                    vel.z *= deltaTime * UltreonCraft.TPS;

                    player.setVelocity(player.getVelocity().add(vel));
                } else {
                    player.setX(0);
                    player.setZ(0);
                    player.setY(120);
                    player.xRot = 45;
                    player.yRot =  -45;
                }
            }
        }
    }

    public boolean isControllerConnected() {
        return !this.controllers.isEmpty();
    }

    public static Vector2 getJoystick(JoystickType joystick) {
        return JOYSTICKS.get(joystick).cpy();
    }

    public static boolean isControllerButtonDown(ControllerButton button) {
        Controller current = Controllers.getCurrent();
        if (current == null) return false;
        ControllerMapping mapping = current.getMapping();
        return CONTROLLER_BUTTONS.get(button.get(mapping));
    }

    @CanIgnoreReturnValue
    public static boolean cancelVibration() {
        Controller current = Controllers.getCurrent();
        if (current == null) return false;
        current.cancelVibration();
        return true;
    }

    @CanIgnoreReturnValue
    public static boolean startVibration(int duration, float strength) {
        Controller current = Controllers.getCurrent();
        if (current == null) return false;
        current.startVibration(duration, Mth.clamp(strength, 0.0F, 1.0F));
        return true;
    }
}
