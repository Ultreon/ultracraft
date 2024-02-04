package com.ultreon.craft.gamerule;

import com.ultreon.craft.api.commands.CommandExecuteException;
import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GameRules {
    private final List<Rule<?>> rules = new ArrayList<>();

    public Rule<Double> numberRule(String key, double value) {
        NumberRule rule = new NumberRule(key, value);
        this.rules.add(rule);
        return rule;
    }

    public Rule<Boolean> booleanRule(String key, boolean value) {
        BooleanRule rule = new BooleanRule(key, value);
        this.rules.add(rule);
        return rule;
    }

    public <T extends Enum<T>> Rule<T> enumRule(String key, T value) {
        EnumRule<T> rule = new EnumRule<>(key, value);
        this.rules.add(rule);
        return rule;
    }

    public List<Rule<?>> getRules() {
        return Collections.unmodifiableList(this.rules);
    }

    public @Nullable Rule<?> getRule(String name) {
        return this.rules.stream().filter(rule -> Objects.equals(rule.getKey(), name)).findFirst().orElse(null);
    }

    private static class NumberRule implements Rule<Double> {
        private final String key;
        private Double value;
        private final Double def;

        public NumberRule(String key, Double value) {
            this.key = key;
            this.value = value;
            this.def = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public Double getValue() {
            return this.value;
        }

        @Override
        public Double getDefault() {
            return this.def;
        }

        @Override
        public String getStringValue() {
            return String.valueOf(this.value);
        }

        @Override
        public void setStringValue(String value) throws CommandExecuteException {
            try {
                this.value = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new CommandExecuteException("Invalid 64-bit float value: " + value);
            }
        }

        @Override
        public void setValue(Double value) {
            this.value = value;
        }
    }

    private static class BooleanRule implements Rule<Boolean> {
        private final String key;
        private Boolean value;
        private final Boolean def;

        public BooleanRule(String key, Boolean value) {
            this.key = key;
            this.value = value;
            this.def = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public Boolean getValue() {
            return this.value;
        }

        @Override
        public Boolean getDefault() {
            return this.def;
        }

        @Override
        public String getStringValue() {
            return String.valueOf(this.value);
        }

        @Override
        public void setValue(Boolean value) {
            this.value = value;
        }

        @Override
        public void setStringValue(String value) throws CommandExecuteException {
            switch (value) {
                case "true" -> this.value = true;
                case "false" -> this.value = false;
                default -> throw new CommandExecuteException("Invalid boolean value: " + value);
            }
        }
    }

    private static class EnumRule<E extends Enum<E>> implements Rule<E> {
        private final String key;
        private final Class<? extends Enum> enumClass;
        private E value;
        private final E def;

        public EnumRule(String key, E value) {
            this.key = key;
            this.value = value;
            this.def = value;
            this.enumClass = value.getClass();
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public E getValue() {
            return this.value;
        }

        @Override
        public E getDefault() {
            return this.def;
        }

        @Override
        public String getStringValue() {
            return String.valueOf(this.value);
        }

        @Override
        public void setValue(E value) {
            this.value = value;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void setStringValue(String value) throws CommandExecuteException {
            E val = (E) EnumUtils.getEnum(this.enumClass, value.toLowerCase().replace("-", "_"));
            if (val == null) throw new CommandExecuteException("Unknown value: " + value);
            this.value = val;
        }
    }
}