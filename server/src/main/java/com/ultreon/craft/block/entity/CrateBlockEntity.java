package com.ultreon.craft.block.entity;

import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;

public class CrateBlockEntity extends BlockEntity {
    private final ItemStack[] items = new ItemStack[9 * 3];

    public CrateBlockEntity(BlockEntityType<?> type, World world, BlockPos pos) {
        super(type, world, pos);

        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.empty();
        }
    }

    @Override
    public void load(MapType data) {
        super.load(data);

        int i = 0;
        for (MapType mapType : data.<MapType>getList("Items")) {
            items[i] = ItemStack.load(mapType);
        }
    }

    @Override
    public MapType save(MapType data) {
        ListType<MapType> itemData = new ListType<>();
        for (ItemStack stack : this.items) {
            itemData.add(stack.save());
        }

        return super.save(data);
    }

    public ItemStack get(int slot) {
        return items[slot];
    }

    public void set(int slot, ItemStack item) {
        items[slot] = item;
    }

    public ItemStack remove(int slot) {
        ItemStack item = items[slot];
        items[slot] = ItemStack.empty();
        return item;
    }

    public ItemStack get(int x, int y) {
        return get(y * 3 + x);
    }

    public void set(int x, int y, ItemStack item) {
        set(y * 3 + x, item);
    }

    public ItemStack remove(int x, int y) {
        return remove(y * 3 + x);
    }

    public void open(Player player) {
        // TODO: Crate menu
    }
}
