package com.ultreon.craft.client.gui;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.*;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.Experimental
public record GuiBuilder(Screen screen) {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends Button<T>> Button<T> button(Supplier<Position> positionSupplier, Callback<Button<?>> callback) {
        Button add = this.screen.add(new Button(0, 0, 200, 21) {
            @Override
            public void revalidate() {
                super.revalidate();

                this.pos.set(positionSupplier.get());
            }
        });
        add.callback(callback);
        return add;
    }

    public Label label(Supplier<Position> pos) {
        return this.label(Alignment.LEFT, pos);
    }

    public Label label(Alignment alignment, Supplier<Position> positionSupplier) {
        return this.screen.add(new Label(0, 0, alignment) {
            @Override
            public void revalidate() {
                super.revalidate();

                this.pos.set(positionSupplier.get());
            }
        });
    }

    @CanIgnoreReturnValue
    public <T> SelectionList<T> selectionList(int itemHeight, Supplier<Bounds> boundsSupplier) {
        return this.screen.add(new SelectionList<>(boundsSupplier.get().pos(), boundsSupplier.get().size(), itemHeight) {
            @Override
            public void revalidate() {
                super.revalidate();

                this.pos.set(boundsSupplier.get().pos());
                this.size.set(boundsSupplier.get().size());
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @CanIgnoreReturnValue
    public <T extends TextEntry<T>> TextEntry<T> textEntry(Supplier<Position> positionSupplier) {
        return this.screen.add(new TextEntry(0, 0, 200, 21) {
            @Override
            public void revalidate() {
                super.revalidate();

                this.pos.set(positionSupplier.get());
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @CanIgnoreReturnValue
    public <T extends TextEntry<T>> TextEntry<T> textEntry(Supplier<Position> positionSupplier, Callback<T> callback) {
        TextEntry add = this.screen.add(new TextEntry(0, 0, 200, 21) {
            @Override
            public void revalidate() {
                super.revalidate();

                this.pos.set(positionSupplier.get());
            }
        });
        add.callback(callback);
        return add;
    }

    @CanIgnoreReturnValue
    public Panel panelBounds(Supplier<Bounds> boundsSupplier) {
        return this.screen.add(new Panel(0, 0, 0, 0) {
            @Override
            public void revalidate() {
                super.revalidate();

                this.pos.set(boundsSupplier.get().pos());
                this.size.set(boundsSupplier.get().size());
            }
        });
    }

    @CanIgnoreReturnValue
    public Panel panel(Supplier<Position> positionSupplier) {
        return this.screen.add(new Panel(0, 0, 0, 0) {
            @Override
            public void revalidate() {
                super.revalidate();

                this.pos.set(positionSupplier.get());
            }
        });
    }
}
