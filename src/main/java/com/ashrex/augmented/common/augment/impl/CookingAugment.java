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

public record CookingAugment(int cookTime) implements Augment<CookingAugment>
{
    public static final CookingAugment INSTANCE = new CookingAugment(30);
    public static final AugmentType<CookingAugment> TYPE = new AugmentType<>(
            AugmentedMod.rl("cooking"),
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.INT.fieldOf("cooking").orElse(0).forGetter(CookingAugment::cookTime)
            ).apply(instance, CookingAugment::new)),
            StreamCodec.composite(
                    ByteBufCodecs.INT, CookingAugment::cookTime,
                    CookingAugment::new
            ),
            () -> INSTANCE
    );

    public CookingAugment setTime(int time)
{
    return new CookingAugment(time);
}

    @Override
    public AugmentType<CookingAugment> type()
    {
        return TYPE;
    }

}

