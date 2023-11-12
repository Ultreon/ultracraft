package com.ultreon.craft.client.gui;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.*;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.Experimental
public record GuiBuilder(Screen screen) {
    @CanIgnoreReturnValue
    public <T extends Button<T>> Button<T> button(Supplier<Position> positionSupplier, Callback<T> callback) {
        return this.screen.add(new Button<T>(0, 0, 200, 21))
                .onRevalidate(it -> it.pos(positionSupplier.get()))
                .callback(callback);
    }

    public <T extends Enum<T>> CycleButton<T> cycleButton(Supplier<Position> positionSupplier, Callback<CycleButton<T>> callback) {
        return this.screen.add(new CycleButton<T>())
                .onRevalidate(it -> it.pos(positionSupplier.get()))
                .callback(callback);
    }

    @CanIgnoreReturnValue
    public Label label(Supplier<Position> pos) {
        return this.label(Alignment.LEFT, pos);
    }

    @CanIgnoreReturnValue
    public Label label(Alignment alignment, Supplier<Position> pos) {
        return this.screen.add(new Label(0, 0, alignment))
                .onRevalidate(it -> it.pos(pos.get()));
    }

    @CanIgnoreReturnValue
    public <T> SelectionList<T> selectionList(int itemHeight, Supplier<Bounds> bounds) {
        return this.screen.add(new SelectionList<T>())
                .itemHeight(itemHeight)
                .onRevalidate(it -> it.bounds(bounds.get()));
    }

    @CanIgnoreReturnValue
    public <T extends TextEntry<T>> TextEntry<T> textEntry(Supplier<Position> pos) {
        return this.screen.add(new TextEntry<T>()
                .onRevalidate(it -> it.pos(pos.get())));
    }

    @CanIgnoreReturnValue
    public <T extends TextEntry<T>> TextEntry<T> textEntry(Supplier<Position> pos, Callback<T> callback) {
        return this.screen.add(new TextEntry<T>()
                        .onRevalidate(it -> it.pos(pos.get())))
                .callback(callback).callback(callback);
    }

    @CanIgnoreReturnValue
    public Panel panelBounds(Supplier<Bounds> bounds) {
        return this.screen.add(new Panel())
                .onRevalidate(it -> it.bounds(bounds.get()));
    }

    @CanIgnoreReturnValue
    public Panel panel(Supplier<Position> positionSupplier) {
        return this.screen.add(new Panel(0, 0, 0, 0))
                .onRevalidate(it -> it.pos(positionSupplier.get()));
    }

    @CanIgnoreReturnValue
    public <T extends Widget<T>> T custom(T widget) {
        return this.screen.add(widget);
    }

    @CanIgnoreReturnValue
    public Slider slider(Supplier<Position> positionSupplier) {
        return this.screen.add(new Slider())
                .onRevalidate(it -> it.pos(positionSupplier.get()));
    }

    @CanIgnoreReturnValue
    public <T extends Widget<T>> T custom(T widget, Supplier<Position> positionSupplier) {
        return this.screen.add(widget)
                .onRevalidate(it -> it.pos(positionSupplier.get()));
    }
}
