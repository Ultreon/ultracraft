package com.ultreon.craft.client.gui.screens.container;

import com.badlogic.gdx.Input;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.menu.ContainerMenu;
import com.ultreon.craft.menu.ItemSlot;
import com.ultreon.craft.network.packets.c2s.C2SCloseContainerMenuPacket;
import com.ultreon.craft.network.packets.c2s.C2SMenuTakeItemPacket;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ContainerScreen extends Screen {
    private final int maxSlots;
    private final ContainerMenu container;
    private final LocalPlayer player;

    public ContainerScreen(ContainerMenu container, TextObject title, int maxSlots) {
        this(container, UltracraftClient.get().screen, title, maxSlots);
    }

    public ContainerScreen(ContainerMenu container, @Nullable Screen back, TextObject title, int maxSlots) {
        super(title, back);
        this.container = container;
        this.maxSlots = maxSlots;

        this.player = this.client.player;
        Preconditions.checkNotNull(this.player, "Local player is null");
    }

    @Override
    public final void build(GuiBuilder builder) {
        //* Stub
    }

    public int left() {
        return (this.size.width - this.backgroundWidth()) / 2;
    }
    public int top() {
        return (this.size.height - this.backgroundHeight()) / 2;
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
            this.client.itemRenderer.render(slotItem.getItem(), renderer, x, y);

            if (slot.isWithinBounds(mouseX - this.left(), mouseY - this.top())) {
                renderer.fill(x, y, 16, 16, Color.WHITE.withAlpha(0x60));
            }

            if (!slotItem.isEmpty() && slotItem.getCount() > 1) {
                String text = Integer.toString(slotItem.getCount());
                renderer.drawTextLeft(text, x + 18 - this.font.width(text), y + 17 - this.font.lineHeight, Color.WHITE, false);
            }
        }
    }

    protected void renderBackgroundImage(Renderer renderer) {
        renderer.blit(this.getBackground(), this.left(), this.top(), this.backgroundWidth(), this.backgroundHeight());
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        this.renderSlots(renderer, mouseX, mouseY);
        this.renderForeground(renderer, mouseX, mouseY, deltaTime);
    }

    public void renderForeground(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        ItemSlot slotAt = this.getSlotAt(mouseX, mouseY);
        if (slotAt != null && !slotAt.getItem().isEmpty()) {
            this.renderTooltip(renderer, mouseX + 4, mouseY + 4, slotAt.getItem().getItem().getTranslation(), slotAt.getItem().getDescription(), slotAt.getItem().getItem().getId().toString());
        }

        ItemStack cursor = this.player.getCursor();
        if (!cursor.isEmpty()) {
            this.client.itemRenderer.render(cursor.getItem(), renderer, mouseX - 8, mouseY - 8);
        }
    }

    private void renderTooltip(Renderer renderer, int x, int y, TextObject title, List<TextObject> description, @Nullable String subTitle) {
        var all = Lists.newArrayList(description);
        all.add(0, title);
        if (subTitle != null) all.add(TextObject.literal(subTitle));
        int textWidth = all.stream().mapToInt(value -> this.font.width(value)).max().orElse(0);
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

        renderer.drawTextLeft(title, x + 3, y + 3, Color.WHITE);

        int lineNr = 0;
        for (TextObject line : description) {
            renderer.drawTextLeft(line, x + 3, y + 3 + this.font.lineHeight + 3 + lineNr * (this.font.lineHeight + 1f) - 1, Color.rgb(0xa0a0a0));
            lineNr++;
        }

        if (subTitle != null)
            renderer.drawTextLeft(subTitle, x + 3, y + 3 + this.font.lineHeight + 3 + lineNr * (this.font.lineHeight + 1f) - 1, Color.rgb(0x606060));
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
            this.client.connection.send(new C2SMenuTakeItemPacket(slot.getIndex(), false));
            return true;
        }
        if (button == Input.Buttons.RIGHT) {
            this.client.connection.send(new C2SMenuTakeItemPacket(slot.getIndex(), true));
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

    @Override
    public void onClosed() {
        super.onClosed();

        this.client.connection.send(new C2SCloseContainerMenuPacket());
    }
}
