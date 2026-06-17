package com.ashrex.augmented.event;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.common.registry.ModAugments;
import com.mrcrayfish.backpacked.BackpackHelper;
import com.mrcrayfish.backpacked.common.augment.Augments;
import com.mrcrayfish.backpacked.common.augment.impl.RecallAugment;
import com.mrcrayfish.backpacked.core.ModAugmentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = AugmentedMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class TrackingEvent {

    @SubscribeEvent
    public static void trackDeath(PlayerTickEvent.Post event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();

        boolean hasAugment = !BackpackHelper.getBackpackInventoriesWithAugment(
                player, ModAugments.RECOVERY_AUGMENT.get()
        ).isEmpty();

        if(hasAugment && !player.getTags().contains("recover")) player.addTag("recover");
        else if(player.getTags().contains("recover") && !hasAugment) player.removeTag("recover");

        if(player.isDeadOrDying() && !player.getTags().contains("died") && player.getTags().contains("recover"))
        {
            player.addTag("died");
            player.removeTag("recover");
        }

        if(!player.getTags().contains("died")) return;

        player.getLastDeathLocation().ifPresent(globalPos -> {
            BlockPos blockPos = globalPos.pos();
            var direction = blockPos.subtract(player.blockPosition());
            Vec3 dist = new Vec3(direction.getX(), direction.getY(), direction.getZ());
            Vec3 normalized = new Vec3(direction.getX(), direction.getY(), direction.getZ()).normalize();

            double x = normalized.x*2 + player.getX();
            double y = normalized.y + player.getY();
            double z = normalized.z*2 + player.getZ();

            level.sendParticles(
                    ParticleTypes.WAX_OFF,
                    x, y + 1.0, z,  // slightly above player
                    1,               // count
                    0.1, 0.1, 0.1,         // no spread
                    0.01             // speed
            );



            if((dist.length() != 0 && dist.length() < 4) || player.getTags().contains("recover"))
            {
                player.removeTag("died");
                player.removeTag("recover");
            }
        });


    }
}