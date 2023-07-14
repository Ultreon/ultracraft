package com.ultreon.craft.render.gui.screens;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.widget.Button;
import com.ultreon.craft.render.gui.widget.SelectionList;
import com.ultreon.libs.translations.v1.Language;
import com.ultreon.libs.translations.v1.LanguageManager;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class LanguageScreen extends Screen {
    private Button backButton;
    private SelectionList<Locale> list;
    private final GlyphLayout layout = new GlyphLayout();

    public LanguageScreen() {
        super("Language");
    }

    @Override
    public void show() {
        super.show();

        this.list = this.add(new SelectionList<>(0, 40, this.width, this.height - 40, 14));
        this.list.setSelectable(true);
        this.list.setItemRenderer(this::renderItem);
        this.list.setOnSelected(locale -> {
            this.game.settings.language.set(locale);
            this.game.settings.save();
            this.updateTexts();
        });

        List<Locale> locales = LanguageManager.INSTANCE.getLocales().stream().sorted((o1, o2) -> o1.getDisplayLanguage().compareToIgnoreCase(o2.getDisplayLanguage())).collect(Collectors.toList());
        this.list.addEntries(locales);

        this.backButton = this.add(new Button(this.width / 2 - 100, 10, 200, Language.translate("craft.screen.language.back"), caller -> {
            if (this.game.world != null) {
                this.game.showScreen(new PauseScreen());
            } else {
                this.game.showScreen(null);
            }
        }));
        this.backButton.setColor(Color.rgb(0xff0000));
    }

    private void renderItem(Renderer renderer, Locale locale, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        String text = locale.getDisplayLanguage(new Locale("en")) + " (" + locale.getDisplayCountry(new Locale("en")) + ")";
        text += " - " + locale.getDisplayLanguage(locale) + " (" + locale.getDisplayCountry(locale) + ")";
        renderer.drawCenteredText(text, this.width / 2, 11f);
    }

    private void updateTexts() {
        this.backButton.setMessage(Language.translate("craft.screen.language.back"));
    }

    public Button getBackButton() {
        return this.backButton;
    }
}
