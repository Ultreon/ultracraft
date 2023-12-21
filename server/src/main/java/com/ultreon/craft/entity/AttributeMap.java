package com.ultreon.craft.entity;

import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import org.apache.commons.collections4.map.DefaultedMap;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AttributeMap {
    private final Map<Attribute, @Nullable Double> bases = new Reference2DoubleArrayMap<>();
    private final DefaultedMap<Attribute, Map<UUID, AttributeModifier>> modifiers = new DefaultedMap<>(input -> new HashMap<>());

    public void setBase(Attribute attribute, double base) {
        this.bases.put(attribute, base);
    }

    public double getBase(Attribute attribute) {
        Double v = this.bases.get(attribute);
        return v != null ? v : 0;
    }

    public void addModifier(Attribute attribute, AttributeModifier modifier) {
        this.modifiers.get(attribute).put(modifier.id(), modifier);
    }

    public AttributeModifier removeModifier(Attribute attribute, UUID uuid) {
        return this.modifiers.get(attribute).remove(uuid);
    }

    public double get(Attribute attribute) {
        Double value = this.bases.get(attribute);
        if (value == null) throw new IllegalStateException("Attribute not set: " + attribute.key());

        for (AttributeModifier modifier : this.modifiers.get(attribute).values().stream().sorted(Comparator.comparing(modifier -> modifier.operation().ordinal())).toList()) {
            AttributeModifier.Operation operation = modifier.operation();
            value = operation.calculate(value, modifier.value());
        }

        return value;
    }
}
