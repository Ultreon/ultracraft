package com.ultreon.craft.menu;

import com.ultreon.craft.block.entity.BlockEntity;
import com.ultreon.craft.block.entity.CrateBlockEntity;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrateMenu extends BlockContainerMenu {
    public final ItemSlot[][] crate = new ItemSlot[9][3];
    private final CrateBlockEntity blockEntity;

    /**
     * Creates a new {@link CrateMenu}
     *
     * @param type   the type of the menu.
     * @param world  the world where the menu is opened in.
     * @param entity the entity that opened the menu.
     * @param pos    the position where the menu is opened.
     */
    public CrateMenu(@NotNull MenuType<?> type, @NotNull World world, @NotNull Entity entity, @Nullable BlockPos pos) {
        this(type, world, entity, CrateMenu.getBlockEntity(world, pos), pos);
    }

    /**
     * Returns the CrateBlockEntity at the specified position in the world.
     *
     * @param world the world in which to search for the CrateBlockEntity
     * @param pos   the position of the CrateBlockEntity
     * @return the CrateBlockEntity at the specified position, or null if not found
     */
    private static CrateBlockEntity getBlockEntity(@NotNull World world, @Nullable BlockPos pos) {
        if (pos == null) return null;
        BlockEntity blockEntity1 = world.getBlockEntity(pos);
        if (!(blockEntity1 instanceof CrateBlockEntity crate)) return null;
        return crate;
    }

    /**
     * Creates a new {@link CrateMenu}
     *
     * @param type   the type of the menu.
     * @param world  the world where the menu is opened in.
     * @param entity the entity that opened the menu.
     * @param pos    the position where the menu is opened.
     */
    public CrateMenu(@NotNull MenuType<?> type, @NotNull World world, @NotNull Entity entity, @Nullable CrateBlockEntity blockEntity, @Nullable BlockPos pos) {
        super(type, world, entity, blockEntity, pos, 63);

        this.blockEntity = blockEntity;

        this.build();
    }

    @Override
    public void build() {
        int idx = 0;
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                this.crate[x][y] = this.addSlot(new ItemSlot(idx++, this, this.blockEntity.get(idx - 1), x * 19 + 6, y * 19 + 6));
            }
        }


        this.inventoryMenu(idx, 0, 76);
    }

    @Override
    protected void onItemChanged(ItemSlot slot) {
        super.onItemChanged(slot);

        int index = slot.getIndex();
        if (index >= 27) return;
        CrateBlockEntity crate = getBlockEntity();
        if (crate == null) return;

        crate.set(index, slot.getItem());
    }

    public @Nullable CrateBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public void setupClient(List<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            ItemSlot slot = this.slots[i];
            if (slot == null) continue;
            slot.setItem(items.get(i), false);
        }
    }
}
