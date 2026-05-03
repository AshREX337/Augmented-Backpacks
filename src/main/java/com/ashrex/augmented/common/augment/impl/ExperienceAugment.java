// CraftingAugment.java
package com.ashrex.augmented.common.augment.impl;

import com.ashrex.augmented.AugmentedMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrcrayfish.backpacked.common.augment.Augment;
import com.mrcrayfish.backpacked.common.augment.AugmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ExperienceAugment(int experience, int max) implements Augment<ExperienceAugment>
{
    public static final AugmentType<ExperienceAugment> TYPE = new AugmentType<>(
            AugmentedMod.rl("experience"),
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.INT.fieldOf("experience").orElse(0).forGetter(ExperienceAugment::experience),
                    Codec.INT.fieldOf("max").orElse(30).forGetter(ExperienceAugment::max)
            ).apply(instance, ExperienceAugment::new)),
            StreamCodec.composite(
                    ByteBufCodecs.INT, ExperienceAugment::experience,
                    ByteBufCodecs.INT, ExperienceAugment::max,
                    ExperienceAugment::new
            ),
            () -> new ExperienceAugment(0, 30)
    );

    @Override
    public AugmentType<ExperienceAugment> type()
    {
        return TYPE;
    }

    public ExperienceAugment setXp(int experience)
    {
        return new ExperienceAugment(experience, this.max);
    }

    public ExperienceAugment setMax(int max)
    {
        return new ExperienceAugment(this.experience, max);
    }
}

