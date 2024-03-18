package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandResult;
import com.ultreon.craft.api.ubo.UboFormatter;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.chat.Chat;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.Location;
import com.ultreon.craft.world.ServerChunk;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.data.DataIo;
import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ChunkCommand extends Command {
    public ChunkCommand() {
        this.requirePermission("ultracraft.commands.kill");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("chunk", "debugChunk");
    }

    @DefineCommand("dump-data")
    public @Nullable CommandResult executeDumpData(CommandSender sender, CommandContext commandContext, String alias) {
        Location location = sender.getLocation();
        if (location == null) return this.errorMessage("Failed to get location");

        ServerWorld serverWorld = location.getSeverWorld();
        UltracraftServer server = serverWorld.getServer();

        server.execute(() -> {
            Chunk chunkAt = serverWorld.getChunkAt(location.getBlockPos());
            if (chunkAt instanceof ServerChunk serverChunk) {
                MapType save = serverChunk.save();

                try {
                    Files.createDirectories(Path.of("debug/chunks"));
                    DataIo.writeCompressed(save, Path.of("debug/chunks/" + location.getBlockPos() + ".ubo").toFile());
                    String uso = DataIo.toUso(save);
                    Files.writeString(Path.of("debug/chunks/" + location.getBlockPos() + ".uso"), uso, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException e) {
                    Chat.sendError(sender, "Failed to save chunk data: " + e.getMessage());
                }
            }

            Chat.sendSuccess(sender, "Saved chunk data at " + location.getBlockPos());
        });

        return infoMessage("Saving chunk data at " + location.getBlockPos());
    }
}
