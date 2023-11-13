package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Notification;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.icon.MessageIcon;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.craft.world.WorldStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class WorldDeleteConfirmScreen extends Screen {
    @NotNull
    private final WorldStorage storage;

    public WorldDeleteConfirmScreen(@NotNull WorldStorage storage) {
        super(TextObject.translation("ultracraft.screen.world_delete_confirm.title").setColor(Color.rgb(0xff4040)));
        this.storage = storage;
    }

    @Override
    public void build(GuiBuilder builder) {
        Label titleLabel = builder.addWithPos(new Label(Alignment.CENTER, Color.RED), () -> new Position(this.getWidth() / 2, this.getHeight() / 2 - 30));
        titleLabel.text().set(this.getTitle());
        titleLabel.scale().set(2);

        builder.addWithPos(new Label(Alignment.CENTER), () -> new Position(this.getWidth() / 2, this.getHeight() / 2))
                .text().translate("ultracraft.screen.world_delete_confirm.message1", this.storage.getDirectory().getFileName().toString());

        builder.addWithPos(new Label(Alignment.CENTER), () -> new Position(this.getWidth() / 2, this.getHeight() / 2 + 15))
                .text().translate("ultracraft.screen.world_delete_confirm.message2");

        var yesBtn = builder.addWithPos(new Button(95), () -> new Position(this.getWidth() / 2 - 100, this.getHeight() / 2 + 50));
        yesBtn.callback().set(this::deleteWorld);
        yesBtn.text().translate("ultracraft.ui.yes");

        var noBtn = builder.addWithPos(new Button(95), () -> new Position(this.getWidth() / 2 + 5, this.getHeight() / 2 + 50));
        noBtn.callback().set(this::onBack);
        noBtn.text().translate("ultracraft.ui.no");
    }

    private void deleteWorld(Button caller) {
        try {
            Path name = this.storage.getDirectory().getFileName();
            this.storage.delete();
            this.client.notifications.add(Notification.builder("World Deleted", "'%s'".formatted(name)).subText("World Manager").icon(MessageIcon.DANGER).build());
        } catch (IOException e) {
            UltracraftClient.crash(e);
        }
        this.back();
    }

    private void onBack(Button caller) {
        this.back();
    }

    public @NotNull WorldStorage getStorage() {
        return this.storage;
    }
}
