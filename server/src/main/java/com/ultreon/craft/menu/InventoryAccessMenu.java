package com.ultreon.craft.menu;

import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class InventoryAccessMenu extends ContainerMenu {
    public final ItemSlot[] hotbar = new ItemSlot[9];
    public final ItemSlot[][] inv = new ItemSlot[9][3];

    /**
     * Creates a new {@link InventoryAccessMenu}
     *
     * @param type   the type of the menu.
     * @param world  the world where the menu is opened in.
     * @param entity the entity that opened the menu.
     * @param pos    the position where the menu is opened.
     * @param size   the number of slots.
     */
    protected InventoryAccessMenu(@NotNull MenuType<?> type, @NotNull World world, @NotNull Entity entity, @Nullable BlockPos pos, int size) {
        super(type, world, entity, pos, size);
    }

    protected int inventoryMenu(int idx, int offX, int offY) {
        if (getEntity() instanceof Player player) {
            for (int x = 0; x < 9; x++) {
                this.hotbar[x] = this.addSlot(new RedirectItemSlot(idx++, player.inventory.hotbar[x], offX + x * 19 + 6, offY + 83));
            }

            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 3; y++) {
                    this.inv[x][y] = this.addSlot(new RedirectItemSlot(idx++, player.inventory.inv[x][y], offX + x * 19 + 6,  offY + y * 19 + 6));
                }
            }
        }

        return idx;
    }
}
