package com.ultreon.craft.menu;

import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateMenu extends InventoryAccessMenu {
    public final ItemSlot[][] crate = new ItemSlot[9][3];

    /**
     * Creates a new {@link CrateMenu}
     *
     * @param type   the type of the menu.
     * @param world  the world where the menu is opened in.
     * @param entity the entity that opened the menu.
     * @param pos    the position where the menu is opened.
     */
    public CrateMenu(@NotNull MenuType<?> type, @NotNull World world, @NotNull Entity entity, @Nullable BlockPos pos) {
        super(type, world, entity, pos, 76);

        this.build();
    }

    @Override
    public void build() {
        int idx = 0;
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                this.crate[x][y] = this.addSlot(new ItemSlot(idx++, this, new ItemStack(), x * 19 + 6, y * 19 + 6));
            }
        }


        this.inventoryMenu(idx,0, 76);
    }
}
