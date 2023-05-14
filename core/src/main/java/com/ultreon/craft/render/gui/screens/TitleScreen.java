package com.ultreon.craft.render.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Matrix4;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.init.Fonts;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.widget.Button;
import com.ultreon.libs.translations.v0.Language;

import java.awt.*;

public class TitleScreen extends Screen {
    private Button startButton;
    private Button optionsButton;
    private Button quitButton;
    private final GlyphLayout layout = new GlyphLayout();

    public TitleScreen() {
        super("Title Screen");
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        startButton.setPos(width / 2 - 100, height - height / 3 + 5);
        optionsButton.setPos(width / 2 - 100, height - height / 3 - 25);
        quitButton.setPos(width / 2 + 5, height - height / 3 - 25);
    }

    @Override
    public void show() {
        clearWidgets();

        super.show();

        startButton = add(new Button(width / 2 - 100, height - height / 3 + 5, 200, Language.translate("craft/screen/title/start_world"), caller -> {
            UltreonCraft.get().startWorld();
        }));

        optionsButton = add(new Button(width / 2 - 100, height - height / 3 - 25, 95, Language.translate("craft/screen/title/options"), caller -> {
            UltreonCraft.get().showScreen(new LanguageScreen());
        }));
        quitButton = add(new Button(width / 2 + 5, height - height / 3 - 25, 95, Language.translate("craft/screen/title/quit"), caller -> {
            Gdx.app.exit();
        }));
        quitButton.setColor(Color.red);
        quitButton.setTextColor(Color.white);
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        Batch batch = renderer.getBatch();
        layout.setText(xlFont, "Ultreon Craft");
        xlFont.setColor(Color.white.darker().darker().toGdx());
        xlFont.draw(batch, "Ultreon Craft", (int)((float) width / 2 - layout.width / 2), (int)((float) (height - 40 - 3)));
        xlFont.setColor(Color.white.toGdx());
        xlFont.draw(batch, "Ultreon Craft", (int)((float) width / 2 - layout.width / 2), (int)((float) (height - 40)));
    }

    public Button getStartButton() {
        return startButton;
    }

    public Button getOptionsButton() {
        return optionsButton;
    }

    public Button getQuitButton() {
        return quitButton;
    }

    @Override
    public boolean canCloseOnEsc() {
        return false;
    }
}
