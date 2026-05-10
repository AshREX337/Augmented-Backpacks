// ModEvents.java
package com.ashrex.augmented;

import com.ashrex.augmented.network.SyncCraftableItemsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
        Set<Identifier> resultIds = new HashSet<>();
        for (RecipeHolder<?> holder : player.level().recipeAccess().getRecipes()) {
            if (holder.value() instanceof CraftingRecipe recipe) {
                ItemStack result = recipe.display().isEmpty() ? ItemStack.EMPTY
                        : recipe.display().get(0).result().resolveForFirstStack(ContextMap.EMPTY);
                if (!result.isEmpty()) {
                    resultIds.add(BuiltInRegistries.ITEM.getKey(result.getItem()));
                }
            }
        }
        PacketDistributor.sendToPlayer(player, new SyncCraftableItemsPacket(new ArrayList<>(resultIds)));
    }
}