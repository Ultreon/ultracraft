package com.ultreon.craft.item.material;

public interface ItemMaterial {
    static Builder builder() {
        return new Builder();
    }

    float getEfficiency();

    class Builder {
        private float efficiency;

        public Builder efficiency(float efficiency) {
            this.efficiency = efficiency;
            return this;
        }

        public ItemMaterial build() {
            return () -> Builder.this.efficiency;
        }
    }
}
