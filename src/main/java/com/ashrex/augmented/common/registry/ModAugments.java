package com.ashrex.augmented.common.registry;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.common.augment.impl.CraftingAugment;
import com.ashrex.augmented.common.augment.impl.ExperienceAugment;
import com.ashrex.augmented.common.augment.impl.FeedingAugment;
import com.ashrex.augmented.common.augment.impl.ToolSwapAugment;
import com.mrcrayfish.backpacked.common.augment.AugmentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModAugments
{
    // Create a deferred register for Backpacked's augment types registry
    public static final DeferredRegister<AugmentType<?>> AUGMENT_TYPES =
            DeferredRegister.create(
                    ResourceKey.createRegistryKey(
                            Identifier.fromNamespaceAndPath("backpacked", "augment_types")
                    ),
                    AugmentedMod.MOD_ID
            );

    // Register our crafting augment type
    public static final Supplier<AugmentType<CraftingAugment>> CRAFTING_AUGMENT = AUGMENT_TYPES.register(
            "crafting",
            () -> CraftingAugment.TYPE
    );

    public static final Supplier<AugmentType<ToolSwapAugment>> SWAPPING_AUGMENT = AUGMENT_TYPES.register(
            "swapping",
            () -> ToolSwapAugment.TYPE
    );

    public static final Supplier<AugmentType<ExperienceAugment>> EXPERIENCE_AUGMENT = AUGMENT_TYPES.register(
            "experience",
            () -> ExperienceAugment.TYPE
    );

    public static final Supplier<AugmentType<FeedingAugment>> FEEDING_AUGMENT = AUGMENT_TYPES.register(
            "feeding",
            () -> FeedingAugment.TYPE
    );
}