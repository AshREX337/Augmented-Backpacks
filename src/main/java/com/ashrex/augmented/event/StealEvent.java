package com.ashrex.augmented.event;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.common.augment.impl.StealAugment;
import com.ashrex.augmented.common.registry.ModAugments;
import com.mrcrayfish.backpacked.BackpackHelper;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import com.mrcrayfish.backpacked.inventory.container.BackpackContainerMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = AugmentedMod.MOD_ID)
public class StealEvent {

    @SubscribeEvent
    public static void steal(PlayerContainerEvent.Open event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!(event.getContainer() instanceof BackpackContainerMenu menu)) return;

        if(!menu.isOwner())
        {
            var augment = ModAugments.STEAL_AUGMENT.get();

            if(!menu.getAugments().has(augment)) return;
            if(play(menu))
            {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
                Vec3 look = player.getLookAngle();
                Vec3 horizontal = new Vec3(look.x, 0, look.z).normalize();

                player.setDeltaMovement(
                        -horizontal.x * 0.75,
                        0.5,
                        -horizontal.z * 0.75
                );
                player.hurtMarked = true;
                player.closeContainer();
            }

        }
    }

    private static boolean play(BackpackContainerMenu menu)
    {
        if(menu.getAugments().firstAugment() instanceof StealAugment steal)
        {
            steal.sound().playSound();
            return steal.evil();
        }
        if(menu.getAugments().secondAugment() instanceof StealAugment steal)
        {
            steal.sound().playSound();
            return steal.evil();
        }
        if(menu.getAugments().thirdAugment() instanceof StealAugment steal)
        {
            steal.sound().playSound();
            return steal.evil();
        }
        if(menu.getAugments().fourthAugment() instanceof StealAugment steal)
        {
            steal.sound().playSound();
            return steal.evil();
        }
        return false;
    }

}
