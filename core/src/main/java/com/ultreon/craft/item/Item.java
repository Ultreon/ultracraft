package com.ultreon.craft.item;

import com.ultreon.craft.registry.Registries;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.translations.v0.Language;
import org.jetbrains.annotations.NotNull;

public class Item {
    public Item() {

    }

    public void use(UseItemContext useItemContext) {

    }

    public String getTranslation() {
        return Language.translate(getTranslationId());
    }

    @NotNull
    public String getTranslationId() {
        Identifier key = Registries.ITEMS.getKey(this);
        return key == null ? "craft/item/air/name" : key.location() + "/item/" + key.path() + "/name";
    }
}
