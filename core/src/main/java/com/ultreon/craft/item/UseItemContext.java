package com.ultreon.craft.item;

import com.ultreon.craft.entity.Player;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.world.World;

public record UseItemContext(World world, Player player, HitResult result) {

}
