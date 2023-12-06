package com.ultreon.craft.entity;

import com.google.common.base.Preconditions;
import com.ultreon.craft.entity.util.EntitySize;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public abstract class EntityType<T extends Entity> {
    private final EntitySize size;

    private EntityType(Builder<T> properties) {
        this.size = new EntitySize(properties.width, properties.height);
    }

    public abstract T create(World world);

    public T spawn(World world) {
        T t = this.create(world);
        world.spawn(t, new MapType());
        return t;
    }

    public T spawn(World world, MapType spawnData) {
        T t = this.create(world);
        world.spawn(t, spawnData);
        return t;
    }

    public EntitySize getSize() {
        return this.size;
    }

    public @Nullable Identifier getId() {
        return Registries.ENTITY_TYPE.getKey(this);
    }

    public static class Builder<T extends Entity> {
        private float width = 0.8f;
        private float height = 1.9f;
        @Nullable
        private EntityFactory<T> factory;

        public Builder() {

        }

        @Contract("_,_->this")
        public Builder<T> size(float width, float height) {
            this.width = width;
            this.height = height;
            return this;
        }

        @Contract("_->this")
        public Builder<T> factory(EntityFactory<T> factory) {
            this.factory = factory;
            return this;
        }

        public EntityType<T> build() {
            Preconditions.checkNotNull(this.factory, "Entity factory is not set");

            return new EntityType<>(this) {
                @Override
                public T create(World world) {
                    return Builder.this.factory.create(this, world);
                }
            };
        }
    }

    @FunctionalInterface
    public interface EntityFactory<T extends Entity> {
        T create(EntityType<T> type, World world);
    }
}
