package com.ultreon.craft.client.gui.screens.options;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.screens.options.OptionsScreen.BooleanEnum;
import com.ultreon.craft.client.gui.widget.CycleButton;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.Slider;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.client.text.UITranslations;
import com.ultreon.craft.text.TextObject;

import java.util.Objects;

public class VideoSettingsScreen extends Screen {
    private static final TextObject TITLE = TextObject.translation("ultracraft.screen.options.video.title");

    public VideoSettingsScreen() {
        super(VideoSettingsScreen.TITLE);
    }

    public VideoSettingsScreen(Screen parent) {
        super(VideoSettingsScreen.TITLE, parent);
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(this.size.width / 2, this.size.height / 2 - 85)));


        builder.add(Slider.of(200, 30, 160)
                .text(TextObject.translation("ultracraft.screen.options.video.fov"))
                .value(this.client.config.get().video.fov)
                .bounds(() -> new Bounds(this.getWidth() / 2 - 152, this.getHeight() / 2 - 50, 150, 21))
                .callback(this::setFov));

        builder.add(Slider.of(200, 30, 160)
                .text(TextObject.translation("ultracraft.screen.options.video.renderDistance"))
                .value(this.client.config.get().renderDistance)
                .bounds(() -> new Bounds(this.getWidth() / 2 + 2, this.getHeight() / 2 - 50, 150, 21))
                .callback(this::setRenderDistance));

        builder.add(new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().video.enableVsync ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .label(TextObject.translation("ultracraft.screen.options.video.vSync"))
                .bounds(() -> new Bounds(this.getWidth() / 2 - 152, this.getHeight() / 2 - 25, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("ultracraft.ui.enabled") : TextObject.translation("ultracraft.ui.disabled"))
                .callback(this::setVsync));

        builder.add(new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().video.fullscreen ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .label(TextObject.translation("ultracraft.screen.options.video.fullscreen"))
                .bounds(() -> new Bounds(this.getWidth() / 2 + 2, this.getHeight() / 2 - 25, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "ultracraft.ui.enabled" : "ultracraft.ui.disabled"))
                .callback(this::setFullscreen)
        );

        builder.add(new CycleButton<OptionsScreen.Scale>()
                .values(OptionsScreen.Scale.values())
                .value(Objects.requireNonNullElse(OptionsScreen.Scale.of(this.client.config.get().video.guiScale), OptionsScreen.Scale.MEDIUM))
                .label(TextObject.translation("ultracraft.screen.options.video.guiScale"))
                .bounds(() -> new Bounds(this.getWidth() / 2 - 152, this.getHeight() / 2, 150, 21))
                .formatter(scale -> {
                    if (scale.get() == 0) {
                        return TextObject.literal("Automatic");
                    }
                    return TextObject.literal(scale.get() + "x");
                })
                .callback(this::setScale)
        );

        builder.add(Slider.of(200, 1, 240)
                .text(TextObject.translation("ultracraft.screen.options.video.frameRate"))
                .value(this.client.config.get().video.fpsLimit)
                .bounds(() -> new Bounds(this.getWidth() / 2 + 2, this.getHeight() / 2, 150, 21))
                .callback(this::setFrameRate));

        builder.add(TextButton.of(UITranslations.OK)
                .bounds(() -> new Bounds(this.size.width / 2 - 75, this.size.height / 2 + 50, 150, 21))
                .callback(caller -> this.back()));
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
