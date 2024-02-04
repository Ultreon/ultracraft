package com.ultreon.craft.entity;

import com.ultreon.libs.functions.v0.BiDouble2DoubleFunction;

import java.util.UUID;

public record AttributeModifier(UUID id, com.ultreon.craft.entity.AttributeModifier.Operation operation, double value) {

    public enum Operation {
        PLUS((a, b) -> a + b),
        MULTIPLY((a, b) -> a * b);

        private final BiDouble2DoubleFunction function;

        Operation(BiDouble2DoubleFunction function) {
            this.function = function;
        }

        public double calculate(double a, double b) {
            return this.function.apply(a, b);
        }
    }
}
