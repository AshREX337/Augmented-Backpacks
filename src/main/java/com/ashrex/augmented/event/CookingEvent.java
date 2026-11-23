package com.ashrex.augmented.event;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.common.augment.impl.CookingAugment;
import com.ashrex.augmented.common.registry.ModAugments;
import com.mrcrayfish.backpacked.BackpackHelper;
import com.mrcrayfish.backpacked.common.augment.Augments;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.Objects;

@EventBusSubscriber(modid = AugmentedMod.MOD_ID)
public class CookingEvent {

    @SubscribeEvent
    public static void storeXP(PlayerTickEvent.Post event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if(player.tickCount % 20 != 0) return;

        var backpackInv = BackpackHelper.getBackpackInventoriesWithAugment(player, ModAugments.COOKING_AUGMENT.get());

        for(var thing : backpackInv)
        {
            var augment = thing.augment();

            BackpackInventory inventory = thing.inventory();
            var backpack = thing.inventory().getBackpackStack();
            CookingAugment cooking = BackpackHelper.findAugment(backpack, ModAugments.COOKING_AUGMENT.get());

            ItemStack fuel = null;
            int numFoods = 0;
            ArrayList<String> cookedFoods = new ArrayList<String>();

            var currentAugments = Augments.get(backpack);

            Augments.Position pos = null;
            if(currentAugments.firstAugment() == augment) pos = Augments.Position.FIRST;
            if(currentAugments.secondAugment() == augment) pos = Augments.Position.SECOND;
            if(currentAugments.thirdAugment() == augment) pos = Augments.Position.THIRD;
            if(currentAugments.fourthAugment() == augment) pos = Augments.Position.FOURTH;

            for(int i = inventory.getContainerSize()-1; i>=0; i--)
            {
                ItemStack stack = inventory.getItem(i);
                if(!stack.isEmpty() && stack.getBurnTime(RecipeType.SMELTING) > 0 && fuel == null)
                {
                    fuel = stack;
                    if(cooking.cookTime() > 5)
                    {
                        player.getCooldowns().addCooldown(fuel.getItem(), 100);
                        var newAugment = cooking.setTime(5);
                        fuel.consume(1, player);
                        var newAugments = currentAugments.setAugment(pos, newAugment);
                        Augments.set(backpack, newAugments);
                        return;
                    }
                }
            }

            System.out.println(cooking.cookTime());

            for(int i = inventory.getContainerSize()-1; i>=0; i--)
            {
                ItemStack stack = inventory.getItem(i);

                FoodProperties isFood = stack.get(DataComponents.FOOD);
                if (isFood == null || isFood.nutrition() < 1) continue;

                var input = new SingleRecipeInput(stack);
                var manager = player.level().getRecipeManager();

                var recipe = manager.getRecipeFor(RecipeType.SMELTING, input, player.level());
                boolean canBeCooked = recipe.isPresent();

                if(canBeCooked)
                {
                    numFoods++;
                    if(!player.getCooldowns().isOnCooldown(stack.getItem())) player.getCooldowns().addCooldown(stack.getItem(), cooking.cookTime() * 20);
                    if(player.getCooldowns().isOnCooldown(stack.getItem()) && fuel != null && !fuel.isEmpty() && cooking.cookTime() == 5) player.getCooldowns().addCooldown(stack.getItem(), 100);
                }

                if(canBeCooked && cooking.cookTime() == 0 && !cookedFoods.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()))
                {
                    cookedFoods.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                    var food = recipe.get().value().assemble(input, player.level().registryAccess());
                    inventory.addItem(food);
                    stack.consume(1, player);
                    player.level().playSound(
                            null,
                            player.blockPosition(),
                            SoundEvents.FIRE_EXTINGUISH,
                            SoundSource.PLAYERS,
                            1.0f,
                            1.0f
                    );
                }

            }

            CookingAugment newAugment;
            if(numFoods == 0)
            {
                if(cooking.cookTime() == 30) return;
                newAugment = cooking.setTime(30);
                var newAugments = currentAugments.setAugment(pos, newAugment);
                Augments.set(backpack, newAugments);
                return;
            }

            if(cooking.cookTime() == 0)
            {
                if(fuel != null && !fuel.isEmpty())
                {
                    newAugment = cooking.setTime(5);
                    fuel.consume(1, player);

                    player.getCooldowns().addCooldown(fuel.getItem(), 120);
                }
                else newAugment = cooking.setTime(30);
            }
            else newAugment = cooking.setTime(Math.max(cooking.cookTime() - 1, 0));


            var newAugments = currentAugments.setAugment(pos, newAugment);
            Augments.set(backpack, newAugments);
        }

    }
}
