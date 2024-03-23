package com.ultreon.craft.world.loot;

import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.world.rng.RNG;

public interface LootGenerator {
    Iterable<ItemStack> generate(RNG random);
}
