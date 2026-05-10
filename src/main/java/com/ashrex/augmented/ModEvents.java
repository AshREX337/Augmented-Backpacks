// ModEvents.java
package com.ashrex.augmented;

import com.ashrex.augmented.network.SyncCraftableItemsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

@EventBusSubscriber(modid = AugmentedMod.MOD_ID)
public class ModEvents
{
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

            // Each ingredient slot is a list of valid items (any one will do)
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