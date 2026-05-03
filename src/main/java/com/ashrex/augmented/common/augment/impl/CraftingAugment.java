// CraftingAugment.java
package com.ashrex.augmented.common.augment.impl;

import com.ashrex.augmented.AugmentedMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrcrayfish.backpacked.common.FilterableItems;
import com.mrcrayfish.backpacked.common.ItemCollection;
import com.mrcrayfish.backpacked.common.augment.Augment;
import com.mrcrayfish.backpacked.common.augment.AugmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public record CraftingAugment(ItemCollection favorites) implements Augment<CraftingAugment>, FilterableItems<CraftingAugment>
{
    public static final AugmentType<CraftingAugment> TYPE = new AugmentType<>(
            AugmentedMod.rl("crafting"),
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ItemCollection.CODEC.fieldOf("favorites").orElse(ItemCollection.EMPTY).forGetter(CraftingAugment::favorites)
            ).apply(instance, CraftingAugment::new)),
            StreamCodec.composite(
                    ItemCollection.STREAM_CODEC, CraftingAugment::favorites,
                    CraftingAugment::new
            ),
            () -> new CraftingAugment(ItemCollection.EMPTY)
    );

    @Override
    public AugmentType<CraftingAugment> type()
    {
        return TYPE;
    }

    // FilterableItems interface implementation
    @Override
    public CraftingAugment addItemFilter(Item item)
    {
        return new CraftingAugment(this.favorites.add(item));
    }

    @Override
    public CraftingAugment removeItemFilter(Item item)
    {
        return new CraftingAugment(this.favorites.remove(item));
    }

    @Override
    public boolean isFilteringItem(Item item)
    {
        return this.favorites.has(item);
    }

    @Override
    public boolean isFilterFull()
    {
        return false; // No limit for favorites
    }
}