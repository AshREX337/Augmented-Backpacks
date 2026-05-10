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


@EventBusSubscriber(modid = AugmentedMod.MOD_ID)
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
                    AugmentedMod.LOGGER.info(stack.get(DataComponents.FOOD).nutrition() + ", " + max);
                    AugmentedMod.LOGGER.info(stack.get(DataComponents.FOOD).saturation() + ", " + max);
                    if(feeding.nutrition() && stack.get(DataComponents.FOOD).nutrition() > max)
                    {
                        max = stack.get(DataComponents.FOOD).nutrition();
                        food = stack;
                    }
                    if(feeding.saturation() && stack.get(DataComponents.FOOD).saturation() > max)
                    {
                        max = stack.get(DataComponents.FOOD).saturation();
                        food = stack;
                    }
                }
            }

            if(food == null) return;
            FoodProperties foodProperties = food.get(DataComponents.FOOD);

            if(hunger > 12)
            {
                player.getFoodData().eat(foodProperties.nutrition(), foodProperties.saturation());
                food.shrink(1);
                return;
            }
            if(foodProperties.nutrition() - hunger < 2)
            {
                player.getFoodData().eat(foodProperties.nutrition(), foodProperties.saturation());
                food.shrink(1);
            }

        }
    }

}
