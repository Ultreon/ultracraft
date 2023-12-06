package com.ultreon.craft.client.gui.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.UIContainer;
import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

@ApiStatus.Experimental
public abstract class Screen extends UIContainer<Screen> {
    protected TextObject title;
    public Screen parentScreen;
    public Widget directHovered;
    public @Nullable Widget focused;

    protected Screen(String title) {
        this(TextObject.literal(title));
    }

    protected Screen(TextObject title) {
        this(title, UltracraftClient.get().screen);
    }

    protected Screen(String title, Screen parent) {
        this(TextObject.literal(title), parent);
    }

    protected Screen(TextObject title, Screen parent) {
        super(0, 0, Screen.width(), Screen.height());
        this.parentScreen = parent;
        this.root = this;
        this.title = title;
        this.visible = true;
    }

    public final void resize(int width, int height) {
        this.size.set(width, height);
        this.revalidate();
    }

    @Override
    public void revalidate() {
        super.revalidate();
        this.visible = true;
    }

    @Override
    public final String getName() {
        return "Screen";
    }

    private static int width() {
        return UltracraftClient.get().getScaledWidth();
    }

    private static int height() {
        return UltracraftClient.get().getScaledHeight();
    }

    public final void init(int width, int height) {
        this.setSize(width, height);
        this.build(new GuiBuilder(this));
        this.revalidate();
    }

    public abstract void build(GuiBuilder builder);

    /**
     * Renders the background of this screen.
     * By default, renders a transparent background if the world is loaded, otherwise renders a solid background.
     *
     * @param renderer renderer to draw/render with.
     */
    protected void renderBackground(Renderer renderer) {
        if (this.client.world != null) this.renderTransparentBackground(renderer);
        else this.renderSolidBackground(renderer);
    }

    @Override
    protected final void renderBackground(Renderer renderer, float deltaTime) {
        this.directHovered = null;

        this.renderBackground(renderer);
        super.renderBackground(renderer, deltaTime);
    }

    /**
     * Renders a solid background
     *
     * @param renderer renderer to draw/render with.
     */
    protected void renderSolidBackground(Renderer renderer) {
        renderer.fill(0, 0, this.size.width, this.size.height, Color.argb(0xff202020));
    }

    /**
     * Renders a transparent background.
     *
     * @param renderer renderer to draw/render with.
     */
    protected void renderTransparentBackground(Renderer renderer) {
        renderer.fill(0, 0, this.size.width, this.size.height, Color.argb(0xd0202020));
    }

    /**
     * @return true if this screen successfully closes, false otherwise.
     */
    public boolean back() {
        if (!this.canClose()) return false;
        this.client.showScreen(this.parentScreen);
        return true;
    }

    /**
     * Handles files being dropped into this screen.
     *
     * @param files list of files being dropped.
     */
    @ApiStatus.OverrideOnly
    public void filesDropped(List<FileHandle> files) {

    }

    @Override
    public Path path() {
        UIContainer<?> screen = this.parentScreen;
        if (screen == null) screen = UIContainer.ROOT;
        return screen.path().resolve("OldScreen[" + this.createTime + "]");
    }

    public TextObject getTitle() {
        return this.title;
    }

    public boolean canClose() {
        return true;
    }

    public Screen title(String title) {
        this.title = TextObject.literal(title);
        return this;
    }

    public Screen title(TextObject title) {
        this.title = title;
        return this;
    }

    public Screen titleTranslation(String title) {
        this.title = TextObject.translation(title);
        return this;
    }

    public String getRawTitle() {
        return this.title == null ? "" : this.title.getText();
    }

    /**
     * Called when this screen is closing.
     *
     * @param next screen to go to after this screen is closed.
     * @return true to continue to the next screen, false otherwise.
     */
    public boolean onClose(Screen next) {
        return true;
    }

    /**
     * Non-cancelable version of {@link Screen#onClose(Screen)}.
     * Called when this screen is going to be closed.
     */
    public void onClosed() {
        this.widgets.clear();
    }

    @Override
    public <C extends Widget> C add(C widget) {
        return super.add(widget);
    }

    public boolean isHoveringClickable() {
        Widget hovered = this.directHovered;
        return hovered != null && hovered.isClickable();
    }

    public boolean canCloseWithEsc() {
        return true;
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
        Widget widgetsAt = this.getWidgetAt(mouseX, mouseY);
        if (this.focused != null) this.focused.onFocusLost();
        this.focused = widgetsAt;
        if (this.focused != null) this.focused.onFocusGained();

        return super.mouseClick(mouseX, mouseY, button, clicks);
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (keyCode == Input.Keys.ESCAPE && this.canCloseWithEsc()) {
            this.back();
            return true;
        }

        if (this.focused != null) {
            if (this.focused.keyPress(keyCode)) return true;
        }

        return super.keyRelease(keyCode);
    }

    @Override
    public boolean keyRelease(int keyCode) {
        if (this.focused != null) {
            if (this.focused.keyRelease(keyCode)) return true;
        }

        return super.keyRelease(keyCode);
    }

    @Override
    public boolean charType(char character) {
        if (this.focused != null) {
            if (this.focused.charType(character)) return true;
        }

        return super.charType(character);
    }

    protected final void close() {
        this.client.showScreen(null);
    }
}
