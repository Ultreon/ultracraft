package com.ultreon.craft.debug.inspect;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.debug.ValueTracker;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.LivingEntity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.entity.util.EntitySize;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.menu.Inventory;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.*;
import com.ultreon.libs.commons.v0.tuple.Pair;
import com.ultreon.libs.commons.v0.tuple.Quadruple;
import com.ultreon.libs.commons.v0.tuple.Quintuple;
import com.ultreon.libs.commons.v0.tuple.Triple;

import java.util.Collection;
import java.util.Map;

public class DefaultInspections {
    public static void register() {
        InspectionRoot.registerAutoFill(Entity.class, node -> {
            node.create("position", Entity::getBlockPos);
            node.create("health", Entity::getSize);
            node.create("isAffectedByFluid", Entity::isAffectedByFluid);
        });
        InspectionRoot.registerAutoFill(LivingEntity.class, node -> {
            node.create("health", LivingEntity::getHealth);
            node.create("maxHealth", LivingEntity::getMaxHeath);
            node.create("isInvincible", LivingEntity::isInvincible);
            node.create("isInWater", LivingEntity::isInWater);
        });
        InspectionRoot.registerAutoFill(Player.class, node -> {
            node.createNode("inventory", value -> value.inventory);
            node.create("isCrouching", Player::isCrouching);
            node.create("isAllowFlight", Player::isAllowFlight);
            node.create("isFlying", Player::isFlying);
            node.create("isSpectator", Player::isSpectator);
            node.create("selected", value -> value.selected);
            node.create("uuid", Player::getUuid);
        });
        InspectionRoot.registerAutoFill(Inventory.class, node -> {
            node.createNode("hotbar", value -> value.hotbar);
            node.createNode("inv", value -> value.inv);
        });
        InspectionRoot.registerAutoFill(ServerPlayer.class, node -> {
            node.createNode("cursor", ServerPlayer::getCursor);
            node.create("name", ServerPlayer::getName);
        });
        InspectionRoot.registerAutoFill(ItemStack.class, node -> {
            node.create("item", ItemStack::getItem);
            node.create("count", ItemStack::getCount);
        });
        InspectionRoot.registerAutoFill(Collection.class, node -> {
            node.create("size", Collection::size);
            node.createNode("items", Collection::toArray);
        });
        InspectionRoot.registerAutoFill(MapType.class, node -> {
            node.create("size", MapType::size);
            node.createNode("entries", mapType -> mapType.entries().toArray());
        });
        InspectionRoot.registerAutoFill(ListType.class, node -> {
            node.create("size", ListType::size);
            node.createNode("entries", mapType -> mapType.getValue().toArray());
        });
        InspectionRoot.registerAutoFill(ByteArrayType.class, node -> {
            node.create("size", ByteArrayType::size);
            node.createNode("entries", ByteArrayType::getValue);
        });
        InspectionRoot.registerAutoFill(ShortArrayType.class, node -> {
            node.create("size", ShortArrayType::size);
            node.createNode("entries", ShortArrayType::getValue);
        });
        InspectionRoot.registerAutoFill(IntArrayType.class, node -> {
            node.create("size", IntArrayType::size);
            node.createNode("entries", IntArrayType::getValue);
        });
        InspectionRoot.registerAutoFill(LongArrayType.class, node -> {
            node.create("size", LongArrayType::size);
            node.createNode("entries", LongArrayType::getValue);
        });
        InspectionRoot.registerAutoFill(FloatArrayType.class, node -> {
            node.create("size", FloatArrayType::size);
            node.createNode("entries", FloatArrayType::getValue);
        });
        InspectionRoot.registerAutoFill(DoubleArrayType.class, node -> {
            node.create("size", DoubleArrayType::size);
            node.createNode("entries", DoubleArrayType::getValue);
        });
        InspectionRoot.registerAutoFill(Map.class, node -> {
            node.create("size", Map::size);
            node.createNode("entries", mapType -> mapType.entrySet().toArray());
        });
        InspectionRoot.registerAutoFill(Map.Entry.class, node -> {
            try {
                node.create(String.valueOf(node.getValue().getKey()), Map.Entry::getValue);
            } catch (Throwable e) {
                node.create("failure", e::getMessage);
            }
        });
        InspectionRoot.registerAutoFill(World.class, node -> {
            node.createNode("loadedChunks", World::getLoadedChunks);
            node.create("seed", World::getSeed);
            node.create("totalChunks", World::getTotalChunks);
            node.create("chunksLoaded", World::getChunksLoaded);
            node.create("spawnPoint", World::getSpawnPoint);
        });
        InspectionRoot.registerAutoFill(ServerWorld.class, node -> {
            node.create("playTime", ServerWorld::getPlayTime);
            node.create("chunksToLoad", ServerWorld::getChunksToLoad);
            node.create("chunkLoads", ValueTracker::getChunkLoads);
            node.create("chunkUnloads", ServerWorld::getChunkUnloads);
            node.create("chunkSaves", ServerWorld::getChunkSaves);
        });
        InspectionRoot.registerFormatter(Item.class, element -> {
            var id = element.getId();
            return id == null ? null : element.getId().toString();
        });
        InspectionRoot.registerFormatter(Block.class, element -> {
            var id = element.getId();
            return id == null ? null : element.getId().toString();
        });
        InspectionRoot.registerFormatter(EntityType.class, element -> {
            var id = element.getId();
            return id == null ? null : element.getId().toString();
        });
        InspectionRoot.registerFormatter(EntitySize.class, element -> element.width() + "x" + element.height());
//        InspectionRoot.registerFormatter(Vec2f.class, element -> element.x + "," + element.y);
//        InspectionRoot.registerFormatter(Vec2d.class, element -> element.x + "," + element.y);
//        InspectionRoot.registerFormatter(Vec2i.class, element -> element.x + "," + element.y);
//        InspectionRoot.registerFormatter(Vec3f.class, element -> element.x + "," + element.y);
//        InspectionRoot.registerFormatter(Vec3d.class, element -> element.x + "," + element.y);
//        InspectionRoot.registerFormatter(Vec3i.class, element -> element.x + "," + element.y);
//        InspectionRoot.registerFormatter(Vec4f.class, element -> element.x + "," + element.y);
//        InspectionRoot.registerFormatter(Vec4d.class, element -> element.x + "," + element.y);
//        InspectionRoot.registerFormatter(Vec4i.class, element -> element.x + "," + element.y);
        InspectionRoot.registerFormatter(Pair.class, element -> element.getFirst() + "," + element.getSecond());
        InspectionRoot.registerFormatter(Triple.class, element -> element.getFirst() + "," + element.getSecond() + "," + element.getThird());
        InspectionRoot.registerFormatter(Quadruple.class, element -> element.getFirst() + "," + element.getSecond() + "," + element.getThird() + "," + element.getFourth());
        InspectionRoot.registerFormatter(Quintuple.class, element -> element.getFirst() + "," + element.getSecond() + "," + element.getThird() + "," + element.getFourth() + "," + element.getFifth());
        InspectionRoot.registerFormatter(Enum.class, Enum::name);
        InspectionRoot.registerFormatter(ByteType.class, byteType -> Byte.toString(byteType.getValue()));
        InspectionRoot.registerFormatter(ShortType.class, shortType -> Short.toString(shortType.getValue()));
        InspectionRoot.registerFormatter(IntType.class, intType -> Integer.toString(intType.getValue()));
        InspectionRoot.registerFormatter(LongType.class, longType -> Long.toString(longType.getValue()));
        InspectionRoot.registerFormatter(FloatType.class, floatType -> Float.toString(floatType.getValue()));
        InspectionRoot.registerFormatter(DoubleType.class, doubleType -> Double.toString(doubleType.getValue()));
        InspectionRoot.registerFormatter(BooleanType.class, booleanType -> Boolean.toString(booleanType.getValue()));
        InspectionRoot.registerFormatter(CharType.class, charType -> Character.toString(charType.getValue()));
        InspectionRoot.registerFormatter(StringType.class, stringType -> "\"" + stringType.getValue() + "\"");
        InspectionRoot.registerFormatter(UUIDType.class, uuidType -> uuidType.getValue().toString());
        InspectionRoot.registerFormatter(BitSetType.class, bitSetType -> bitSetType.getValue().toString());
    }
}
