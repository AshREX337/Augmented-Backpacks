// CraftingAugment.java
package com.ashrex.augmented.common.augment.impl;

import com.ashrex.augmented.AugmentedMod;
import com.mojang.serialization.MapCodec;
import com.mrcrayfish.backpacked.common.augment.Augment;
import com.mrcrayfish.backpacked.common.augment.AugmentType;
import net.minecraft.network.codec.StreamCodec;

public record TrashAugment() implements Augment<TrashAugment>
{
    public static final TrashAugment INSTANCE = new TrashAugment();
    public static final AugmentType<TrashAugment> TYPE = new AugmentType<>(
            AugmentedMod.rl("trash"),
            MapCodec.unit(INSTANCE),
            StreamCodec.unit(INSTANCE),
            () -> INSTANCE
    );

    @Override
    public AugmentType<TrashAugment> type()
    {
        return TYPE;
    }

}

