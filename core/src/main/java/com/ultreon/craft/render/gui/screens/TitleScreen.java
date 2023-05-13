package com.ultreon.craft.render.gui.screens;

import com.badlogic.gdx.Gdx;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.gui.widget.Button;

public class TitleScreen extends Screen {
    private Button startButton;
    private Button quitButton;

    public TitleScreen() {
        super("Title Screen");
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        startButton.setPos(width / 2 - 100, height - height / 3 + 5);
        quitButton.setPos(width / 2 - 100, height - height / 3 - 25);
    }

    @Override
    public void show() {
        clearWidgets();

        super.show();

        startButton = add(new Button(width / 2 - 100, height - height / 3 + 5, 200, "Start New World", caller -> {
            UltreonCraft.get().startWorld();
        }));

        quitButton = add(new Button(width / 2 - 100, height - height / 3 - 25, 200, "Quit", caller -> {
            Gdx.app.exit();
        }));
        quitButton.setColor(Color.red);
        quitButton.setTextColor(Color.white);
    }

    public Button getStartButton() {
        return startButton;
    }

    public Button getQuitButton() {
        return quitButton;
    }

    @Override
    public boolean canCloseOnEsc() {
        return false;
    }
}
