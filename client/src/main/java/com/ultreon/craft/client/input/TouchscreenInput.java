package com.ultreon.craft.client.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.events.GuiEvents;
import com.ultreon.craft.client.events.ScreenEvents;
import com.ultreon.craft.client.gui.screens.PauseScreen;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class TouchscreenInput extends GameInput {
    public static final KeyBind PAUSE_KEY = KeyBinds.pauseKey;
    public static final KeyBind IM_GUI_KEY = KeyBinds.imGuiKey;
    public static final KeyBind IM_GUI_FOCUS_KEY = KeyBinds.imGuiFocusKey;
    public static final KeyBind DEBUG_KEY = KeyBinds.debugKey;
    public static final KeyBind INSPECT_KEY = KeyBinds.inspectKey;
    public static final KeyBind HIDE_HUD_KEY = KeyBinds.hideHudKey;
    public static final KeyBind SCREENSHOT_KEY = KeyBinds.screenshotKey;
    public static final KeyBind INVENTORY_KEY = KeyBinds.inventoryKey;
    public static final KeyBind CHAT_KEY = KeyBinds.chatKey;
    public static final KeyBind COMMAND_KEY = KeyBinds.commandKey;
    public static final KeyBind FULL_SCREEN_KEY = KeyBinds.fullScreenKey;
    public static final KeyBind THIRD_PERSON_KEY = KeyBinds.thirdPersonKey;

    public TouchscreenInput(UltracraftClient client, Camera camera) {
        super(client, camera);
    }

    public static Vector2 getMouseDelta() {
        return new Vector2(Gdx.input.getDeltaX(), Gdx.input.getDeltaY());
    }

    public static boolean isPressingAnyButton() {
        return IntStream.rangeClosed(0, Input.Buttons.FORWARD).anyMatch(i -> Gdx.input.isButtonPressed(i));
    }

    public static void setCursorCaught(boolean caught) {
        if (Gdx.input.isCursorCatched() == caught) return;

        Gdx.input.setCursorCatched(caught);
        if (!caught) {
            Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        super.keyDown(keyCode);

        Screen currentScreen = this.client.screen;
        if (currentScreen != null && !Gdx.input.isCursorCatched() && currentScreen.keyPress(keyCode))
            return true;

        Player player = this.client.player;
        if (player == null || keyCode < Input.Keys.NUM_1 || keyCode > Input.Keys.NUM_9 || !Gdx.input.isCursorCatched())
            return false;

        int index = keyCode - Input.Keys.NUM_1;
        player.selectBlock(index);
        return true;

    }

    @Override
    public boolean keyUp(int keyCode) {
        super.keyUp(keyCode);

        Screen currentScreen = this.client.screen;
        if (currentScreen != null)
            return currentScreen.keyRelease(keyCode);

        return false;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (TouchscreenInput.PAUSE_KEY.isJustPressed() && Gdx.input.isCursorCatched()) {
            this.client.showScreen(new PauseScreen());
        } else if (TouchscreenInput.PAUSE_KEY.isJustPressed() && !Gdx.input.isCursorCatched()) {
            this.client.showScreen(null);
        }

        LocalPlayer player = this.client.player;
        if (this.client.screen == null && player != null && Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen) && client.motionPointer != null) {
            float deltaX = Gdx.input.getDeltaX(client.motionPointer.pointer());
            float deltaY = Gdx.input.getDeltaY(client.motionPointer.pointer());

            if (deltaX != 0 || deltaY != 0) {
                player.rotate(deltaX, deltaY);
            }
        }

        if (client.motionPointer.pos().dst(getPos(client.motionPointer.pointer())) > 10 * UltracraftClient.get().getGuiScale()) {
            this.resetBreaking();
        }
    }

    private Vec2i getPos(int pointer) {
        return new Vec2i(Gdx.input.getX(pointer), Gdx.input.getY(pointer));
    }

    @Override
    public boolean keyTyped(char character) {
        Screen currentScreen = this.client.screen;
        if (currentScreen != null)
            return currentScreen.charType(character);

        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        if (Gdx.input.isCursorCatched())
            return false;

        Screen currentScreen = this.client.screen;

        if (currentScreen == null)
            return false;

        currentScreen.mouseMove((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()));
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        if (!Gdx.input.isCursorCatched()) {
            Screen currentScreen = this.client.screen;
            if (currentScreen != null) currentScreen.mouseDrag(
                    (int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()),
                    (int) (Gdx.input.getDeltaX(pointer) / this.client.getGuiScale()), (int) (Gdx.input.getDeltaY(pointer) / this.client.getGuiScale()), pointer);
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        Screen currentScreen = this.client.screen;
        World world = this.client.world;
        Player player = this.client.player;
        if (!Gdx.input.isCursorCatched() && currentScreen != null) {
            int mouseX = (int) (screenX / this.client.getGuiScale());
            int mouseY = (int) (screenY / this.client.getGuiScale());
            boolean canceled = GuiEvents.MOUSE_PRESS.factory().onMousePressScreen(mouseX, mouseY, button).isCanceled();
            boolean pressed = currentScreen.mousePress(mouseX, mouseY, button);
            return !canceled && pressed;
        }

        if (world == null || this.client.screen != null)
            return false;

        if (player == null)
            return false;

        int mouseX = (int) (screenX / this.client.getGuiScale());
        int mouseY = (int) (screenY / this.client.getGuiScale());
        if (this.client.hud.isMouseOver(mouseX, mouseY)) {
            return this.client.hud.touchDown(mouseX, mouseY, pointer, button);
        }

        if (this.client.screen == null && Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen) && this.client.motionPointer == null) {
            this.client.motionPointer = new TouchPoint(screenX, screenY, pointer, button);
        }

        this.hitResult = client.hitResult;

        return this.doPlayerInteraction(button, hitResult, world, player);
    }

    private boolean doPlayerInteraction(int button, HitResult hitResult, World world, Player player) {
        Vec3i pos = hitResult.getPos();
        Block block = world.get(new BlockPos(pos));
        Vec3i posNext = hitResult.getNext();
        Block blockNext = world.get(new BlockPos(posNext));

        if (!hitResult.isCollide() || block == null || block.isAir())
            return false;

        if (button == Input.Buttons.RIGHT && blockNext != null && blockNext.isAir()) {
            this.useItem(player, world, hitResult);
            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        this.client.stopBreaking();

        LocalPlayer player = this.client.player;
        TouchPoint motionPoint = this.client.motionPointer;
        if (this.client.screen == null && player != null && Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen) && motionPoint != null && motionPoint.pointer() == pointer) {
            this.client.motionPointer = null;
            return true;
        }

        Screen currentScreen = this.client.screen;
        if (currentScreen == null) {
            @Nullable TouchPoint motionPointer = this.client.motionPointer;
            if (motionPointer != null && motionPointer.pointer() == pointer) {
                this.client.motionPointer = null;
                return true;
            } else {
                int mouseX = (int) (screenX / this.client.getGuiScale());
                int mouseY = (int) (screenY / this.client.getGuiScale());
                return this.client.hud.touchUp(mouseX, mouseY, pointer, button);
            }
        }

        boolean flag = false;
        if (!GuiEvents.MOUSE_RELEASE.factory().onMouseReleaseScreen((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button).isCanceled())
            flag |= currentScreen.mouseRelease((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button);

        if (!GuiEvents.MOUSE_CLICK.factory().onMouseClickScreen((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button, 1).isCanceled())
            flag |= currentScreen.mouseClick((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button, 1);

        return flag;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        Screen currentScreen = this.client.screen;

        if (GamePlatform.get().isShowingImGui()) return false;

        Player player = this.client.player;
        if (currentScreen == null && player != null) {
            int scrollAmount = (int) amountY;
            int i = (player.selected + scrollAmount) % 9;

            if (i < 0)
                i += 9;

            player.selected = i;
            return true;
        }

        if (currentScreen != null && !GuiEvents.MOUSE_WHEEL.factory().onMouseWheelScreen((int) (Gdx.input.getX() / this.client.getGuiScale()), (int) (Gdx.input.getY() / this.client.getGuiScale()), amountY).isCanceled())
            return currentScreen.mouseWheel((int) (Gdx.input.getX() / this.client.getGuiScale()), (int) (Gdx.input.getY() / this.client.getGuiScale()), amountY);

        return false;
    }
}
