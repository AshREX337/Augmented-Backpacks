// CraftingAugment.java
package com.ashrex.augmented.common.augment.impl;

import com.ashrex.augmented.AugmentedMod;
import com.mojang.serialization.MapCodec;
import com.mrcrayfish.backpacked.common.augment.Augment;
import com.mrcrayfish.backpacked.common.augment.AugmentType;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public record ToolSwapAugment() implements Augment<ToolSwapAugment>
{
    public static final ToolSwapAugment INSTANCE = new ToolSwapAugment();
    public static final AugmentType<ToolSwapAugment> TYPE = new AugmentType<>(
            AugmentedMod.rl("swapping"),
            MapCodec.unit(INSTANCE),
            StreamCodec.unit(INSTANCE),
            () -> INSTANCE
    );

    @Override
    public AugmentType<ToolSwapAugment> type()
    {
        return TYPE;
    }

}

