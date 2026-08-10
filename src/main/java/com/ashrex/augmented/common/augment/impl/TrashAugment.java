// CraftingAugment.java
package com.ashrex.augmented.common.augment.impl;

import com.ashrex.augmented.AugmentedMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrcrayfish.backpacked.Config;
import com.mrcrayfish.backpacked.client.LabelAndDescription;
import com.mrcrayfish.backpacked.common.FilterableItems;
import com.mrcrayfish.backpacked.common.ItemCollection;
import com.mrcrayfish.backpacked.common.augment.Augment;
import com.mrcrayfish.backpacked.common.augment.AugmentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public record TrashAugment(ItemCollection filters, InventoryMode invMode) implements Augment<TrashAugment>, FilterableItems<TrashAugment>
{
    public static final AugmentType<TrashAugment> TYPE = new AugmentType<>(
            AugmentedMod.rl("trash"),
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ItemCollection.CODEC.fieldOf("filters").orElse(ItemCollection.EMPTY).forGetter(TrashAugment::filters),
                    InventoryMode.CODEC.fieldOf("invMode").orElse(InventoryMode.BACKPACK).forGetter(TrashAugment::invMode)
            ).apply(instance, TrashAugment::new)),
            StreamCodec.composite(
                    ItemCollection.STREAM_CODEC, TrashAugment::filters,
                    InventoryMode.STREAM_CODEC, TrashAugment::invMode,
                    TrashAugment::new
            ),
            () -> new TrashAugment(ItemCollection.EMPTY, InventoryMode.BACKPACK)
    );

    @Override
    public AugmentType<TrashAugment> type()
    {
        return TYPE;
    }

    @Override
    public TrashAugment addItemFilter(Item item) {
        return new TrashAugment(this.filters.add(item), invMode);
    }

    @Override
    public TrashAugment removeItemFilter(Item item) {
        return new TrashAugment(this.filters.remove(item), invMode);
    }

    @Override
    public boolean isFilteringItem(Item item) {
        return this.filters.has(item);
    }

    @Override
    public boolean isFilterFull() {
        return this.filters.ids().size() >= Config.AUGMENTS.funnelling.maxFilters.get();
    }

    public TrashAugment toggleMode(InventoryMode mode)
    {
        return new TrashAugment(this.filters, mode);
    }

    public boolean test(ItemStack stack)
    {
        boolean matched = this.filters.has(stack.getItem());
        return matched;
    }

    public enum InventoryMode implements StringRepresentable, LabelAndDescription
    {
        BACKPACK(true, false),
        PLAYER(false, true),
        BOTH(true, true);

        public static final Codec<InventoryMode> CODEC = StringRepresentable.fromEnum(InventoryMode::values);
        public static final StreamCodec<FriendlyByteBuf, InventoryMode> STREAM_CODEC = StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(InventoryMode.class));
        private static final String LANGUAGE_KEY = "augment.backpacked.trash.song";

        private final boolean backpack;
        private final boolean player;
        private final Component name;
        private final Component tooltip;

        InventoryMode(boolean backpack, boolean player)
        {
            this.backpack = backpack;
            this.player = player;
            this.name = Component.translatable("%s.%s".formatted(LANGUAGE_KEY, this.getSerializedName()));
            this.tooltip = Component.translatable("%s.%s.tooltip".formatted(LANGUAGE_KEY, this.getSerializedName()));
        }

        @Override
        public String getSerializedName()
        {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public boolean checkBackpack()
        {
            return this.backpack;
        }

        public boolean checkPlayer()
        {
            return this.player;
        }

        public boolean checkBoth()
        {
            return this.backpack && this.player;
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

