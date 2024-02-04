package com.ultreon.craft.entity;

import com.google.common.base.Preconditions;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec3d;

public class DroppedItem extends Entity {
    private ItemStack stack;
    private int age;

    public DroppedItem(EntityType<? extends Entity> entityType, World world) {
        super(entityType, world);

        this.stack = new ItemStack();
    }

    public DroppedItem(World world, ItemStack stack, Vec3d position, Vec3d velocity) {
        super(EntityTypes.DROPPED_ITEM, world);
        this.stack = stack;
        this.setPosition(position);
        this.setVelocity(velocity);
    }

    @Override
    public void tick() {
        super.tick();
        this.age++;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public void setStack(ItemStack stack) {
        Preconditions.checkNotNull(stack, "Stack cannot be null");
        this.stack = stack;
    }

    public int getAge() {
        return this.age;
    }

    @Override
    public MapType save(MapType data) {
        super.save(data);
        data.put("Item", this.stack.save());
        data.putInt("age", this.age);
        return data;
    }

    @Override
    public void load(MapType data) {
        super.load(data);
        this.stack = ItemStack.load(data.getMap("Item"));
        this.age = data.getInt("age");
    }
}
