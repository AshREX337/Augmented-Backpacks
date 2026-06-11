package com.ashrex.augmented.event;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.common.augment.impl.FeedingAugment;
import com.ashrex.augmented.common.augment.impl.ToolSwapAugment;
import com.ashrex.augmented.common.registry.ModAugments;
import com.mrcrayfish.backpacked.BackpackHelper;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;


@EventBusSubscriber(modid = AugmentedMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class FeedingEvent {

    @SubscribeEvent
    public static void hungry(PlayerTickEvent.Pre event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if(!player.getFoodData().needsFood()) return;
        int hunger = 20 - player.getFoodData().getFoodLevel();
        float fullness = player.getFoodData().getSaturationLevel();
        BackpackInventory inventory = null;

        var backpackInv = BackpackHelper.getBackpackInventoriesWithAugment(player, ModAugments.FEEDING_AUGMENT.get());
        for(var thing : backpackInv)
        {
            FeedingAugment augment = thing.augment();
            inventory = thing.inventory();
            var backpack = thing.inventory().getBackpackStack();
            FeedingAugment feeding = BackpackHelper.findAugment(backpack, ModAugments.FEEDING_AUGMENT.get());

            float max = 0;
            ItemStack food = null;
            for(int i = inventory.getContainerSize()-1; i>=0; i--)
            {
                ItemStack stack = inventory.getItem(i);
                if(stack.is(Tags.Items.FOODS))
                {

                    FoodProperties foodProperties = stack.get(DataComponents.FOOD);

                    // FIX: Some items like `cake` may have a `c:foods` tag
                    // but is only edible as a block
                    if (foodProperties == null) {
                        continue;
                    }

                    AugmentedMod.LOGGER.info(foodProperties.nutrition() + ", " + max);
                    AugmentedMod.LOGGER.info(foodProperties.saturation() + ", " + max);
                    if(feeding.nutrition() && foodProperties.nutrition() > max)
                    {
                        max = foodProperties.nutrition();
                        food = stack;
                    }
                    if(feeding.saturation() && foodProperties.saturation() > max)
                    {
                        max = foodProperties.saturation();
                        food = stack;
                    }
                }
            }

            if(food == null) return;
            if(hunger > 12)
            {
                player.eat(player.level(), food);
                return;
            }
            if(food.get(DataComponents.FOOD).nutrition() - hunger < 2)
            {
                player.eat(player.level(), food);
            }

        }
    }

}
