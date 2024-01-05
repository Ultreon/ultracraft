package com.ultreon.craft.world.loot;

import com.google.common.collect.ImmutableList;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.world.rng.RandomSource;
import com.ultreon.data.types.MapType;
import org.apache.commons.lang3.IntegerRange;

import java.util.List;

public class RandomLoot implements LootGenerator {
    private final List<LootEntry> entries;

    public RandomLoot(LootEntry... entries) {
        this.entries = List.of(entries);
    }

    @Override
    public Iterable<ItemStack> generate(RandomSource random) {
        var items = new ImmutableList.Builder<ItemStack>();

        for (var entry : this.entries) {
            int count = entry.randomCount(random);
            items.add(new ItemStack(entry.item(), count, entry.data()));
        }

        return items.build();
    }

    public List<LootEntry> getEntries() {
        return this.entries;
    }

    public interface LootEntry {
        int randomCount(RandomSource random);

        Item item();

        MapType data();
    }

    public record CountLootEntry(IntegerRange range, Item item, MapType data) implements LootEntry {
        public CountLootEntry(IntegerRange range, Item item, MapType data) {
            this.range = range;
            this.item = item;

            this.data = data == null ? new MapType() : data;
        }

        public CountLootEntry(IntegerRange range, Item rock) {
            this(range, rock, new MapType());
        }

        @Override
        public int randomCount(RandomSource random) {
            return random.randint(this.range.getMinimum(), this.range.getMaximum());
        }
    }

    public record ChanceLootEntry(float chance, Item item, MapType data) implements LootEntry {
        public ChanceLootEntry(float chance, Item item, MapType data) {
            this.chance = chance;
            this.item = item;

            this.data = data == null ? new MapType() : data;
        }

        public ChanceLootEntry(float chance, Item rock) {
            this(chance, rock, new MapType());
        }

        @Override
        public int randomCount(RandomSource random) {
            return random.chance(this.chance) ? 1 : 0;
        }
    }
}
