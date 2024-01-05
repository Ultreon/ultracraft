package com.ultreon.craft.world.loot;

import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.world.rng.RandomSource;

public interface LootGenerator {
    Iterable<ItemStack> generate(RandomSource random);
}
