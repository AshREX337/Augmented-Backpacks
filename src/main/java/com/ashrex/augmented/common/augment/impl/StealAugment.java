package com.ashrex.augmented.common.augment.impl;

import com.ashrex.augmented.AugmentedMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrcrayfish.backpacked.client.LabelAndDescription;
import com.mrcrayfish.backpacked.common.augment.Augment;
import com.mrcrayfish.backpacked.common.augment.AugmentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public record StealAugment(SoundChoice sound, boolean evil) implements Augment<StealAugment> {

    public static final AugmentType<StealAugment> TYPE = new AugmentType<>(
            AugmentedMod.rl("stealing"),
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    SoundChoice.CODEC.fieldOf("sound").orElse(SoundChoice.BELL).forGetter(StealAugment::sound),
                    Codec.BOOL.fieldOf("evil").orElse(false).forGetter(StealAugment::evil)
            ).apply(instance, StealAugment::new)),
            StreamCodec.composite(
                    SoundChoice.STREAM_CODEC, StealAugment::sound,
                    ByteBufCodecs.BOOL, StealAugment::evil,
                    StealAugment::new
            ),
            () -> new StealAugment(SoundChoice.BELL, false)
    );

    @Override
    public AugmentType<StealAugment> type()
    {
        return TYPE;
    }

    public StealAugment switchMode(SoundChoice sound)
    {
        sound.playSound();
        return new StealAugment(sound, this.evil);
    }

    public StealAugment setEvil(boolean e)
    {
        return new StealAugment(this.sound, e);
    }

    public enum SoundChoice implements StringRepresentable, LabelAndDescription
    {
        CHEST(SoundEvents.CHEST_OPEN),
        SHULKER(SoundEvents.SHULKER_BOX_OPEN),
        AMETHYST(SoundEvents.AMETHYST_BLOCK_HIT),
        BELL(SoundEvents.BELL_BLOCK),
        WOOD(SoundEvents.AXE_STRIP),
        CREEPER(SoundEvents.CREEPER_PRIMED),
        DRAGON(SoundEvents.ENDER_DRAGON_GROWL);

        public static final Codec<SoundChoice> CODEC = StringRepresentable.fromEnum(SoundChoice::values);
        public static final StreamCodec<FriendlyByteBuf, SoundChoice> STREAM_CODEC = StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(SoundChoice.class));
        private static final String LANGUAGE_KEY = "augment.backpacked.stealing.sound";

        private final SoundEvent sound;
        private final Component name;
        private final Component tooltip;

        SoundChoice(SoundEvent sound)
        {
            this.sound = sound;
            this.name = Component.translatable("%s.%s".formatted(LANGUAGE_KEY, this.getSerializedName()));
            this.tooltip = Component.translatable("%s.%s.tooltip".formatted(LANGUAGE_KEY, this.getSerializedName()));
        }

        @Override
        public String getSerializedName()
        {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public SoundEvent checkSound()
        {
            return this.sound;
        }

        public void playSound()
        {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(this.sound, 1.0F, 3.0f)
            );
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
