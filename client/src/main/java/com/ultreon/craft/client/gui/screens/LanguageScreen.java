package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.*;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.SelectionList;
import com.ultreon.craft.text.TextObject;
import com.ultreon.libs.translations.v1.LanguageManager;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LanguageScreen extends Screen {
    private Label titleLabel;
    private Button backButton;
    private SelectionList<Locale> list;

    public LanguageScreen() {
        super(TextObject.translation("ultracraft.screen.language"));
    }

    @Override
    public void build(GuiBuilder builder) {
        List<Locale> locales = LanguageManager.INSTANCE.getLocales().stream().sorted((a, b) -> a.getDisplayLanguage().compareToIgnoreCase(b.getDisplayLanguage())).collect(Collectors.toList());

        this.titleLabel = builder.addWithPos(new Label(Alignment.CENTER), () -> new Position(this.size.width / 2, 15));
        this.titleLabel.text().set(this.title);
        this.titleLabel.scale().set(2);

        this.list = builder.addWithBounds(new SelectionList<>(21), () -> new Bounds(this.size.width / 2 - 200, 50, 400, this.size.height - 90));
        this.list.withSelectable(true).withItemRenderer(this::renderItem).withEntries(locales).withCallback(locale -> {
            this.client.settings.language.set(locale);
            this.client.settings.save();
        });

        this.backButton = builder.addWithPos(new Button(), () -> new Position(this.size.width / 2 - 100, this.size.height - 30));
        this.backButton.callback().set(caller -> this.back());
        this.backButton.text().translate("ultracraft.ui.back");
    }

    private void renderItem(Renderer renderer, Locale locale, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        String text = locale.getDisplayLanguage(new Locale("en")) + " (" + locale.getDisplayCountry(new Locale("en")) + ")";
        text += " - " + locale.getDisplayLanguage(locale) + " (" + locale.getDisplayCountry(locale) + ")";
        renderer.drawTextCenter(text, this.list.getX() + this.list.getWidth() / 2f, y + 4f);
    }

    public Label getTitleLabel() {
        return this.titleLabel;
    }

    public SelectionList<Locale> getList() {
        return this.list;
    }

    public Button getBackButton() {
        return this.backButton;
    }
}
