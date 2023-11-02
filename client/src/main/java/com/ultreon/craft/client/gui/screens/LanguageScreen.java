package com.ultreon.craft.client.gui.screens;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.ultreon.craft.client.util.Color;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.SelectionList;
import com.ultreon.libs.translations.v1.Language;
import com.ultreon.libs.translations.v1.LanguageManager;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LanguageScreen extends Screen {
    private Button backButton;
    private Button langButton1;
    private Button langButton2;
    private Button langButton3;
    private Button prevPageButton;
    private Button nextPageButton;
    private SelectionList<Locale> list;
    private final GlyphLayout layout = new GlyphLayout();

    public LanguageScreen() {
        super("Language");
    }

    @Override
    public void init() {
        super.init();

        this.list = this.add(new SelectionList<>(0, 0, this.width, this.height - 40, 28));
        this.list.setSelectable(true);
        this.list.setItemRenderer(this::renderItem);
        this.list.setOnSelected(locale -> {
            this.client.settings.language.set(locale);
            this.client.settings.save();
            this.updateTexts();
        });

        List<Locale> locales = LanguageManager.INSTANCE.getLocales().stream().sorted((o1, o2) -> o1.getDisplayLanguage().compareToIgnoreCase(o2.getDisplayLanguage())).collect(Collectors.toList());
        this.list.addEntries(locales);

        this.backButton = this.add(new Button(this.width / 2 - 100, this.height - 30, 200, Language.translate("craft.screen.language.back"), caller -> {
            if (this.client.world != null) {
                this.client.showScreen(new PauseScreen());
            } else {
                this.client.showScreen(null);
            }
        }));
        this.backButton.setColor(Color.rgb(0xff0000));

        if (false) {
            this.prevPageButton = this.add(new Button(this.width / 2 - 100 - 10 - 21, this.height / 2 + 21 + 15, 21, 21 * 3 + 20, "<"));
            this.nextPageButton = this.add(new Button(this.width / 2 + 100 + 10, this.height / 2 + 21 + 15, 21, 21 * 3 + 20, ">"));

            this.langButton1 = this.add(new Button(this.width / 2 - 100, this.height / 2 - 21 - 5, 200, "English (" + new Locale("en").getDisplayLanguage(new Locale("en")) + ")", caller -> {
                this.client.settings.language.set(new Locale("en"));
                this.client.settings.save();
                this.updateTexts();
            }));
            this.langButton2 = this.add(new Button(this.width / 2 - 100, this.height / 2 + 5, 200, "Dutch (" + new Locale("nl").getDisplayLanguage(new Locale("nl")) + ")", caller -> {
                this.client.settings.language.set(new Locale("nl"));
                this.client.settings.save();
                this.updateTexts();
            }));
            this.langButton3 = this.add(new Button(this.width / 2 - 100, this.height / 2 + 21 + 15, 200, "German (" + new Locale("de").getDisplayLanguage(new Locale("de")) + ")", caller -> {
                this.client.settings.language.set(new Locale("de"));
                this.client.settings.save();
                this.updateTexts();
            }));
            this.backButton = this.add(new Button(this.width / 2 - 100 - 10 - 21, this.height / 2 - 42 - 25, 200 + 10 * 2 + 21 * 2, Language.translate("craft.screen.language.back"), caller -> {
                if (this.client.world != null) {
                    this.client.showScreen(new PauseScreen());
                } else {
                    this.client.showScreen(null);
                }
            }));
            this.backButton.setColor(Color.rgb(0xff0000));
        }
    }

    private void renderItem(Renderer renderer, Locale locale, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        if (selected) {
            System.out.println("renderer = " + renderer + ", locale = " + locale + ", y = " + y + ", mouseX = " + mouseX + ", mouseY = " + mouseY + ", selected = " + selected + ", deltaTime = " + deltaTime);
        }
        String text = locale.getDisplayLanguage(new Locale("en")) + " (" + locale.getDisplayCountry(new Locale("en")) + ")";
        text += " - " + locale.getDisplayLanguage(locale) + " (" + locale.getDisplayCountry(locale) + ")";
        renderer.drawCenteredText(text, this.width / 2f, y + 4f);
    }

    private void updateTexts() {
        this.backButton.setMessage(Language.translate("craft.screen.language.back"));
    }

    public Button getBackButton() {
        return this.backButton;
    }

    public Button getLangButton1() {
        return this.langButton1;
    }

    public Button getLangButton2() {
        return this.langButton2;
    }

    public Button getLangButton3() {
        return this.langButton3;
    }

    public Button getPrevPageButton() {
        return this.prevPageButton;
    }

    public Button getNextPageButton() {
        return this.nextPageButton;
    }
}
