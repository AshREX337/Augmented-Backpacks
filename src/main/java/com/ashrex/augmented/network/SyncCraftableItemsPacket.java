package com.ashrex.augmented.network;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.client.ClientRecipeCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SyncCraftableItemsPacket(Map<Identifier, List<List<List<Identifier>>>> recipeIngredients) implements CustomPacketPayload
{
    public static final Type<SyncCraftableItemsPacket> TYPE =
            new Type<>(AugmentedMod.rl("sync_craftable_items"));

    public static final StreamCodec<FriendlyByteBuf, SyncCraftableItemsPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(
                            HashMap::new,
                            Identifier.STREAM_CODEC,
                            Identifier.STREAM_CODEC
                                    .apply(ByteBufCodecs.list())
                                    .apply(ByteBufCodecs.list())
                                    .apply(ByteBufCodecs.list())
                    ),
                    SyncCraftableItemsPacket::recipeIngredients,
                    SyncCraftableItemsPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(SyncCraftableItemsPacket msg, IPayloadContext ctx)
    {
        ctx.enqueueWork(() -> ClientRecipeCache.update(msg.recipeIngredients()));
    }
}