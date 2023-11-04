package com.ultreon.craft.text;

import java.text.AttributedString;

class AttributedStringBuilder {
    private AttributedString builString;

    public AttributedStringBuilder() {
        this.builString = new AttributedString("");
    }

    public void append(AttributedStringBuilder strings) {
        if (strings == null) {
            return;
        }
        this.append(strings.build());

    }

    public void append(AttributedString string) {
        if (string == null) {
            return;
        }
        this.builString = AttributedStringUtil.concat(this.builString, string);
    }

    public AttributedString build() {
        return this.builString;
    }

    @Override
    public String toString() {
        return AttributedStringUtil.getString(this.builString);
    }
}