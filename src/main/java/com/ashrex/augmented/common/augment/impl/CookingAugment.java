// CraftingAugment.java
package com.ashrex.augmented.common.augment.impl;

import com.ashrex.augmented.AugmentedMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrcrayfish.backpacked.common.augment.Augment;
import com.mrcrayfish.backpacked.common.augment.AugmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CookingAugment(int steps, int max) implements Augment<CookingAugment>
{
    public static final AugmentType<CookingAugment> TYPE = new AugmentType<>(
            AugmentedMod.rl("cooking"),
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.INT.fieldOf("steps").orElse(100).forGetter(CookingAugment::steps),
                    Codec.INT.fieldOf("max").orElse(100).forGetter(CookingAugment::max)
            ).apply(instance, CookingAugment::new)),
            StreamCodec.composite(
                    ByteBufCodecs.INT, CookingAugment::steps,
                    ByteBufCodecs.INT, CookingAugment::max,
                    CookingAugment::new
            ),
            () -> new CookingAugment(100, 100)
    );

    @Override
    public AugmentType<CookingAugment> type()
    {
        return TYPE;
    }

    public CookingAugment setXp(int steps)
    {
        return new CookingAugment(steps, this.max);
    }

    public CookingAugment setMax(int max)
    {
        return new CookingAugment(this.steps, max);
    }

}

