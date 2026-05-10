// SyncCraftableItemsPacketHandler.java
package com.ashrex.augmented.network;

import com.ashrex.augmented.client.ClientRecipeCache;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncCraftableItemsPacketHandler
{
    public static void handle(SyncCraftableItemsPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> ClientRecipeCache.update(packet.recipeIds()));
    }
}