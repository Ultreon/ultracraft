package com.ultreon.craft.render.gui.screens.world;

import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.Callback;
import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.craft.render.gui.widget.Button;
import com.ultreon.craft.text.CommonTexts;

public class ConfirmationScreen extends Screen {
    private final String description;
    private final Callback<ConfirmationScreen> accept;
    private Button yesButton;
    private Button noButton;

    public ConfirmationScreen(String title, String description, Callback<ConfirmationScreen> accept) {
        super(title);
        this.description = description;
        this.accept = accept;
    }

    @Override
    public void show() {
        super.show();

        this.yesButton = new Button(this.width / 2 + 5, this.height - 160, 95, CommonTexts.yes(), caller -> this.accept.call(this));

        this.noButton = new Button(this.width / 2 - 100, this.height - 160, 95, CommonTexts.yes(), caller -> this.back());
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        renderer.drawCenteredTextScaled(this.title, 2.0F, this.width / 2, this.height - 80, Color.rgb(0xff404040));
        renderer.drawCenteredText(this.title, this.width / 2, this.height - 120, Color.rgb(0xff404040));
    }

    public Button getYesButton() {
        return this.yesButton;
    }

    public Button getNoButton() {
        return this.noButton;
    }
}
