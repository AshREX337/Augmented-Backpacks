package com.ashrex.augmented.common.registry;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.common.augment.impl.*;
import com.mrcrayfish.backpacked.common.augment.AugmentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModAugments
{
    // Create a deferred register for Backpacked's augment types registry
    public static final DeferredRegister<AugmentType<?>> AUGMENTS =
            DeferredRegister.create(
                    ResourceKey.createRegistryKey(
                            ResourceLocation.fromNamespaceAndPath("backpacked", "augment_types")
                    ),
                    AugmentedMod.MOD_ID
            );

    // Register our crafting augment type
    public static final Supplier<AugmentType<CraftingAugment>> CRAFTING_AUGMENT = AUGMENTS.register(
            "crafting",
            () -> CraftingAugment.TYPE
    );

    public static final Supplier<AugmentType<ToolSwapAugment>> SWAPPING_AUGMENT = AUGMENTS.register(
            "swapping",
            () -> ToolSwapAugment.TYPE
    );

    public static final Supplier<AugmentType<ExperienceAugment>> EXPERIENCE_AUGMENT = AUGMENTS.register(
            "experience",
            () -> ExperienceAugment.TYPE
    );

    public static final Supplier<AugmentType<FeedingAugment>> FEEDING_AUGMENT = AUGMENTS.register(
            "feeding",
            () -> FeedingAugment.TYPE
    );

    public static final Supplier<AugmentType<RecoveryAugment>> RECOVERY_AUGMENT = AUGMENTS.register(
            "recovery",
            () -> RecoveryAugment.TYPE
    );

    public static final Supplier<AugmentType<TrashAugment>> TRASH_AUGMENT = AUGMENTS.register(
            "trash",
            () -> TrashAugment.TYPE
    );
}