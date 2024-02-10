package com.ultreon.craft.client.input.gamepad;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

public final class GamepadMappings {
    private final List<GamepadMapping<GamepadButton>> buttonMappings = new ArrayList<>();
    private final List<GamepadMapping<GamepadAxis>> axisMappings = new ArrayList<>();
    private final List<GamepadMapping<GamepadJoystick>> joystickMappings = new ArrayList<>();
    private final List<GamepadMapping<GamepadTrigger>> triggerMappings = new ArrayList<>();

    public List<GamepadMapping<GamepadButton>> getButtonMappings() {
        return buttonMappings;
    }

    public List<GamepadMapping<GamepadAxis>> getAxisMappings() {
        return axisMappings;
    }

    public List<GamepadMapping<GamepadJoystick>> getJoystickMappings() {
        return joystickMappings;
    }

    public List<GamepadMapping<GamepadTrigger>> getTriggerMappings() {
        return triggerMappings;
    }

    public List<GamepadMapping<?>> getLeftSideMappings() {
        List<GamepadMapping<?>> mappings = new ArrayList<>();
        mappings.addAll(this.getButtonMappings());
        mappings.addAll(this.getAxisMappings());
        mappings.addAll(this.getTriggerMappings());
        mappings.addAll(this.getJoystickMappings());
        return mappings.stream().filter(mapping -> mapping.side() == GamepadMapping.Side.LEFT).toList();
    }

    public List<GamepadMapping<?>> getRightSideMappings() {
        List<GamepadMapping<?>> mappings = new ArrayList<>();
        mappings.addAll(this.getButtonMappings());
        mappings.addAll(this.getAxisMappings());
        mappings.addAll(this.getTriggerMappings());
        mappings.addAll(this.getJoystickMappings());
        return mappings.stream().filter(mapping -> mapping.side() == GamepadMapping.Side.RIGHT).toList();
    }

    @SuppressWarnings("unchecked")
    public <T extends GamepadMapping<?>> T register(T mapping) {
        Preconditions.checkNotNull(mapping, "mapping cannot be null");

        switch (mapping.action()) {
            case GamepadAction.Button ignored -> this.buttonMappings.add((GamepadMapping<GamepadButton>) mapping);
            case GamepadAction.Axis ignored -> this.axisMappings.add((GamepadMapping<GamepadAxis>) mapping);
            case GamepadAction.Joystick ignored -> this.joystickMappings.add((GamepadMapping<GamepadJoystick>) mapping);
            case GamepadAction.Trigger ignored -> this.triggerMappings.add((GamepadMapping<GamepadTrigger>) mapping);
            case null, default -> throw new IllegalArgumentException("Unsupported gamepad action: " + mapping.action().getClass().getName());
        }

        return mapping;
    }

    public List<GamepadMapping<?>> getAllMappings() {
        List<GamepadMapping<?>> mappings = new ArrayList<>();
        mappings.addAll(this.getButtonMappings());
        mappings.addAll(this.getAxisMappings());
        mappings.addAll(this.getTriggerMappings());
        mappings.addAll(this.getJoystickMappings());
        return mappings;
    }
}
