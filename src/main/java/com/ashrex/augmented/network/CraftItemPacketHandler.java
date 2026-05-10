// CraftItemPacketHandler.java
package com.ashrex.augmented.network;

import com.ashrex.augmented.AugmentedMod;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

public class CraftItemPacketHandler
{
    public static void handle(CraftItemPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            Identifier itemId = packet.itemId();
            net.minecraft.world.item.Item targetItem = BuiltInRegistries.ITEM.getValue(itemId);
            if (targetItem == null || targetItem == Items.AIR) return;

            // Find first matching crafting recipe for this item
            RecipeHolder<CraftingRecipe> foundRecipe = null;
            for (RecipeHolder<?> holder : serverPlayer.level().recipeAccess().getRecipes()) {
                if (!(holder.value() instanceof CraftingRecipe recipe)) continue;
                ItemStack result = recipe.display().isEmpty() ? ItemStack.EMPTY
                        : recipe.display().get(0).result().resolveForFirstStack(ContextMap.EMPTY);
                if (result.getItem() == targetItem) {
                    foundRecipe = new RecipeHolder<>(holder.id(), recipe);
                    break;
                }
            }

            if (foundRecipe == null) {
                serverPlayer.displayClientMessage(
                        Component.translatable("augment.augmented.crafting.no_recipe"), true);
                return;
            }

            CraftingRecipe recipe = foundRecipe.value();

            BackpackInventory backpackInv = null;
            if (serverPlayer.containerMenu != null && serverPlayer.containerMenu.slots.size() > 0) {
                Container container = serverPlayer.containerMenu.slots.get(0).container;
                if (container instanceof BackpackInventory) {
                    backpackInv = (BackpackInventory) container;
                }
            }

            List<ItemStack> ingredients = new ArrayList<>();
            if (!gatherIngredients(serverPlayer, backpackInv, recipe, ingredients)) {
                serverPlayer.displayClientMessage(
                        Component.translatable("augment.augmented.crafting.insufficient_materials"), true);
                return;
            }

            CraftingInput craftingInput = CraftingInput.of(3, 3, ingredients);
            ItemStack result = recipe.assemble(craftingInput, serverPlayer.level().registryAccess());

            if (result.isEmpty()) return;

            consumeIngredients(serverPlayer, backpackInv, ingredients);

            ItemStack resultCopy = result.copy();
            if (!serverPlayer.getInventory().add(resultCopy)) {
                serverPlayer.drop(resultCopy, false);
            }

            if (backpackInv != null) backpackInv.setChanged();
            serverPlayer.inventoryMenu.broadcastChanges();

            serverPlayer.displayClientMessage(
                    Component.translatable("augment.augmented.crafting.crafted", result.getHoverName()), true);
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    private static boolean gatherIngredients(ServerPlayer player, BackpackInventory backpackInv,
                                             CraftingRecipe recipe, List<ItemStack> ingredients)
    {
        List<Ingredient> recipeIngredients = recipe.placementInfo().ingredients();
        Map<InventorySource, Map<Integer, Integer>> slotUsage = new HashMap<>();
        slotUsage.put(InventorySource.PLAYER, new HashMap<>());
        if(backpackInv != null) {
            slotUsage.put(InventorySource.BACKPACK, new HashMap<>());
        }

        // Fill up to 9 slots (3x3 crafting grid)
        for(int i = 0; i < Math.min(recipeIngredients.size(), 9); i++) {
            Ingredient ingredient = recipeIngredients.get(i);

            if(ingredient.isEmpty()) {
                ingredients.add(ItemStack.EMPTY);
                continue;
            }

            ItemStack found = null;

            // Try player inventory first
            for(int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if(stack.isEmpty() || !ingredient.test(stack)) continue;

                int used = slotUsage.get(InventorySource.PLAYER).getOrDefault(slot, 0);
                if(stack.getCount() > used) {
                    found = stack.copy();
                    found.setCount(1);
                    slotUsage.get(InventorySource.PLAYER).put(slot, used + 1);
                    break;
                }
            }

            // Try backpack if not found in player inventory
            if(found == null && backpackInv != null) {
                for(int slot = 0; slot < backpackInv.getContainerSize(); slot++) {
                    ItemStack stack = backpackInv.getItem(slot);
                    if(stack.isEmpty() || !ingredient.test(stack)) continue;

                    int used = slotUsage.get(InventorySource.BACKPACK).getOrDefault(slot, 0);
                    if(stack.getCount() > used) {
                        found = stack.copy();
                        found.setCount(1);
                        slotUsage.get(InventorySource.BACKPACK).put(slot, used + 1);
                        break;
                    }
                }
            }

            if(found == null) {
                return false;
            }

            ingredients.add(found);
        }

        // Fill remaining slots with empty stacks
        while(ingredients.size() < 9) {
            ingredients.add(ItemStack.EMPTY);
        }

        return true;
    }

    private static void consumeIngredients(ServerPlayer player, BackpackInventory backpackInv,
                                           List<ItemStack> ingredients)
    {
        for(ItemStack ingredient : ingredients) {
            if(ingredient.isEmpty()) continue;

            // Try to consume from player inventory first
            boolean consumed = false;
            for(int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack invStack = player.getInventory().getItem(slot);
                if(ItemStack.isSameItemSameComponents(invStack, ingredient)) {
                    System.out.println("Consuming from player inv slot " + slot);
                    invStack.shrink(1);
                    if(invStack.isEmpty()) {
                        player.getInventory().setItem(slot, ItemStack.EMPTY);
                    }
                    consumed = true;
                    break;
                }
            }

            // Try backpack if not found in player inventory
            if(!consumed && backpackInv != null) {
                for(int slot = 0; slot < backpackInv.getContainerSize(); slot++) {
                    ItemStack invStack = backpackInv.getItem(slot);
                    if(ItemStack.isSameItemSameComponents(invStack, ingredient)) {
                        System.out.println("Consuming from backpack slot " + slot);
                        invStack.shrink(1);
                        if(invStack.isEmpty()) {
                            backpackInv.setItem(slot, ItemStack.EMPTY);
                        }
                        consumed = true;
                        break;
                    }
                }
            }
        }

        player.getInventory().setChanged();
        if(backpackInv != null) {
            backpackInv.setChanged();
        }
    }

    private enum InventorySource {
        PLAYER, BACKPACK
    }
}