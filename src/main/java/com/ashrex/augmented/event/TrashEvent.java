package com.ashrex.augmented.event;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.common.augment.impl.FeedingAugment;
import com.ashrex.augmented.common.augment.impl.ToolSwapAugment;
import com.ashrex.augmented.common.augment.impl.TrashAugment;
import com.ashrex.augmented.common.registry.ModAugments;
import com.mrcrayfish.backpacked.BackpackHelper;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Objects;

@EventBusSubscriber(modid = AugmentedMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class TrashEvent {

    @SubscribeEvent
    public static void trash(PlayerTickEvent.Pre event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        BackpackInventory inventory = null;

        var backpackInv = BackpackHelper.getBackpackInventoriesWithAugment(player, ModAugments.TRASH_AUGMENT.get());
        for(var thing : backpackInv)
        {
            TrashAugment augment = thing.augment();
            var mode = augment.invMode();
            inventory = thing.inventory();
            var playerInv = player.getInventory();

            if(mode.checkBackpack() || mode.checkBoth())
            {
                for(int i = inventory.getContainerSize()-1; i>=0; i--)
                {
                    ItemStack stack = inventory.getItem(i);
                    if(augment.test(stack))
                    {
                        player.drop(stack.copy(), false, false);
                        stack.shrink(stack.getCount());
                    }
                }
            }
            if(mode.checkPlayer() || mode.checkBoth())
            {
                for(int i = playerInv.getContainerSize()-1; i>=0; i--)
                {
                    ItemStack stack = playerInv.getItem(i);
                    if(augment.test(stack))
                    {
                        player.drop(stack.copy(), false, false);
                        stack.shrink(stack.getCount());
                    }
                }
            }

        }
    }

}
