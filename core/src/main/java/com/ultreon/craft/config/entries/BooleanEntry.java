package com.ultreon.craft.config.entries;

import com.ultreon.craft.config.Config;
import com.ultreon.craft.config.gui.ConfigEntry;
import com.ultreon.craft.render.gui.CycleButton;
import com.ultreon.craft.render.gui.GuiComponent;
import com.ultreon.libs.translations.v0.Language;

public class BooleanEntry extends ConfigEntry<Boolean> {
    public BooleanEntry(String key, boolean value, Config config) {
        super(key, value, config);
    }

    @Override
    protected Boolean read(String text) {
        return Boolean.parseBoolean(text);
    }

    @Override
    public GuiComponent createButton(Config options, int x, int y, int width) {
        return new CycleButton<Boolean>(x, y, width, 20, this.getDescription())
                .withFormatter(o -> (boolean)o ? Language.translate("misc.on") : Language.translate("misc.off")).withValues(true, false);
    }

    @Override
    public void setFromWidget(GuiComponent widget) {
        CycleButton<?> cycleButton = (CycleButton<?>) widget;
        boolean value = (boolean) cycleButton.getValue();
        this.set(value);
    }
}
