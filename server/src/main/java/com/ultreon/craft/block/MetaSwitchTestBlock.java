package com.ultreon.craft.block;

import com.ultreon.craft.block.state.BlockDataEntry;
import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.UseResult;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public class MetaSwitchTestBlock extends Block {
    public MetaSwitchTestBlock() {

    }

    @Override
    public UseResult use(@NotNull World world, @NotNull Player player, @NotNull Item item, @NotNull BlockPos pos) {
        BlockMetadata metadata = world.get(pos);
        BlockDataEntry<Boolean> test = metadata.getEntry("on");
        metadata = metadata.withEntry("on", test.map(b -> !b));

        world.set(pos, metadata);

        return UseResult.ALLOW;
    }

    @Override
    public BlockMetadata createMeta() {
        return super.createMeta().withEntry("on", BlockDataEntry.of(false));
    }
}
