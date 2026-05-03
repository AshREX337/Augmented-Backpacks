// CraftItemPacketHandler.java
package com.ashrex.augmented.network;

import com.ashrex.augmented.AugmentedMod;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

public class CraftItemPacketHandler
{
    public static void handle(CraftItemPacket packet, IPayloadContext context)
    {
        System.out.println("========================================");
        System.out.println("PACKET RECEIVED!!!");
        System.out.println("Recipe ID: " + packet.recipeId());
        System.out.println("========================================");

        if(context.flow().isClientbound()) {
            System.out.println("ERROR: Received on client side!");
            return;
        }

        context.enqueueWork(() -> {
            if(!(context.player() instanceof ServerPlayer serverPlayer)) {
                System.out.println("ERROR: Not a ServerPlayer!");
                return;
            }

            System.out.println("Processing craft for player: " + serverPlayer.getName().getString());

            Optional<RecipeHolder<?>> recipeOpt = serverPlayer.level().getRecipeManager()
                    .byKey(packet.recipeId());

            if(recipeOpt.isEmpty()) {
                System.out.println("ERROR: Recipe not found!");
                serverPlayer.displayClientMessage(
                        Component.translatable("augment.augmented.crafting.no_recipe"), true);
                return;
            }

            RecipeHolder<?> holder = recipeOpt.get();
            if(!(holder.value() instanceof CraftingRecipe recipe)) {
                System.out.println("ERROR: Not a CraftingRecipe!");
                serverPlayer.displayClientMessage(
                        Component.translatable("augment.augmented.crafting.no_recipe"), true);
                return;
            }

            System.out.println("Recipe found: " + recipe.getClass().getSimpleName());

            // Get the backpack inventory if it exists
            BackpackInventory backpackInv = null;
            if(serverPlayer.containerMenu != null &&
                    serverPlayer.containerMenu.slots.size() > 0) {
                Container container = serverPlayer.containerMenu.slots.get(0).container;
                if(container instanceof BackpackInventory) {
                    backpackInv = (BackpackInventory) container;
                    System.out.println("Found backpack inventory!");
                }
            }

            // Build a crafting input from available items
            List<ItemStack> ingredients = new ArrayList<>();
            if(!gatherIngredients(serverPlayer, backpackInv, recipe, ingredients)) {
                System.out.println("ERROR: Could not gather ingredients!");
                serverPlayer.displayClientMessage(
                        Component.translatable("augment.augmented.crafting.insufficient_materials"), true);
                return;
            }

            System.out.println("Ingredients gathered: " + ingredients.size());

            // Create CraftingInput for 1.21.1
            CraftingInput craftingInput = CraftingInput.of(3, 3, ingredients);

            // Get result
            ItemStack result = recipe.assemble(craftingInput, serverPlayer.level().registryAccess());

            if(result.isEmpty()) {
                System.out.println("ERROR: Empty result!");
                return;
            }

            System.out.println("Result: " + result.getItem() + " x" + result.getCount());

            // Consume ingredients
            consumeIngredients(serverPlayer, backpackInv, ingredients);

            // Give result
            ItemStack resultCopy = result.copy();
            if(!serverPlayer.getInventory().add(resultCopy)) {
                serverPlayer.drop(resultCopy, false);
            }

            // Force sync
            if(backpackInv != null) {
                backpackInv.setChanged();
            }
            serverPlayer.inventoryMenu.broadcastChanges();

            System.out.println("Crafting complete!");

            serverPlayer.displayClientMessage(
                    Component.translatable("augment.augmented.crafting.crafted",
                            result.getHoverName()),
                    true
            );
        }).exceptionally(throwable -> {
            System.out.println("EXCEPTION during crafting!");
            throwable.printStackTrace();
            return null;
        });
    }

    private static boolean gatherIngredients(ServerPlayer player, BackpackInventory backpackInv,
                                             CraftingRecipe recipe, List<ItemStack> ingredients)
    {
        List<Ingredient> recipeIngredients = recipe.getIngredients();
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