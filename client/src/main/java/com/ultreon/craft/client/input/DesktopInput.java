package com.ultreon.craft.client.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.events.ScreenEvents;
import com.ultreon.craft.client.gui.screens.PauseScreen;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.screens.container.InventoryScreen;
import com.ultreon.craft.client.imgui.ImGuiOverlay;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.events.ItemEvents;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.UseItemContext;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.ultreon.libs.translations.v1.Language;

import java.util.stream.IntStream;

public class DesktopInput extends GameInput {
    public int pauseKey = Input.Keys.ESCAPE;
    public int imGuiKey = Input.Keys.F9;
    public int imGuiFocusKey = Input.Keys.F10;
    public int debugHudKey = Input.Keys.F3;
    public int screenshotKey = Input.Keys.F2;
    public int inventoryKey = Input.Keys.E;
    public int fullScreenKey = Input.Keys.F11;
    public int thirdPersonKey = Input.Keys.F5;

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
        if (!caught) {
            Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        super.keyDown(keyCode);

        Screen currentScreen = this.client.screen;
        if (currentScreen != null && !Gdx.input.isCursorCatched()) {
            boolean flag = currentScreen.keyPress(keyCode);
            if (flag) return true;
        }

        if (keyCode == this.screenshotKey) {
            this.client.screenshot();
        }

        if (keyCode == this.fullScreenKey) {
            this.client.setFullScreen(!this.client.isFullScreen());
        }

        if (keyCode == this.thirdPersonKey) {
            this.client.setInThirdPerson(!this.client.isInThirdPerson());
        }

        if (Gdx.input.isCursorCatched()) {
            if (keyCode == this.pauseKey && GameInput.isKeyDown(this.pauseKey)) {
                UltracraftClient.invoke(() -> this.client.showScreen(new PauseScreen()));
                return true;
            }

            Player player = this.client.player;
            if (player != null) {
                if (keyCode >= Input.Keys.NUM_1 && keyCode <= Input.Keys.NUM_9) {
                    int index = keyCode - Input.Keys.NUM_1;
                    player.selectBlock(index);
                }
            }
        }

        return true;
    }

    @Override
    public boolean keyUp(int keyCode) {
        super.keyUp(keyCode);

        Player player = this.client.player;

        Screen currentScreen = this.client.screen;
        if (keyCode == this.imGuiKey) {
            if (this.client.isShowingImGui() && this.client.world != null) {
                DesktopInput.setCursorCaught(true);
            }
            this.client.setShowingImGui(!this.client.isShowingImGui());
        } else if (keyCode == this.imGuiFocusKey) {
            if (this.client.isShowingImGui() && this.client.world != null && this.client.screen == null) {
                DesktopInput.setCursorCaught(!Gdx.input.isCursorCatched());
            }
        } else if (keyCode == this.debugHudKey) {
            this.client.showDebugHud = !this.client.showDebugHud;
        } else if (currentScreen == null && player != null) {
            if (keyCode == this.inventoryKey && this.client.showScreen(new InventoryScreen(player.inventory, Language.translate("ultracraft.screen.inventory")))) {
                return true;
            }
        }

        if (currentScreen != null) currentScreen.keyRelease(keyCode);
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        Screen currentScreen = this.client.screen;
        if (currentScreen != null) currentScreen.charType(character);

        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        if (!Gdx.input.isCursorCatched()) {
            Screen currentScreen = this.client.screen;
            if (currentScreen != null)
                currentScreen.mouseMove((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()));
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        if (!Gdx.input.isCursorCatched()) {
            Screen currentScreen = this.client.screen;
            if (currentScreen != null) currentScreen.mouseMove(screenX, screenY);
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        World world = this.client.world;
        if (Gdx.input.isCursorCatched()) {
            if (world != null && this.client.screen == null) {
                if (!Gdx.input.isCursorCatched() && !UltracraftClient.get().isShowingImGui()) {
                    DesktopInput.setCursorCaught(true);
                    return true;
                }

                Player player = this.client.player;
                HitResult hitResult = this.client.hitResult;
                if (player != null && hitResult != null) {
                    Vec3i pos = hitResult.getPos();
                    Block block = world.get(new BlockPos(pos));
                    Vec3i posNext = hitResult.getNext();
                    Block blockNext = world.get(new BlockPos(posNext));
                    if (hitResult.isCollide() && block != null && !block.isAir()) {
                        if (button == Input.Buttons.LEFT) {
                            this.client.startBreaking();
                        } else if (button == Input.Buttons.RIGHT && blockNext != null && blockNext.isAir()) {
                            ItemStack stack = player.getSelectedItem();
                            UseItemContext context = new UseItemContext(world, player, hitResult, stack);
                            Item item = stack.getItem();
                            ItemEvents.USE.factory().onUseItem(item, context);
                            item.use(context);
                        }
                    }
                }
            }
        } else {
            Screen currentScreen = this.client.screen;
            if (currentScreen != null) {
                if (!ScreenEvents.MOUSE_PRESS.factory().onMousePressScreen((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button).isCanceled()) {
                    currentScreen.mousePress((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button);
                }
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        screenX -= this.client.getDrawOffset().x;
        screenY -= this.client.getDrawOffset().y;

        if (!Gdx.input.isCursorCatched()) {
            Screen currentScreen = this.client.screen;
            if (currentScreen != null) {
                if (!ScreenEvents.MOUSE_RELEASE.factory().onMouseReleaseScreen((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button).isCanceled()) {
                    currentScreen.mouseRelease((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button);
                }

                if (!ScreenEvents.MOUSE_CLICK.factory().onMouseClickScreen((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button, 1).isCanceled()) {
                    currentScreen.mouseClick((int) (screenX / this.client.getGuiScale()), (int) (screenY / this.client.getGuiScale()), button, 1);
                }
            }
        }
        this.client.stopBreaking();
        return false;
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
            if (i < 0) {
                i += 9;
            }
            player.selected = i;
            return true;
        } else if (currentScreen != null && !ScreenEvents.MOUSE_WHEEL.factory().onMouseWheelScreen((int) (Gdx.input.getX() / this.client.getGuiScale()), (int) (Gdx.input.getY() / this.client.getGuiScale()), amountY).isCanceled()) {
            return currentScreen.mouseWheel((int) (Gdx.input.getX() / this.client.getGuiScale()), (int) (Gdx.input.getY() / this.client.getGuiScale()), amountY);
        }

        return false;
    }
}
