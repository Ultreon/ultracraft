package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandOutput;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.ServerChunk;
import com.ultreon.craft.world.World;
import com.ultreon.data.DataIo;
import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class DumpChunkCommand extends Command {
    private static final File DEBUG_DUMP_DIR = new File("debug/dump_chunk/");

    public DumpChunkCommand() {
        this.requirePermission("ultracraft.commands.dumpchunk");
        this.setCategory(CommandCategory.EDIT);
        this.data().aliases("dumpchunk");
    }

    @SubCommand("<string:filename>")
    public @Nullable CommandOutput execute(CommandSender sender, CommandContext commandContext, String alias, String filename) {
        if (!(sender instanceof Player player)) {
            return needPlayer();
        }

        World world = player.getWorld();
        ChunkPos pos = player.getChunkPos();
        Chunk chunk = world.getChunk(pos);

        if (chunk == null) {
            return this.errorMessage("Failed to get chunk at %s".formatted(pos));
        }

        if (chunk instanceof ServerChunk serverChunk) {
            MapType save = serverChunk.save();
            if (!DEBUG_DUMP_DIR.exists() && !DEBUG_DUMP_DIR.mkdirs()) {
                return this.errorMessage("Failed to create " + DEBUG_DUMP_DIR.getAbsolutePath() + " directory");
            }

            File file = new File(DEBUG_DUMP_DIR, filename + ".ubo");
            try {
                DataIo.writeCompressed(save, file);
            } catch (IOException e) {
                return this.errorMessage("Failed to save chunk to %s".formatted(file.getAbsolutePath()));
            }

            return this.successMessage("Saved chunk to %s".formatted(file.getAbsolutePath()));
        }

        return this.errorMessage("Failed to get chunk at %s".formatted(pos));
    }
}
