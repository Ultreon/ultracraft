package com.ultreon.craft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.Ray;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.render.gui.screens.PauseScreen;
import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.world.World;
import it.unimi.dsi.fastutil.ints.IntArraySet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.Deflater;

public class InputManager extends InputAdapter {
    private static final float DEG_PER_PIXEL = 0.6384300433839F;
    public int pauseKey = Input.Keys.ESCAPE;
    public int imGuiKey = Input.Keys.F3;
    public int imGuiFocusKey = Input.Keys.F4;
    public int screenshotKey = Input.Keys.F2;
    private static final IntArraySet keys = new IntArraySet();
    private final UltreonCraft game;
    private int xPos;
    private int yPos;
    private int deltaX;
    private int deltaY;
    private boolean isCaptured;
    private boolean wasCaptured;

    public InputManager(UltreonCraft game, Camera camera) {
        this.game = game;
    }

    @Override
    public boolean keyDown(int keycode) {
        keys.add(keycode);

        Screen currentScreen = game.currentScreen;
        if (currentScreen != null && !Gdx.input.isCursorCatched()) {
            boolean flag = currentScreen.keyPress(keycode);
            if (flag) return true;
        }

        if (keycode == screenshotKey) {
            Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            PixmapIO.writePNG(Gdx.files.local("screenshots/screenshot_%s.png".formatted(DateTimeFormatter.ofPattern("MM.dd.yyyy-HH.mm.ss").format(LocalDateTime.now()))), pixmap, Deflater.DEFAULT_COMPRESSION, true);
            pixmap.dispose();
        }

        if (Gdx.input.isCursorCatched()) {
            if (isKeyDown(pauseKey) && Gdx.input.isCursorCatched()) {
                game.showScreen(new PauseScreen());
                return true;
            }

            Player player = game.player;
            if (player != null) {
                if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_9) {
                    int index = keycode - Input.Keys.NUM_1;
                    player.selectBlock(index);
                }
            }
        }

        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        keys.remove(keycode);
        if (imGuiKey == keycode) {
            if (game.isShowingImGui() && game.world != null) {
                Gdx.input.setCursorCatched(true);
            }
            game.setShowingImGui(!game.isShowingImGui());
        }
        if (imGuiFocusKey == keycode && game.isShowingImGui() && game.world != null && game.currentScreen == null) {
            Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched());
        }

        Screen currentScreen = game.currentScreen;
        if (currentScreen != null) currentScreen.keyRelease(keycode);
        return true;
    }

    public static boolean isKeyDown(int keycode) {
        return keys.contains(keycode);
    }
    
    public void update() {
        update(Gdx.graphics.getDeltaTime());
    }

    private void updatePlayerMovement(int screenX, int screenY) {
        if (this.game.player == null) return;

        if (isCaptured && !wasCaptured) {
            this.deltaX = 0;
            this.deltaY = 0;
        } else if (isCaptured) {
            this.deltaX = xPos - screenX;
            this.deltaY = yPos - screenY;
        }

        this.xPos = screenX;
        this.yPos = screenY;

        Vector2 rotation = game.player.getRotation();
        rotation.add(deltaX * DEG_PER_PIXEL, deltaY * DEG_PER_PIXEL);
        game.player.setRotation(rotation);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        this.wasCaptured = isCaptured;
        this.isCaptured = Gdx.input.isCursorCatched();

        if (Gdx.input.isCursorCatched()) {
            updatePlayerMovement(screenX, screenY);
        } else {
            Screen currentScreen = game.currentScreen;
            if (currentScreen != null)
                currentScreen.mouseMove((int) (screenX / game.getGuiScale()), (int) (screenY / game.getGuiScale()));
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        this.wasCaptured = isCaptured;
        this.isCaptured = Gdx.input.isCursorCatched();

        if (Gdx.input.isCursorCatched()) {
            updatePlayerMovement(screenX, screenY);
        } else {
            screenY = game.getHeight() - screenY;
            Screen currentScreen = game.currentScreen;
            if (currentScreen != null) currentScreen.mouseMove(screenX, screenY);
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        World world = this.game.world;
        if (Gdx.input.isCursorCatched()) {
            if (world != null && this.game.currentScreen == null) {
                if (!Gdx.input.isCursorCatched() && !UltreonCraft.get().isShowingImGui()) {
                    Gdx.input.setCursorCatched(true);
                    return true;
                }

                Player player = this.game.player;
                if (player != null) {
                    HitResult hitResult = world.rayCast(new Ray(player.getPosition().add(0, player.getEyeHeight(), 0), player.getLookVector()));
                    GridPoint3 pos = hitResult.pos;
                    Block block = world.get(pos);
                    GridPoint3 posNext = hitResult.next;
                    Block blockNext = world.get(posNext);
                    Block selectedBlock = this.game.player.getSelectedBlock();
                    if (hitResult.collide && block != null && !block.isAir()) {
                        if (button == Input.Buttons.LEFT) {
                            world.set(pos, Blocks.AIR);
                        } else if (button == Input.Buttons.RIGHT && blockNext != null && blockNext.isAir()
                                && !selectedBlock.getBoundingBox(posNext).intersects(this.game.player.getBoundingBox())) {
                            world.set(posNext, selectedBlock);
                        }
                    }
                }
            }
        } else {
            screenY = game.getHeight() - screenY;
            Screen currentScreen = game.currentScreen;
            if (currentScreen != null)
                currentScreen.mousePress((int) (screenX / game.getGuiScale()), (int) (screenY / game.getGuiScale()), button);
        }
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (!Gdx.input.isCursorCatched()) {
            Screen currentScreen = game.currentScreen;
            screenY = game.getHeight() - screenY;
            if (currentScreen != null) {
                currentScreen.mouseRelease((int) (screenX / game.getGuiScale()), (int) (screenY / game.getGuiScale()), button);
                currentScreen.mouseClick((int) (screenX / game.getGuiScale()), (int) (screenY / game.getGuiScale()), button, 1);
            }
        }
        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        Screen currentScreen = game.currentScreen;

        Player player = game.player;
        if (player != null) {
            int scrollAmount = (int) amountY;
            int i = (player.selected + scrollAmount) % 9;
            if (i < 0) {
                i += 9;
            }
            player.selected = i;
        }

        var yPos = game.getHeight() - this.yPos;
        if (currentScreen != null) currentScreen.mouseWheel((int) (xPos / game.getGuiScale()), (int) (yPos / game.getGuiScale()), amountY);
        return super.scrolled(amountX, amountY);
    }

    public void update(float deltaTime) {

    }
}
