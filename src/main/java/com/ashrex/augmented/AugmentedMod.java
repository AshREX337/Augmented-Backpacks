// AugmentedMod.java - MODIFIED
package com.ashrex.augmented;

import com.ashrex.augmented.common.registry.ModAugments;
import com.ashrex.augmented.network.CraftItemPacket;
import com.ashrex.augmented.network.CraftItemPacketHandler;
import com.ashrex.augmented.network.SyncCraftableItemsPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            LOGGER.info("=== REGISTERING PACKETS ===");

            final var registrar = event.registrar(AugmentedMod.MOD_ID);
            registrar.playToServer(
                    CraftItemPacket.TYPE,
                    CraftItemPacket.STREAM_CODEC,
                    CraftItemPacketHandler::handle
            );

            LOGGER.info("Registered CraftItemPacket with ID: {}", CraftItemPacket.TYPE.id());
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
        {
            if (event.getEntity() instanceof ServerPlayer player) {
                syncRecipesToPlayer(player);
            }
        }

        public static void syncRecipesToPlayer(ServerPlayer player)
        {
            Map<Identifier, List<List<List<Identifier>>>> recipeIngredients = new HashMap<>();
            for (RecipeHolder<?> holder : player.level().recipeAccess().getRecipes()) {
                if (!(holder.value() instanceof CraftingRecipe recipe)) continue;
                if (recipe.placementInfo().isImpossibleToPlace()) continue;

                ItemStack result = recipe.display().isEmpty() ? ItemStack.EMPTY
                        : recipe.display().get(0).result().resolveForFirstStack(ContextMap.EMPTY);
                if (result.isEmpty()) continue;

                Identifier resultId = BuiltInRegistries.ITEM.getKey(result.getItem());

                List<List<Identifier>> ingredientSlots = new ArrayList<>();
                for (Ingredient ingredient : recipe.placementInfo().ingredients()) {
                    if (ingredient.isEmpty()) continue;
                    List<Identifier> validItems = ingredient.items()
                            .map(h -> BuiltInRegistries.ITEM.getKey(h.value()))
                            .collect(java.util.stream.Collectors.toList());
                    if (!validItems.isEmpty()) {
                        ingredientSlots.add(validItems);
                    }
                }

                recipeIngredients.computeIfAbsent(resultId, k -> new ArrayList<>()).add(ingredientSlots);
            }
            PacketDistributor.sendToPlayer(player, new SyncCraftableItemsPacket(recipeIngredients));
        }
    }
}