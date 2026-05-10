package com.ashrex.augmented.event;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.common.augment.impl.ToolSwapAugment;
import com.ashrex.augmented.common.registry.ModAugments;
import com.mrcrayfish.backpacked.BackpackHelper;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.awt.event.MouseEvent;

@EventBusSubscriber(modid = AugmentedMod.MOD_ID)
public class ToolSwapEvent {

    @SubscribeEvent
    public static void startBreak(PlayerInteractEvent.LeftClickBlock event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        BlockState state = event.getLevel().getBlockState(event.getPos());
        ItemStack item = event.getItemStack();
        ItemStack axe = null, pickaxe = null, shovel = null;
        int axeI = 0, pickaxeI = 0, shovelI = 0;
        BackpackInventory inventory = null;

        var backpackInv = BackpackHelper.getBackpackInventoriesWithAugment(player, ModAugments.SWAPPING_AUGMENT.get());
        for(var thing : backpackInv)
        {
            ToolSwapAugment augment = thing.augment();
            inventory = thing.inventory();

            for(int i = 0; i<inventory.getContainerSize(); i++)
            {
                if(axe != null && pickaxe != null && shovel != null) break;

                ItemStack stack = inventory.getItem(i);
                if(stack.is(ItemTags.AXES))
                {
                    axe = stack;
                    axeI = i;
                }
                if(stack.is(ItemTags.PICKAXES))
                {
                    pickaxe = stack;
                    pickaxeI = i;
                }
                if(stack.is(ItemTags.SHOVELS))
                {
                    shovel = stack;
                    shovelI = i;
                }

            }
        }

        var temp = item;
        if(item.is(ItemTags.SWORDS)) return;
        if(state.is(BlockTags.MINEABLE_WITH_AXE))
            if(axe != null)
            {
                System.out.println("swapping axe");
                player.setItemInHand(event.getHand(), axe);
                inventory.setItem(axeI, temp);
            }
        if(state.is(BlockTags.MINEABLE_WITH_PICKAXE))
            if(pickaxe != null)
            {
                player.setItemInHand(event.getHand(), pickaxe);
                inventory.setItem(pickaxeI, temp);
            }
        if(state.is(BlockTags.MINEABLE_WITH_SHOVEL))
            if(shovel != null)
            {
                player.setItemInHand(event.getHand(), shovel);
                inventory.setItem(shovelI, temp);
            }
    }

}
