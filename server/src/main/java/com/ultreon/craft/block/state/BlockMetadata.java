package com.ultreon.craft.block.state;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.item.tool.ToolType;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.BoundingBox;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.loot.LootGenerator;
import com.ultreon.data.types.MapType;

import java.util.Map;

public class BlockMetadata {
    public static final BlockMetadata AIR = Blocks.AIR.createMeta();
    private final Block block;
    private final Map<String, BlockDataEntry<?>> properties;

    public BlockMetadata(Block block, Map<String, BlockDataEntry<?>> properties) {
        this.block = block;
        this.properties = properties;
    }

    public static BlockMetadata load(int identifier, PacketBuffer packetBuffer) {
        Block block = Registries.BLOCK.byId(identifier);
        BlockMetadata meta = block.createMeta();
        meta.properties.clear();
        meta.properties.putAll(meta.loadProperties(packetBuffer));

        return meta;
    }

    public static BlockMetadata load(Identifier id, int x, int y, int z, MapType data) {
        Block block = Registries.BLOCK.getElement(id);
        BlockMetadata meta = block.createMeta();
        meta.properties.clear();
        meta.properties.putAll(meta.loadProperties(data.getMap("properties", new MapType())));

        return meta;
    }

    private Map<String, ? extends BlockDataEntry<?>> loadProperties(MapType data) {
        for (Map.Entry<String, ? extends BlockDataEntry<?>> entry : this.getProperties().entrySet()) {
            BlockDataEntry<?> property = entry.getValue().load(data.get(entry.getKey()));
            properties.put(entry.getKey(), property);
        }
        return properties;
    }

    private Map<String, BlockDataEntry<?>> loadProperties(PacketBuffer packetBuffer) {
        int size = packetBuffer.readVarInt();
        for (Map.Entry<String, BlockDataEntry<?>> entry : this.getProperties().entrySet()) {
            BlockDataEntry<?> property = entry.getValue().read(packetBuffer);
            properties.put(entry.getKey(), property);
            size--;

            if (size == 0) break;
        }
        return properties;
    }

    public Block getBlock() {
        return block;
    }

    public Map<String, BlockDataEntry<?>> getProperties() {
        return properties;
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <T> BlockDataEntry<T> getProperty(String name, T... typeGetter) {
        Class<T> type = (Class<T>) typeGetter.getClass().getComponentType();

        BlockDataEntry<?> property = properties.get(name);
        if (property == null)
            throw new IllegalArgumentException("Property " + name + " does not exist in block " + block);
        if (!type.isAssignableFrom(property.getClass()))
            throw new IllegalArgumentException("Property " + name + " is not of type " + type.getSimpleName() + " in block " + block);
        return property.cast(type);
    }

    public void setProperty(String name, BlockDataEntry<?> property) {
        if (!properties.containsKey(name))
            throw new IllegalArgumentException("Property " + name + " does not exist in block " + block);

        properties.put(name, property);
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public boolean isAir() {
        return block.isAir();
    }

    public MapType save() {
        MapType map = new MapType();
        for (Map.Entry<String, BlockDataEntry<?>> entry : properties.entrySet()) {
            map.put(entry.getKey(), entry.getValue().save());
        }
        return map;
    }

    public void onPlace(ServerWorld serverWorld, BlockPos blockPos) {
        this.block.onPlace(serverWorld, blockPos, this);
    }

    public boolean isWater() {
        return block == Blocks.WATER;
    }

    public int write(PacketBuffer encode) {
        encode.writeVarInt(Registries.BLOCK.getRawId(block));
        encode.writeVarInt(properties.size());
        for (Map.Entry<String, BlockDataEntry<?>> entry : properties.entrySet()) {
            entry.getValue().write(encode);
        }
        return properties.size();
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
}
