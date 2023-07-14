package com.ultreon.craft.render.gui.screens.world;

import com.ultreon.craft.datastorage.SaveManager;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.craft.render.gui.widget.Button;
import com.ultreon.craft.render.gui.widget.EditBox;
import com.ultreon.craft.text.CommonTexts;
import com.ultreon.craft.util.Seeder;
import com.ultreon.libs.translations.v1.Language;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CreateWorldScreen extends Screen {
    private EditBox worldName;
    private EditBox seed;
    private Button createButton;
    private Button cancelButton;
    private static final List<String> illegalNames = Arrays.asList("nul", "aux", "con");

    public CreateWorldScreen() {
        super(Language.translate("craft.screen.world_create"));
    }

    @Override
    public void show() {
        super.show();

        this.worldName = this.add(new EditBox(this.width / 2 - 100, this.height - 120, 200, Language.translate("craft.screen.world_create.world_name")));
        this.worldName.setValidator(query -> !illegalNames.contains(query) && !query.startsWith("$") && SaveManager.names().contains(query)
                && !query.contains("|") && !query.contains("?") && !query.contains(":") && !query.contains("&")
                && !query.contains("/") && !query.contains("\\") && !query.contains(";") && !query.contains(".")
        );
        this.seed = this.add(new EditBox(this.width / 2 - 100, this.height - 150, 200, Language.translate("craft.screen.world_create.world_name")));
        this.createButton = this.add(new Button(this.width / 2 - 100, this.height - 180, 95, Language.translate("craft.screen.world_create.create"), this::create));
        this.cancelButton = this.add(new Button(this.width / 2 + 5, this.height - 180, 95, CommonTexts.cancel(), this::cancel));
    }

    private void create(Button button) {
        if (this.seed.isError()) return;

        String seedText = this.seed.getText();

        long seed;
        try {
            seed = Long.parseLong(seedText);
        } catch (NumberFormatException e) {
            seed = Seeder.hash(seedText.toCharArray());
        }

        try {
            this.game.createWorld(this.getWorldName(), seed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cancel(Button caller) {
        this.back();
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        renderer.drawCenteredTextScaled(this.title, 2.0F, this.width / 2, this.height - 40);
    }

    public String getWorldName() {
        return this.worldName.getText();
    }

    public void setWorldName(String text) {
        this.worldName.setText(text);
    }

    public Button getCreateButton() {
        return this.createButton;
    }

    public Button getCancelButton() {
        return this.cancelButton;
    }
}
