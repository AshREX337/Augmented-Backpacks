package com.ashrex.augmented.network;

import com.ashrex.augmented.AugmentedMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CraftItemPacket(Identifier itemId) implements CustomPacketPayload
{
    public static final Type<CraftItemPacket> TYPE =
            new Type<>(AugmentedMod.rl("craft_item"));

    public static final StreamCodec<FriendlyByteBuf, CraftItemPacket> STREAM_CODEC =
            StreamCodec.composite(
                    Identifier.STREAM_CODEC,
                    CraftItemPacket::itemId,
                    CraftItemPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}