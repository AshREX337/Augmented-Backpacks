// AugmentedMod.java - MODIFIED
package com.ashrex.augmented;

import com.ashrex.augmented.common.registry.ModAugments;
import com.ashrex.augmented.network.CraftItemPacket;
import com.ashrex.augmented.network.CraftItemPacketHandler;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(AugmentedMod.MOD_ID)
public class AugmentedMod
{
    public static final String MOD_ID = "augmented";
    public static final String MOD_NAME = "Augmented Backpacks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public AugmentedMod(IEventBus modEventBus, ModContainer modContainer)
    {
        LOGGER.info("Initializing {} - Adding crafting augment to Backpacked", MOD_NAME);

        // Register our augment type with Backpacked's registry
        ModAugments.AUGMENT_TYPES.register(modEventBus);

        LOGGER.info("Crafting augment registered");
    }

    public static Identifier rl(String path)
    {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @EventBusSubscriber(modid = MOD_ID)
    public static class ModEvents
    {
        @SubscribeEvent
        public static void registerPackets(RegisterPayloadHandlersEvent event)
        {
            System.out.println("========================================");
            System.out.println("=== REGISTERING PACKETS ===");
            System.out.println("========================================");

            LOGGER.info("=== REGISTERING PACKETS ===");

            final var registrar = event.registrar(AugmentedMod.MOD_ID);

            registrar.playToServer(
                    CraftItemPacket.TYPE,
                    CraftItemPacket.STREAM_CODEC,
                    CraftItemPacketHandler::handle
            );

            LOGGER.info("Registered CraftItemPacket to server with ID: {}", CraftItemPacket.TYPE.id());
            System.out.println("Registered packet: " + CraftItemPacket.TYPE.id());
        }
    }
}