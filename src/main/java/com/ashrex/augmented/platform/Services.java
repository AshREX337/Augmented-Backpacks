// Services.java
package com.ashrex.augmented.platform;

import com.ashrex.augmented.AugmentedMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;

public class Services
{
    public static boolean isPhysicalClient()
    {
        return FMLLoader.getDist() == Dist.CLIENT;
    }

    public static boolean isModLoaded(String modId)
    {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    public static void checkBackpackedLoaded()
    {
        if (!isModLoaded("backpacked")) {
            AugmentedMod.LOGGER.error("Backpacked mod is not loaded! Ashrex Augmented requires Backpacked to function.");
            throw new RuntimeException("Backpacked is required for Ashrex Augmented");
        }
    }
}