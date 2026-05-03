package com.ashrex.augmented.common.augment.impl;

import com.ashrex.augmented.AugmentedMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrcrayfish.backpacked.client.LabelAndDescription;
import com.mrcrayfish.backpacked.common.augment.Augment;
import com.mrcrayfish.backpacked.common.augment.AugmentType;
import com.mrcrayfish.backpacked.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.awt.*;
import java.util.Locale;

public record FeedingAugment(boolean nutrition, boolean saturation) implements Augment<FeedingAugment> {

    public static final AugmentType<FeedingAugment> TYPE = new AugmentType<>(
            AugmentedMod.rl("feeding"),
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.BOOL.fieldOf("nutrition").orElse(true).forGetter(FeedingAugment::nutrition),
                    Codec.BOOL.fieldOf("saturation").orElse(false).forGetter(FeedingAugment::saturation)
            ).apply(instance, FeedingAugment::new)),
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, FeedingAugment::nutrition,
                    ByteBufCodecs.BOOL, FeedingAugment::saturation,
                    FeedingAugment::new
            ),
            () -> new FeedingAugment(true, false)
    );

    @Override
    public AugmentType<FeedingAugment> type()
    {
        return TYPE;
    }

    public FeedingAugment useNutrition()
    {
        return new FeedingAugment(true, false);
    }

    public FeedingAugment useSaturation()
    {
        return new FeedingAugment(false, true);
    }
}
