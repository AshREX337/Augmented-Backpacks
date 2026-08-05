// CraftItemPacketHandler.java
package com.ashrex.augmented.network;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.common.registry.ModAugments;
import com.mrcrayfish.backpacked.BackpackHelper;
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

            // OLD: menu-slot-0 lookup deleted. NEW: get backpacks straight off the player.
            List<BackpackInventory> backpacks = BackpackHelper
                    .getBackpackInventoriesWithAugment(serverPlayer, ModAugments.CRAFTING_AUGMENT.get())
                    .stream()
                    .map(entry -> entry.inventory())
                    .toList();

            System.out.println("Backpacks found: " + backpacks.size());

            // Build a crafting input from available items
            List<ItemStack> ingredients = new ArrayList<>();
            List<SlotRef> toConsume = new ArrayList<>();
            if (!gatherIngredients(serverPlayer, backpacks, recipe, ingredients, toConsume)) {
                System.out.println("ERROR: Could not gather ingredients!");
                serverPlayer.displayClientMessage(
                        Component.translatable("augment.augmented.crafting.insufficient_materials"), true);
                return;
            }

            System.out.println("Ingredients gathered: " + ingredients.size());

            CraftingInput craftingInput = CraftingInput.of(3, 3, ingredients);
            ItemStack result = recipe.assemble(craftingInput, serverPlayer.level().registryAccess());

            if(result.isEmpty()) {
                System.out.println("ERROR: Empty result!");
                return;
            }

            System.out.println("Result: " + result.getItem() + " x" + result.getCount());

            // Consume ingredients
            consumeIngredients(serverPlayer, backpacks, toConsume);

            // Give result
            ItemStack resultCopy = result.copy();
            if(!serverPlayer.getInventory().add(resultCopy)) {
                serverPlayer.drop(resultCopy, false);
            }

            // Force sync
            for (BackpackInventory b : backpacks) {
                b.setChanged();
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
    private static boolean gatherIngredients(ServerPlayer player, List<BackpackInventory> backpacks,
                                             CraftingRecipe recipe, List<ItemStack> ingredients,
                                             List<SlotRef> toConsume)
    {
        List<Ingredient> recipeIngredients = recipe.getIngredients();
        Map<Integer, Integer> playerUsage = new HashMap<>();
        List<Map<Integer, Integer>> backpackUsage = new ArrayList<>();
        for (int b = 0; b < backpacks.size(); b++) backpackUsage.add(new HashMap<>());

        for (int i = 0; i < Math.min(recipeIngredients.size(), 9); i++) {
            Ingredient ingredient = recipeIngredients.get(i);
            if (ingredient.isEmpty()) { ingredients.add(ItemStack.EMPTY); continue; }

            boolean found = false;

            // Try player inventory
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (stack.isEmpty() || !ingredient.test(stack)) continue;
                int used = playerUsage.getOrDefault(slot, 0);
                if (stack.getCount() > used) {
                    ingredients.add(stack.copyWithCount(1));
                    playerUsage.put(slot, used + 1);
                    toConsume.add(new SlotRef(-1, slot)); // -1 = player inventory
                    found = true;
                    break;
                }
            }

            // Try each backpack in turn
            if (!found) {
                for (int b = 0; b < backpacks.size() && !found; b++) {
                    BackpackInventory backpackInv = backpacks.get(b);
                    Map<Integer, Integer> usage = backpackUsage.get(b);
                    for (int slot = 0; slot < backpackInv.getContainerSize(); slot++) {
                        ItemStack stack = backpackInv.getItem(slot);
                        if (stack.isEmpty() || !ingredient.test(stack)) continue;
                        int used = usage.getOrDefault(slot, 0);
                        if (stack.getCount() > used) {
                            ingredients.add(stack.copyWithCount(1));
                            usage.put(slot, used + 1);
                            toConsume.add(new SlotRef(b, slot)); // b = index into backpacks list
                            found = true;
                            break;
                        }
                    }
                }
            }

            if (!found) return false;
        }

        while (ingredients.size() < 9) ingredients.add(ItemStack.EMPTY);
        return true;
    }

    record SlotRef(int backpackIndex, int slot) {} // backpackIndex == -1 means player inventory

    private static void consumeIngredients(ServerPlayer player, List<BackpackInventory> backpacks,
                                           List<SlotRef> toConsume)
    {
        for (SlotRef ref : toConsume) {
            if (ref.backpackIndex() == -1) {
                ItemStack stack = player.getInventory().getItem(ref.slot());
                stack.shrink(1);
                if (stack.isEmpty()) player.getInventory().setItem(ref.slot(), ItemStack.EMPTY);
            } else {
                BackpackInventory backpackInv = backpacks.get(ref.backpackIndex());
                ItemStack stack = backpackInv.getItem(ref.slot());
                stack.shrink(1);
                if (stack.isEmpty()) backpackInv.setItem(ref.slot(), ItemStack.EMPTY);
            }
        }
        player.getInventory().setChanged();
        for (BackpackInventory b : backpacks) b.setChanged();
    }

    private enum InventorySource {
        PLAYER, BACKPACK
    }
}