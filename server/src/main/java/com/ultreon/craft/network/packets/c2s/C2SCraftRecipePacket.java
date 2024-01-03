package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;
import com.ultreon.craft.recipe.Recipe;
import com.ultreon.craft.recipe.RecipeType;
import com.ultreon.libs.commons.v0.Identifier;

public class C2SCraftRecipePacket extends Packet<InGameServerPacketHandler> {
    private final int typeId;
    private final Identifier recipeId;

    public C2SCraftRecipePacket(RecipeType type, Recipe recipe) {
        this.typeId = type.getId();
        this.recipeId = recipe.getId();
    }

    public C2SCraftRecipePacket(PacketBuffer buffer) {
        this.typeId = buffer.readInt();
        this.recipeId = buffer.readId();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(this.typeId);
        buffer.writeId(this.recipeId);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onCraftRecipe(this.typeId, this.recipeId);
    }
}
