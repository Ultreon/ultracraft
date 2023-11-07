package com.ultreon.craft.render.gui.screens.container;

import com.badlogic.gdx.Input;
import com.google.common.collect.Lists;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.menu.ContainerMenu;
import com.ultreon.craft.menu.ItemSlot;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.GuiComponent;
import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.libs.commons.v0.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ContainerScreen extends Screen {
    private final int maxSlots;
    private final ContainerMenu container;

    public ContainerScreen(ContainerMenu container, String title, int maxSlots) {
        this(container, UltreonCraft.get().currentScreen, title, maxSlots);
    }

    public ContainerScreen(ContainerMenu container, @Nullable Screen back, String title, int maxSlots) {
        super(back, title);
        this.container = container;
        this.maxSlots = maxSlots;
    }

    public int left() {
        return (this.width - this.backgroundWidth()) / 2;
    }
    public int top() {
        return (this.height - this.backgroundHeight()) / 2;
    }

    public abstract int backgroundWidth();
    public abstract int backgroundHeight();

    public abstract Identifier getBackground();

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        this.renderBackgroundImage(renderer);
    }

    private void renderSlots(Renderer renderer, int mouseX, int mouseY) {
        for (var slot : this.container.slots) {
            var x = this.left() + slot.getSlotX();
            var y = this.top() + slot.getSlotY();

            ItemStack slotItem = slot.getItem();
            this.game.itemRenderer.render(slotItem.getItem(), renderer, x, y);

            if (slot.isWithinBounds(mouseX - this.left(), mouseY - this.top())) {
                GuiComponent.fill(renderer, x, y, 16, 16, 0x60ffffff);
            }

            if (!slotItem.isEmpty() && slotItem.getCount() > 1) {
                String text = Integer.toString(slotItem.getCount());
                renderer.drawText(text, x + 18 - this.font.width(text), y + 17 - this.font.lineHeight, Color.WHITE, false);
            }
        }
    }

    protected void renderBackgroundImage(Renderer renderer) {
        renderer.blit(this.getBackground(), this.left(), this.top(), this.backgroundWidth(), this.backgroundHeight());
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.render(renderer, mouseX, mouseY, deltaTime);

        this.renderSlots(renderer, mouseX, mouseY);

        this.renderForeground(renderer, mouseX, mouseY, deltaTime);
    }

    protected void renderForeground(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        ItemSlot slotAt = this.getSlotAt(mouseX, mouseY);
        if (slotAt != null && !slotAt.getItem().isEmpty()) {
            this.renderTooltip(renderer, mouseX + 4, mouseY + 4, slotAt.getItem().getItem().getTranslation(), slotAt.getItem().getDescription(), slotAt.getItem().getItem().getId().toString());
        }
        if (!this.container.getCursor().isEmpty()) {
            this.game.itemRenderer.render(this.container.getCursor().getItem(), renderer, mouseX - 8, mouseY - 8);
        }
    }

    private void renderTooltip(Renderer renderer, int x, int y, String title, List<String> description, @Nullable String subTitle) {
        List<String> all = Lists.newArrayList(description);
        all.add(0, title);
        if (subTitle != null) all.add(subTitle);
        int textWidth = all.stream().mapToInt(value -> (int) this.font.width(value)).max().orElse(0);
        int descHeight = description.size() * (this.font.lineHeight + 1) - 1;
        int textHeight = descHeight + 3 + this.font.lineHeight;

        if (description.isEmpty() && subTitle == null) {
            textHeight -= 3;
        }
        if (subTitle != null) {
            textHeight += 1 + this.font.lineHeight;
        }

        renderer.fill(x + 1, y, textWidth + 4, textHeight + 6, Color.rgb(0x202020));
        renderer.fill(x, y + 1, textWidth + 6, textHeight + 4, Color.rgb(0x202020));
        renderer.box(x + 1, y + 1, textWidth + 4, textHeight + 4, Color.rgb(0x303030));

        renderer.drawText(title, x + 3, y + 3, Color.WHITE);

        int lineNr = 0;
        for (String line : description) {
            renderer.drawText(line, x + 3, y + 3 + this.font.lineHeight + 3 + lineNr * (this.font.lineHeight + 1f) - 1, Color.rgb(0xa0a0a0));
            lineNr++;
        }

        if (subTitle != null)
            renderer.drawText(subTitle, x + 3, y + 3 + this.font.lineHeight + 3 + lineNr * (this.font.lineHeight + 1f) - 1, Color.rgb(0x606060));
    }

    protected @Nullable ItemSlot getSlotAt(int mouseX, int mouseY) {
        for (ItemSlot slot : this.container.slots) {
            if (slot.isWithinBounds(mouseX - this.left(), mouseY - this.top())) {
                return slot;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        ItemSlot slot = this.getSlotAt(x, y);
        if (slot == null) return super.mouseClick(x, y, button, count);
        if (button == Input.Buttons.LEFT) {
            ItemStack cursor = this.container.getCursor();
            ItemStack slotItem = slot.getItem();
            if (!cursor.isEmpty() && cursor.isSameItemSameTag(slotItem)) {
                cursor.transferTo(slotItem, cursor.getCount());
                return true;
            }
            slot.setItem(cursor);
            this.container.setCursor(slotItem);
            return true;
        }
        if (button == Input.Buttons.RIGHT) {
            if (this.container.getCursor().isEmpty()) {
                ItemStack item = slot.getItem().split();
                this.container.setCursor(item);
            } else {
                this.container.getCursor().transferTo(slot.getItem());
            }
            return true;
        }

        return super.mouseClick(x, y, button, count);
    }

    public int getMaxSlots() {
        return this.maxSlots;
    }

    public ItemSlot get(int index) {
        return this.container.get(index);
    }
}