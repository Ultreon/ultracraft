package com.ultreon.craft.client.gui.widget.components;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.component.GameComponent;

public class AlignmentComponent extends GameComponent {
    private Alignment alignment;

    public AlignmentComponent(Alignment alignment) {
        super(Alignment.class);
        this.alignment = alignment;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }
}
