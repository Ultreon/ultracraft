package com.ultreon.craft.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.GridPoint2;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.render.Hud;
import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.craft.world.World;

import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;

public class MobileInput extends GameInput {
    private final Int2ReferenceMap<GridPoint2> origins = new Int2ReferenceArrayMap<>();
    private int rotatePointer = -1;
    private GridPoint2 rotateOrigin;

    public MobileInput(UltreonCraft game, GameCamera camera) {
        super(game, camera);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.BACK) {
            Screen currentScreen = this.game.currentScreen;
            if (currentScreen != null) {
                currentScreen.back();
            } else if (this.game.isPlaying()) {
                this.game.pause();
            }
        }
        return super.keyDown(keycode);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        inGame: if (this.game.isPlaying()) {
            Hud hud = this.game.hud;
            if (hud != null) {
                if (hud.touchDragged(screenX, screenY, pointer)) {
                    break inGame;
                }
            }

            Player player = this.game.player;
            if (player != null && this.rotatePointer == pointer) {
                this.updatePlayerMovement(player, screenX, screenY, pointer);
            }
        }
        return true;
    }

    private void updatePlayerMovement(Player player, int screenX, int screenY, int pointer) {

    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        World world = this.game.world;

        if (this.game.isPlaying() && world != null) {
            Hud hud = this.game.hud;
            if (hud != null) {
                if (hud.touchDown(screenX, screenY, pointer)) {
                    return true;
                }
            }

            if (this.rotatePointer == -1) {
                this.rotateOrigin = new GridPoint2(screenX, screenY);
                this.rotatePointer = pointer;
                return true;
            }

            // TODO: Implement block breaking.
//            Player player = this.game.player;
//            if (player != null) {
//                HitResult hitResult = world.rayCast(new Ray(player.getPosition().add(0, player.getEyeHeight(), 0), player.getLookVector()));
//                Vec3i pos = hitResult.pos;
//                Block block = world.get(pos);
//                Vec3i posNext = hitResult.next;
//                Block blockNext = world.get(posNext);
//                Block selectedBlock = this.game.player.getSelectedBlock();
//                if (hitResult.collide && block != null && !block.isAir()) {
//                    if (button == Input.Buttons.LEFT) {
//                        world.set(pos, Blocks.AIR);
//                    } else if (button == Input.Buttons.RIGHT && blockNext != null && blockNext.isAir()
//                            && !selectedBlock.getBoundingBox(posNext).intersects(this.game.player.getBoundingBox())) {
//                        world.set(posNext, selectedBlock);
//                    }
//                }
//            }
        } else {
            Screen currentScreen = this.game.currentScreen;
            return currentScreen != null && currentScreen.mousePress((int) (screenX / this.game.getGuiScale()), (int) (screenY / this.game.getGuiScale()), button);
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (this.game.isPlaying()) {
            if (this.rotatePointer == pointer) {
                this.rotatePointer = -1;
                this.rotateOrigin = null;
                return true;
            }

            Hud hud = this.game.hud;
            if (hud != null) {
                hud.touchUp(screenX, screenY, pointer);
            }
        } else {
            Screen currentScreen = this.game.currentScreen;
            if (currentScreen != null) {
                currentScreen.mouseRelease((int) (screenX / this.game.getGuiScale()), (int) (screenY / this.game.getGuiScale()), button);
                currentScreen.mouseClick((int) (screenX / this.game.getGuiScale()), (int) (screenY / this.game.getGuiScale()), button, 1);
            }
        }
        return false;
    }

}
