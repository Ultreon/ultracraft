package com.ultreon.craft.block.state;

import com.ultreon.craft.UnsafeApi;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.UseItemContext;
import com.ultreon.craft.item.tool.ToolType;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.BoundingBox;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.CubicDirection;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.loot.LootGenerator;
import com.ultreon.data.types.MapType;
import io.netty.handler.codec.DecoderException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BlockMetadata {
    public static final BlockMetadata AIR = Blocks.AIR.createMeta();
    private final Block block;
    private final Map<String, BlockDataEntry<?>> entries;

    public BlockMetadata(Block block, Map<String, BlockDataEntry<?>> entries) {
        this.block = block;
        this.entries = entries;
    }

    public static BlockMetadata read(PacketBuffer packetBuffer) {
        int rawId = packetBuffer.readVarInt();
        Block block = Registries.BLOCK.byId(rawId);
        if (block == null)
            throw new DecoderException("Block " + rawId + " does not exist");

        BlockMetadata meta = block.createMeta();
        meta.entries.putAll(meta.readEntries(packetBuffer));

        return meta;
    }

    private Map<String, BlockDataEntry<?>> readEntries(PacketBuffer packetBuffer) {
        int size = packetBuffer.readMedium();
        for (int i = 0; i < size; i++) {
            String key = packetBuffer.readString(64);
            BlockDataEntry<?> blockDataEntry = this.entries.get(key);
            if (blockDataEntry == null)
                throw new DecoderException("Entry " + key + " does not exist in block " + block);

            BlockDataEntry<?> property = blockDataEntry.read(packetBuffer);
            entries.put(key, property);
        }
        return entries;
    }

    public int write(PacketBuffer encode) {
        encode.writeVarInt(Registries.BLOCK.getRawId(block));
        encode.writeMedium(entries.size());
        for (Map.Entry<String, BlockDataEntry<?>> entry : entries.entrySet()) {
            encode.writeUTF(entry.getKey(), 64);
            entry.getValue().write(encode);
        }
        return entries.size();
    }

    public static BlockMetadata load(MapType data) {
        Block block = Registries.BLOCK.get(Identifier.parse(data.getString("block")));
        BlockMetadata meta = block.createMeta();
        meta.entries.clear();
        meta.entries.putAll(meta.loadEntries(data.getMap("entries", new MapType())));

        return meta;
    }

    private Map<String, ? extends BlockDataEntry<?>> loadEntries(MapType data) {
        for (Map.Entry<String, ? extends BlockDataEntry<?>> entry : this.getEntries().entrySet()) {
            BlockDataEntry<?> property = entry.getValue().load(data.get(entry.getKey()));
            entries.put(entry.getKey(), property);
        }
        return entries;
    }

    public Block getBlock() {
        return block;
    }


    public Map<String, BlockDataEntry<?>> getEntries() {
        return entries;
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <T> BlockDataEntry<T> getEntry(String name, T... typeGetter) {
        Class<T> type = (Class<T>) typeGetter.getClass().getComponentType();

        BlockDataEntry<?> property = entries.get(name);
        if (property == null)
            throw new IllegalArgumentException("Entry '" + name + "' does not exist in block " + block);
        if (!type.isAssignableFrom(property.value.getClass()))
            throw new IllegalArgumentException("Entry '" + name + "' is not of type " + type.getSimpleName() + " in block " + block);
        return property.cast(type);
    }

    @UnsafeApi
    public final BlockDataEntry<?> getEntryUnsafe(String name) {
        BlockDataEntry<?> property = entries.get(name);
        if (property == null)
            throw new IllegalArgumentException("Entry " + name + " does not exist in block " + block);
        return property;
    }

    public void setEntry(String name, BlockDataEntry<?> entry) {
        if (!entries.containsKey(name))
            throw new IllegalArgumentException("Entry " + name + " does not exist in block " + block);

        entries.put(name, entry);
    }

    public boolean hasEntry(String name) {
        return entries.containsKey(name);
    }

    public boolean isAir() {
        return block.isAir();
    }

    public MapType save() {
        MapType map = new MapType();
        Identifier id = Registries.BLOCK.getId(block);
        if (id == null)
            throw new IllegalArgumentException("Block " + block + " isn't registered");

        map.putString("block", id.toString());

        MapType entriesData = new MapType();
        for (Map.Entry<String, BlockDataEntry<?>> entry : this.entries.entrySet()) {
            map.put(entry.getKey(), entry.getValue().save());
        }

        map.put("Entries", entriesData);
        return map;
    }

    public void onPlace(ServerWorld serverWorld, BlockPos blockPos) {
        this.block.onPlace(serverWorld, blockPos, this);
    }

    public boolean isWater() {
        return block == Blocks.WATER;
    }

    public boolean hasCollider() {
        return block.hasCollider();
    }

    public boolean isFluid() {
        return block.isFluid();
    }

    public BoundingBox getBoundingBox(int x, int y, int z) {
        return block.getBoundingBox(x, y, z, this);
    }

    public boolean isReplaceable() {
        return block.isReplaceable();
    }

    public ToolType getEffectiveTool() {
        return block.getEffectiveTool();
    }

    public float getHardness() {
        return block.getHardness();
    }

    public boolean isToolRequired() {
        return block.isToolRequired();
    }

    public LootGenerator getLootGen() {
        return block.getLootGen(this);
    }

    public boolean isTransparent() {
        return block.isTransparent();
    }

    public <T> BlockMetadata withEntry(String name, BlockDataEntry<T> value) {
        HashMap<String, BlockDataEntry<?>> entries = new HashMap<>(this.entries);
        entries.put(name, value);
        return new BlockMetadata(block, entries);
    }

    @SuppressWarnings("unchecked")
    public <T> BlockMetadata withEntry(String name, T value) {
        HashMap<String, BlockDataEntry<?>> entries = new HashMap<>(this.entries);
        entries.put(name, entries.get(name).cast((Class<T>) value.getClass()).with(value));
        return new BlockMetadata(block, entries);
    }

    @Override
    public String toString() {
        return block.getId() + " #" + entries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockMetadata that = (BlockMetadata) o;
        return Objects.equals(block, that.block) && Objects.equals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, entries);
    }

    public void update(World serverWorld, BlockPos offset) {
        this.block.update(serverWorld, offset, this);
    }

    public boolean canBeReplacedBy(UseItemContext context) {
        return block.canBeReplacedBy(context, this);
    }
}
