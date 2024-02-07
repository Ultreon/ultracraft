package com.ultreon.craft.client.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.events.ScreenEvents;
import com.ultreon.craft.client.gui.screens.ChatScreen;
import com.ultreon.craft.client.gui.screens.PauseScreen;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.screens.container.InventoryScreen;
import com.ultreon.craft.client.imgui.ImGuiOverlay;
import com.ultreon.craft.client.input.key.KeyBind;
import com.ultreon.craft.client.input.key.KeyBinds;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.debug.DebugFlags;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.network.packets.c2s.C2SBlockBreakPacket;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3i;

import java.util.stream.IntStream;

public class DesktopInput extends GameInput {
    public static final KeyBind PAUSE_KEY = KeyBinds.pauseKey;
    public static final KeyBind DROP_ITEM_KEY = KeyBinds.dropItemKey;
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

    public DesktopInput(UltracraftClient client, Camera camera) {
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

        Player player = this.client.player;
        Screen currentScreen = this.client.screen;

        if (DesktopInput.IM_GUI_KEY.isJustPressed()) {
            this.handleImGuiKey();
        } else if (DesktopInput.IM_GUI_FOCUS_KEY.isJustPressed()) {
            this.handleImGuiFocus();
        } else if (DesktopInput.INVENTORY_KEY.isJustPressed() && currentScreen == null && player != null) {
            player.openInventory();
        } else if (DesktopInput.INVENTORY_KEY.isJustPressed() && currentScreen instanceof InventoryScreen && player != null) {
            this.client.showScreen(null);
        } else if (DesktopInput.CHAT_KEY.isJustPressed() && currentScreen == null) {
            this.client.showScreen(new ChatScreen());
        } else if (DesktopInput.COMMAND_KEY.isJustPressed() && currentScreen == null) {
            this.client.showScreen(new ChatScreen("/"));
        } else if (DesktopInput.DEBUG_KEY.isJustPressed()) {
            this.handleDebugKey();
        } else if (DesktopInput.INSPECT_KEY.isJustPressed()) {
            this.handleInspectKey();
        } else if (DesktopInput.SCREENSHOT_KEY.isJustPressed()) {
            this.client.screenshot();
        } else if (DesktopInput.HIDE_HUD_KEY.isJustPressed()) {
            this.client.hideHud = !this.client.hideHud;
        } else if (DesktopInput.FULL_SCREEN_KEY.isJustPressed()) {
            this.client.setFullScreen(!this.client.isFullScreen());
        } else if (DesktopInput.THIRD_PERSON_KEY.isJustPressed()) {
            this.client.setInThirdPerson(!this.client.isInThirdPerson());
        } else if (this.client.world != null && DesktopInput.PAUSE_KEY.isJustPressed() && Gdx.input.isCursorCatched()) {
            this.client.showScreen(new PauseScreen());
        } else if (DesktopInput.PAUSE_KEY.isJustPressed() && !Gdx.input.isCursorCatched() && this.client.screen instanceof PauseScreen) {
            this.client.showScreen(null);
        } else if (DesktopInput.DROP_ITEM_KEY.isJustPressed()) {
            player.dropItem();
        }
    }

    private void handleImGuiKey() {
        if (!UltracraftClient.get().config.get().debugUtils) return;

        if (this.client.isShowingImGui() && this.client.world != null)
            DesktopInput.setCursorCaught(true);

        this.client.setShowingImGui(!this.client.isShowingImGui());
    }

    private void handleInspectKey() {
        if (UltracraftClient.get().config.get().debugUtils && DebugFlags.INSPECTION_ENABLED.enabled()) {
            this.client.inspection.setInspecting(!this.client.inspection.isInspecting());
        }
    }

    private void handleDebugKey() {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) this.client.debugGui.prevPage();
        else this.client.debugGui.nextPage();

        if (!this.client.showDebugHud)
            UltracraftClient.PROFILER.setProfiling(false);
        else if (this.client.config.get().debugUtils && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
            UltracraftClient.PROFILER.setProfiling(true);
    }

    private void handleImGuiFocus() {
        if (this.client.isShowingImGui() && this.client.world != null && this.client.screen == null) {
            DesktopInput.setCursorCaught(!Gdx.input.isCursorCatched());
        }
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
        HitResult hitResult = this.client.hitResult;

        if (!Gdx.input.isCursorCatched() && currentScreen != null) {
            int mouseX = (int) (screenX / this.client.getGuiScale());
            int mouseY = (int) (screenY / this.client.getGuiScale());
            boolean canceled = ScreenEvents.MOUSE_PRESS.factory().onMousePressScreen(mouseX, mouseY, button).isCanceled();
            boolean pressed = currentScreen.mousePress(mouseX, mouseY, button);
            return !canceled && pressed;
        }

        if (world == null || this.client.screen != null)
            return false;

        if (!Gdx.input.isCursorCatched() && !UltracraftClient.get().isShowingImGui()) {
            return true;
        }

        if (player == null || hitResult == null)
            return false;

        return this.doPlayerInteraction(button, hitResult, world, player);
    }

    private boolean doPlayerInteraction(int button, HitResult hitResult, World world, Player player) {
        Vec3i pos = hitResult.getPos();
        Block block = world.get(new BlockPos(pos));
        Vec3i posNext = hitResult.getNext();
        Block blockNext = world.get(new BlockPos(posNext));

        if (!hitResult.isCollide() || block == null || block.isAir())
            return false;

        if (button == Input.Buttons.LEFT) {
            if (player.abilities.instaMine) {
                this.client.connection.send(new C2SBlockBreakPacket(new BlockPos(hitResult.getPos())));
                return true;
            }
            this.client.startBreaking();
            return true;
        }

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

        if (Gdx.input.isCursorCatched())
            return false;

        Screen currentScreen = this.client.screen;
        if (currentScreen == null)
            return false;

        boolean flag = false;
        if (!ScreenEvents.MOUSE_RELEASE.factory().onMouseReleaseScreen((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button).isCanceled())
            flag |= currentScreen.mouseRelease((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button);

        if (!ScreenEvents.MOUSE_CLICK.factory().onMouseClickScreen((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button, 1).isCanceled())
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

        if (ImGuiOverlay.isShown()) return false;

        Player player = this.client.player;
        if (currentScreen == null && player != null) {
            int scrollAmount = (int) amountY;
            int i = (player.selected + scrollAmount) % 9;

            if (i < 0)
                i += 9;

            player.selected = i;
            return true;
        }

        if (currentScreen != null && !ScreenEvents.MOUSE_WHEEL.factory().onMouseWheelScreen((int) (Gdx.input.getX() / this.client.getGuiScale()), (int) (Gdx.input.getY() / this.client.getGuiScale()), amountY).isCanceled())
            return currentScreen.mouseWheel((int) (Gdx.input.getX() / this.client.getGuiScale()), (int) (Gdx.input.getY() / this.client.getGuiScale()), amountY);

        return false;
    }
}
