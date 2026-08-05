package com.ashrex.augmented.common.augment.impl;

import com.ashrex.augmented.AugmentedMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrcrayfish.backpacked.client.LabelAndDescription;
import com.mrcrayfish.backpacked.common.augment.Augment;
import com.mrcrayfish.backpacked.common.augment.AugmentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public record FeedingAugment(FeedMode mode) implements Augment<FeedingAugment> {

    public static final AugmentType<FeedingAugment> TYPE = new AugmentType<>(
            AugmentedMod.rl("feeding"),
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    FeedMode.CODEC.fieldOf("mode").orElse(FeedMode.NUTRITION).forGetter(FeedingAugment::mode)
            ).apply(instance, FeedingAugment::new)),
            StreamCodec.composite(
                    FeedMode.STREAM_CODEC, FeedingAugment::mode,
                    FeedingAugment::new
            ),
            () -> new FeedingAugment(FeedMode.NUTRITION)
    );

    @Override
    public AugmentType<FeedingAugment> type()
    {
        return TYPE;
    }

    public FeedingAugment switchMode(FeedingAugment.FeedMode mode)
    {
        return new FeedingAugment(mode);
    }

    public enum FeedMode implements StringRepresentable, LabelAndDescription
    {
        NUTRITION(true, false),
        SATURATION(false, true);

        public static final Codec<FeedingAugment.FeedMode> CODEC = StringRepresentable.fromEnum(FeedingAugment.FeedMode::values);
        public static final StreamCodec<FriendlyByteBuf, FeedingAugment.FeedMode> STREAM_CODEC = StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(FeedingAugment.FeedMode.class));
        private static final String LANGUAGE_KEY = "augment.backpacked.feeding";

        private final boolean nutrition;
        private final boolean saturation;
        private final Component name;
        private final Component tooltip;

        FeedMode(boolean nutrition, boolean saturation)
        {
            this.nutrition = nutrition;
            this.saturation = saturation;
            this.name = Component.translatable("%s.%s".formatted(LANGUAGE_KEY, this.getSerializedName()));
            this.tooltip = Component.translatable("%s.%s.tooltip".formatted(LANGUAGE_KEY, this.getSerializedName()));
        }

        @Override
        public String getSerializedName()
        {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public boolean checkNutrition()
        {
            return this.nutrition;
        }

        public boolean checkSaturation()
        {
            return this.saturation;
        }

        @Override
        public Component label()
        {
            return this.name;
        }

        @Override
        public Component description()
        {
            return this.tooltip;
        }
    }
}
