package com.ashrex.augmented.network;

import com.ashrex.augmented.AugmentedMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = AugmentedMod.MOD_ID)
public class ModNetworking
{
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event)
    {
        var registrar = event.registrar(AugmentedMod.MOD_ID);

        registrar.playToClient(
                SyncCraftableItemsPacket.TYPE,
                SyncCraftableItemsPacket.STREAM_CODEC,
                SyncCraftableItemsPacket::handle
        );
    }
}