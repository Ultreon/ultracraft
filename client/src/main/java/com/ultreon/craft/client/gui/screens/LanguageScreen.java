package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.config.Config;
import com.ultreon.craft.client.gui.*;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.SelectionList;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.client.text.LanguageManager;
import com.ultreon.craft.client.text.UITranslations;
import com.ultreon.craft.text.TextObject;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LanguageScreen extends Screen {
    private Label titleLabel;
    private TextButton backButton;
    private SelectionList<Locale> list;

    public LanguageScreen() {
        super(TextObject.translation("ultracraft.screen.language.title"));
    }

    @Override
    public void build(GuiBuilder builder) {
        List<Locale> locales = LanguageManager.INSTANCE.getLocales().stream().sorted((a, b) -> a.getDisplayLanguage().compareToIgnoreCase(b.getDisplayLanguage())).collect(Collectors.toList());

        this.titleLabel = builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, 15))
                .scale(2));

        this.list = builder.add(new SelectionList<Locale>(21).bounds(() -> new Bounds(this.size.width / 2 - 200, 50, 400, this.size.height - 90)))
                .selectable(true)
                .entries(locales)
                .itemRenderer(this::renderItem)
                .callback(this::setLanguage);

        this.list.entry(LanguageManager.getCurrentLanguage()).select();

        this.backButton = builder.add(TextButton.of(UITranslations.BACK)
                .bounds(() -> new Bounds(this.size.width / 2 - 75, this.size.height - 30, 150, 21))
                .callback(caller -> this.back()));
    }

    private void renderItem(Renderer renderer, Locale locale, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        String text = locale.getDisplayLanguage(Locale.of("en")) + " (" + locale.getDisplayCountry(Locale.of("en")) + ")";
        text += " - " + locale.getDisplayLanguage(locale) + " (" + locale.getDisplayCountry(locale) + ")";

        renderer.textCenter(text, this.list.getX() + this.list.getWidth() / 2f, y + 4f);
    }

    public Label getTitleLabel() {
        return this.titleLabel;
    }

    public SelectionList<Locale> getList() {
        return this.list;
    }

    public TextButton getBackButton() {
        return this.backButton;
    }

    private void setLanguage(Locale locale) {
        Config.language = LanguageManager.INSTANCE.getLanguageID(locale);
        this.client.newConfig.save();
    }
}
