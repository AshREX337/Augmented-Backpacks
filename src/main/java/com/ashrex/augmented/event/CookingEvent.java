package com.ashrex.augmented.event;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.common.registry.ModAugments;
import com.mrcrayfish.backpacked.BackpackHelper;
import com.mrcrayfish.backpacked.common.augment.Augments;
import com.mrcrayfish.backpacked.common.augment.impl.RecallAugment;
import com.mrcrayfish.backpacked.core.ModAugmentTypes;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

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

            //if(player.)

        }

    }
}
