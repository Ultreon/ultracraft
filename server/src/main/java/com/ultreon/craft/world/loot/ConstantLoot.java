package com.ultreon.craft.world.loot;

import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.world.rng.RNG;

import java.util.List;

public class ConstantLoot implements LootGenerator {
    public static final LootGenerator EMPTY = new ConstantLoot();
    private final List<ItemStack> loot;

    public ConstantLoot(ItemStack... loot) {
        this.loot = List.of(loot);
    }

    public ConstantLoot(List<ItemStack> loot) {
        this.loot = loot;
    }

    @Override
    public Iterable<ItemStack> generate(RNG random) {
        return this.loot;
    }
}
