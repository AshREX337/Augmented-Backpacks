// CraftingAugment.java
package com.ashrex.augmented.common.augment.impl;

import com.ashrex.augmented.AugmentedMod;
import com.mojang.serialization.MapCodec;
import com.mrcrayfish.backpacked.common.augment.Augment;
import com.mrcrayfish.backpacked.common.augment.AugmentType;
import net.minecraft.network.codec.StreamCodec;

public record RecoveryAugment() implements Augment<RecoveryAugment>
{
    public static final RecoveryAugment INSTANCE = new RecoveryAugment();
    public static final AugmentType<RecoveryAugment> TYPE = new AugmentType<>(
            AugmentedMod.rl("recovery"),
            MapCodec.unit(INSTANCE),
            StreamCodec.unit(INSTANCE),
            () -> INSTANCE
    );

    @Override
    public AugmentType<RecoveryAugment> type()
    {
        return TYPE;
    }

}

