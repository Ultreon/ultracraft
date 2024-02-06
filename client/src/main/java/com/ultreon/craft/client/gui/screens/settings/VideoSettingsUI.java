package com.ultreon.craft.client.gui.screens.settings;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.screens.options.OptionsScreen;
import com.ultreon.craft.client.gui.screens.options.OptionsScreen.BooleanEnum;
import com.ultreon.craft.client.gui.screens.tabs.TabBuilder;
import com.ultreon.craft.client.gui.widget.CycleButton;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.Slider;
import com.ultreon.craft.text.TextObject;

import java.util.Objects;

public class VideoSettingsUI {
    static final TextObject TITLE = TextObject.translation("ultracraft.screen.options.video.title");
    private UltracraftClient client;

    public void build(TabBuilder builder) {
        this.client = builder.client();

        builder.add(Label.of(builder.title())
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(builder.content().getX() + 235, builder.content().getY() + 25)));


        builder.add(TextObject.translation("ultracraft.screen.options.video.fov"), Slider.of(200, 30, 160)
                .value(this.client.config.get().video.fov)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 50, 150, 21))
                .callback(this::setFov));

        builder.add(TextObject.translation("ultracraft.screen.options.video.renderDistance"), Slider.of(200, 30, 160)
                .value(this.client.config.get().renderDistance)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 75, 150, 21))
                .callback(this::setRenderDistance));

        builder.add(TextObject.translation("ultracraft.screen.options.video.vSync"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().video.enableVsync ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 100, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("ultracraft.ui.enabled") : TextObject.translation("ultracraft.ui.disabled"))
                .callback(this::setVsync));

        builder.add(TextObject.translation("ultracraft.screen.options.video.fullscreen"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().video.fullscreen ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 125, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "ultracraft.ui.enabled" : "ultracraft.ui.disabled"))
                .callback(this::setFullscreen));

        builder.add(TextObject.translation("ultracraft.screen.options.video.guiScale"), new CycleButton<OptionsScreen.Scale>()
                .values(OptionsScreen.Scale.values())
                .value(Objects.requireNonNullElse(OptionsScreen.Scale.of(this.client.config.get().video.guiScale), OptionsScreen.Scale.MEDIUM))
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 150, 150, 21))
                .formatter(scale -> {
                    if (scale.get() == 0) {
                        return TextObject.literal("Automatic");
                    }
                    return TextObject.literal(scale.get() + "x");
                })
                .callback(this::setScale));

        builder.add(TextObject.translation("ultracraft.screen.options.video.frameRate"), Slider.of(200, 10, 240)
                .value(this.client.config.get().video.fpsLimit)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 175, 150, 21))
                .callback(this::setFrameRate));
    }

    private void setFrameRate(Slider slider) {
        this.client.config.get().video.fpsLimit = slider.value().get();
        this.client.config.save();
    }

    private void setVsync(CycleButton<BooleanEnum> button) {
        this.client.config.get().video.enableVsync = button.getValue().get();
        this.client.config.save();
    }

    private void setScale(CycleButton<OptionsScreen.Scale> caller) {
        int value = caller.getValue().get();
        this.client.setAutomaticScale(caller.getValue() == OptionsScreen.Scale.AUTO);
        this.client.setGuiScale(value);
        this.client.config.get().video.guiScale = value;
        this.client.config.save();
    }

    private void setFullscreen(CycleButton<BooleanEnum> caller) {
        boolean bool = caller.getValue().get();
        this.client.config.get().video.fullscreen = bool;
        this.client.setFullScreen(bool);
        this.client.config.save();
    }

    private void setFov(Slider slider) {
        int fov = slider.value().get();
        this.client.config.get().video.fov = fov;
        this.client.camera.fieldOfView = fov;
        this.client.config.save();
    }

    private void setRenderDistance(Slider slider) {
        this.client.config.get().renderDistance = slider.value().get();
        this.client.config.save();
    }
}
