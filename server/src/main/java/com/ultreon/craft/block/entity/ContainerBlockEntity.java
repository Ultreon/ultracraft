package com.ultreon.craft.block.entity;

import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.menu.ContainerMenu;
import com.ultreon.craft.menu.CrateMenu;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.NotNull;

public abstract class ContainerBlockEntity<T extends ContainerMenu> extends BlockEntity {
    private final ItemStack[] items;
    private T menu;

    public ContainerBlockEntity(BlockEntityType<?> type, World world, BlockPos pos, int itemCapacity) {
        super(type, world, pos);

        this.items = new ItemStack[itemCapacity];

        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.empty();
        }
    }

    public T getMenu() {
        return menu;
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

        data.put("Items", itemData);

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
        if (this.menu != null) {
            player.openMenu(this.menu);
            return;
        }

        this.menu = createMenu(player);
        player.openMenu(this.menu);
    }

    @NotNull
    public abstract T createMenu(Player player);

    public void onGainedViewer(Player player, CrateMenu menu) {
        // Implementation purposes
    }

    public void onLostViewer(Player player, CrateMenu menu) {
        if (this.menu != menu) return;

        if (this.menu.isOnItsOwn()) {
            this.menu = null;
        }
    }
}
