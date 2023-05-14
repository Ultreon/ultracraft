package com.ultreon.craft.render.gui.screens;

import com.ultreon.craft.options.GameSettings;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.gui.widget.Button;
import com.ultreon.libs.translations.v0.Language;
import com.ultreon.libs.translations.v0.LanguageManager;

import java.util.Locale;

public class LanguageScreen extends Screen {
    private Button backButton;
    private Button langButton1;
    private Button langButton2;
    private Button langButton3;
    private Button prevPageButton;
    private Button nextPageButton;

    public LanguageScreen() {
        super("Language");
    }

    @Override
    public void show() {
        super.show();

        prevPageButton = add(new Button(width / 2 - 100 - 10 - 21, height / 2 - 21 - 15, 21, 21 * 3 + 20, "<"));
        nextPageButton = add(new Button(width / 2 + 100 + 10, height / 2 - 21 - 15, 21, 21 * 3 + 20, ">"));

        langButton1 = add(new Button(width / 2 - 100, height / 2 + 21 + 5, 200, "English (" + new Locale("en").getDisplayLanguage(new Locale("en")) + ")", caller -> {
            game.settings.setLanguage(new Locale("en"));
            updateTexts();
        }));
        langButton2 = add(new Button(width / 2 - 100, height / 2 - 5, 200, "Dutch (" + new Locale("nl").getDisplayLanguage(new Locale("nl")) + ")", caller -> {
            game.settings.setLanguage(new Locale("nl"));
            updateTexts();
        }));
        langButton3 = add(new Button(width / 2 - 100, height / 2 - 21 - 15, 200, "German (" + new Locale("de").getDisplayLanguage(new Locale("de")) + ")", caller -> {
            game.settings.setLanguage(new Locale("de"));
            updateTexts();
        }));
        backButton = add(new Button(width / 2 - 100 - 10 - 21, height / 2 - 42 - 25, 200 + 10 * 2 + 21 * 2, Language.translate("craft/screen/language/back"), caller -> {
            if (game.world != null) {
                game.showScreen(new PauseScreen());
            } else {
                game.showScreen(null);
            }
        }));
        backButton.setColor(Color.rgb(0xff0000));
    }

    private void updateTexts() {
        backButton.setMessage(Language.translate("craft/screen/language/back"));
    }

    public Button getBackButton() {
        return backButton;
    }

    public Button getLangButton1() {
        return langButton1;
    }

    public Button getLangButton2() {
        return langButton2;
    }

    public Button getLangButton3() {
        return langButton3;
    }

    public Button getPrevPageButton() {
        return prevPageButton;
    }

    public Button getNextPageButton() {
        return nextPageButton;
    }
}
