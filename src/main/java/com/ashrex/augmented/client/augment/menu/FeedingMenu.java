package com.ashrex.augmented.client.augment.menu;

import com.ashrex.augmented.common.augment.impl.FeedingAugment;
import com.mrcrayfish.backpacked.client.augment.AugmentHolder;
import com.mrcrayfish.backpacked.client.augment.AugmentSettingsMenu;
import com.mrcrayfish.backpacked.client.gui.screen.widget.BackpackButtons;
import com.mrcrayfish.backpacked.client.gui.screen.widget.Divider;
import com.mrcrayfish.backpacked.client.gui.screen.widget.Stepper;
import com.mrcrayfish.backpacked.client.gui.screen.widget.TitleWidget;
import com.mrcrayfish.backpacked.client.gui.screen.widget.popup.PopupMenuHandler;
import com.mrcrayfish.backpacked.common.augment.impl.LightweaverAugment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;

public class FeedingMenu extends AugmentSettingsMenu
{
    private static final Component OPTIONS_LABEL = Component.translatable("backpacked.gui.options");
    private static final Component NUTRITION_LABEL = Component.translatable("augment.backpacked.feeding.nutrition");
    private static final Component NUTRITION_TOOLTIP = Component.translatable("augment.backpacked.feeding.nutrition.tooltip");
    private static final Component SATURATION_LABEL = Component.translatable("augment.backpacked.feeding.saturation");
    private static final Component SATURATION_TOOLTIP = Component.translatable("augment.backpacked.feeding.saturation.tooltip");

    private static final int MIN_CONTENT_WIDTH = 130;

    public FeedingMenu(PopupMenuHandler handler, AugmentHolder<FeedingAugment> holder)
    {
        super(handler, menu -> {
            LinearLayout layout = LinearLayout.vertical().spacing(2);
            TitleWidget title = layout.addChild(new TitleWidget(OPTIONS_LABEL, Minecraft.getInstance().font));
            Divider divider = layout.addChild(Divider.horizontal(Math.max(MIN_CONTENT_WIDTH, title.getWidth())).colour(0xFFE0CDB7));
            title.setWidth(divider.getWidth());
            layout.addChild(createOption(NUTRITION_LABEL, NUTRITION_TOOLTIP, BackpackButtons.onOff(() -> {
                        return holder.get().nutrition();
                    }, newValue -> {
                        holder.update(holder.get().useNutrition());
                    })
                    .setSize(60, 18)
                    .build(), divider.getWidth()));
            layout.addChild(createOption(SATURATION_LABEL, SATURATION_TOOLTIP, BackpackButtons.onOff(() -> {
                        return holder.get().saturation();
                    }, newValue -> {
                        holder.update(holder.get().useSaturation());
                    })
                    .setSize(60, 18)
                    .build(), divider.getWidth()));
            return layout;
        });
    }
}