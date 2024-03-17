package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandResult;
import com.ultreon.craft.api.ubo.UboFormatter;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.IType;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SummonCommand extends Command {
    public SummonCommand() {
        this.requirePermission("ultracraft.commands.kill");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("summon", "spawn");
    }

    @DefineCommand("<entity-type> <position> <world>")
    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, EntityType<?> entityType, Vec3d position, World world) {
        Entity entity = entityType.create(world);
        entity.setPosition(position);

        Entity spawn = world.spawn(entity);
        if (spawn == null) {
            return errorMessage("Failed to spawn " + entity.getName() + " at " + position);
        }

        return successMessage("Spawned " + entity.getName() + " at " + position);
    }

    @DefineCommand("<entity-type> <position> <world> using <ubo:spawn-data>")
    public @Nullable CommandResult executeUsingSpawnData(CommandSender sender, CommandContext commandContext, String alias, EntityType<?> entityType, Vec3d position, World world, IType<?> ubo) {
        Entity entity = entityType.create(world);
        entity.setPosition(position);

        if (ubo instanceof MapType) {
            Entity spawn = world.spawn(entity, (MapType) ubo);
            if (spawn == null) {
                return errorMessage("Failed to spawn " + entity.getName() + " at " + UboFormatter.format(ubo));
            }
        } else {
            return errorMessage("Invalid UBO type: " + ubo.getClass().getSimpleName());
        }

        return successMessage("Spawned " + entity.getName() + " at " + UboFormatter.format(ubo));
    }

    @DefineCommand("<entity-type> <position> <world> with <ubo:data>")
    public @Nullable CommandResult executeWithData(CommandSender sender, CommandContext commandContext, String alias, EntityType<?> entityType, Vec3d position, World world, IType<?> ubo) {
        Entity entity = entityType.create(world);
        entity.setPosition(position);

        if (ubo instanceof MapType) {
            entity.load((MapType) ubo);
        } else {
            return errorMessage("Invalid UBO type: " + ubo.getClass().getSimpleName());
        }

        Entity spawn = world.spawn(entity);
        if (spawn == null) {
            return errorMessage("Failed to spawn " + entity.getName() + " at " + UboFormatter.format(ubo));
        }

        return successMessage("Spawned " + entity.getName() + " at " + UboFormatter.format(ubo));
    }
}
