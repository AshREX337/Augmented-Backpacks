package com.ashrex.augmented.event;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.common.augment.impl.FeedingAugment;
import com.ashrex.augmented.common.registry.ModAugments;
import com.mrcrayfish.backpacked.BackpackHelper;
import com.mrcrayfish.backpacked.common.augment.Augments;
import com.mrcrayfish.backpacked.common.augment.impl.RecallAugment;
import com.mrcrayfish.backpacked.core.ModAugmentTypes;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Objects;

@EventBusSubscriber(modid = AugmentedMod.MOD_ID)
public class CookingEvent {

    @SubscribeEvent
    public static void storeXP(PlayerTickEvent.Post event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        var backpackInv = BackpackHelper.getBackpackInventoriesWithAugment(player, ModAugments.COOKING_AUGMENT.get());

        for(var thing : backpackInv)
        {
            var augment = thing.augment();
            var stepsRemaining = augment.steps();
            var max = augment.max();

            BackpackInventory inventory = thing.inventory();
            var backpack = thing.inventory().getBackpackStack();
            FeedingAugment feeding = BackpackHelper.findAugment(backpack, ModAugments.FEEDING_AUGMENT.get());

            for(int i = inventory.getContainerSize()-1; i>=0; i--)
            {
                ItemStack stack = inventory.getItem(i);
                if(stack.is(Tags.Items.FOODS))
                {
                    int food = Objects.requireNonNull(stack.get(DataComponents.FOOD)).nutrition();
                    if(food == 0) return;

                    var input = new SingleRecipeInput(stack);
                    var manager = player.level().getRecipeManager();
                    boolean canBeCooked = manager.getRecipeFor(RecipeType.SMELTING, input, player.level()).isPresent() ||
                                        manager.getRecipeFor(RecipeType.SMOKING, input, player.level()).isPresent() ||
                                        manager.getRecipeFor(RecipeType.CAMPFIRE_COOKING, input, player.level()).isPresent();

                    if(canBeCooked) //set max
                        return;
                }

            }
            System.out.println(player.walkDist % 100);
        }

    }
}
