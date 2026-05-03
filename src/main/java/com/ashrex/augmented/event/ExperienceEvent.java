package com.ashrex.augmented.event;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.common.augment.impl.ExperienceAugment;
import com.ashrex.augmented.common.registry.ModAugments;
import com.mrcrayfish.backpacked.BackpackHelper;
import com.mrcrayfish.backpacked.common.augment.Augments;
import com.mrcrayfish.backpacked.common.augment.impl.RecallAugment;
import com.mrcrayfish.backpacked.core.ModAugmentTypes;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = AugmentedMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ExperienceEvent {

    @SubscribeEvent
    public static void storeXP(PlayerTickEvent.Pre event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        int currentPlayerXP = player.experienceLevel;
        if(player.isDeadOrDying() && !player.getTags().contains("died")) player.addTag("died");

        var backpackInv = BackpackHelper.getBackpackInventoriesWithAugment(player, ModAugments.EXPERIENCE_AUGMENT.get());

        for(var thing : backpackInv)
        {
            var storedXp = thing.augment().experience();

            var stack = thing.inventory().getBackpackStack();
            var augment = thing.augment();

            var currentAugments = Augments.get(stack);
            Augments.Position pos = null;
            RecallAugment recall = BackpackHelper.findAugment(stack, ModAugmentTypes.RECALL.get());

            if(currentAugments.firstAugment() == augment) pos = Augments.Position.FIRST;
            if(currentAugments.secondAugment() == augment) pos = Augments.Position.SECOND;
            if(currentAugments.thirdAugment() == augment) pos = Augments.Position.THIRD;
            if(currentAugments.fourthAugment() == augment) pos = Augments.Position.FOURTH;

            if(currentPlayerXP < storedXp && player.getTags().contains("died"))
            {
                player.setExperienceLevels(storedXp);
                player.removeTag("died");
            }
            var limit = 30;
            if(recall != null) limit = 60;

            var newAugment = thing.augment().setMax(limit);
            var xpAugment = currentPlayerXP > limit ? newAugment.setXp(limit) : newAugment.setXp(currentPlayerXP);


            var newAugments = currentAugments.setAugment(pos, xpAugment);
            Augments.set(stack, newAugments);

        }

    }
}
