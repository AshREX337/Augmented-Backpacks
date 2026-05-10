// AugmentScreenRegistrar.java
package com.ashrex.augmented.client;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.client.augment.menu.CraftingMenu;
import com.ashrex.augmented.client.augment.menu.ExperienceMenu;
import com.ashrex.augmented.client.augment.menu.FeedingMenu;
import com.ashrex.augmented.common.registry.ModAugments;
import com.mrcrayfish.backpacked.client.augment.AugmentSettingsFactories;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = AugmentedMod.MOD_ID, value = Dist.CLIENT)
public class AugmentScreenRegistrar
{
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            var craftingType = ModAugments.CRAFTING_AUGMENT.get();
            AugmentSettingsFactories.registerFactory(craftingType, CraftingMenu::new);
            AugmentedMod.LOGGER.info("Successfully registered crafting augment GUI menu");
        });

        event.enqueueWork(() -> {
            var expType = ModAugments.EXPERIENCE_AUGMENT.get();
            AugmentSettingsFactories.registerFactory(expType, ExperienceMenu::new);
        });

        event.enqueueWork(() -> {
            var expType = ModAugments.FEEDING_AUGMENT.get();
            AugmentSettingsFactories.registerFactory(expType, FeedingMenu::new);
        });
    }
}