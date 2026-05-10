package com.ashrex.augmented.network;

import com.ashrex.augmented.AugmentedMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record SyncCraftableItemsPacket(List<Identifier> recipeIds) implements CustomPacketPayload
{
    public static final Type<SyncCraftableItemsPacket> TYPE =
            new Type<>(AugmentedMod.rl("sync_craftable_items"));

    public static final StreamCodec<FriendlyByteBuf, SyncCraftableItemsPacket> STREAM_CODEC =
            StreamCodec.composite(
                    Identifier.STREAM_CODEC.apply(net.minecraft.network.codec.ByteBufCodecs.list()),
                    SyncCraftableItemsPacket::recipeIds,
                    SyncCraftableItemsPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(SyncCraftableItemsPacket msg, IPayloadContext ctx)
    {
        ctx.enqueueWork(() -> {
            // CLIENT SIDE ONLY CODE HERE
            // Example: update your client cache safely
            com.ashrex.augmented.client.ClientRecipeCache.update(msg.recipeIds());
        });
    }
}