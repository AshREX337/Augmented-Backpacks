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
    private static final Component MODE_LABEL = Component.translatable("augment.backpacked.feeding.mode");
    private static final Component MODE_LABEL_TOOLTIP = Component.translatable("augment.backpacked.feeding.mode.tooltip");

    private static final int MIN_CONTENT_WIDTH = 160;

    public FeedingMenu(PopupMenuHandler handler, AugmentHolder<FeedingAugment> holder)
    {
        super(handler, menu -> {
            LinearLayout layout = LinearLayout.vertical().spacing(2);
            TitleWidget title = layout.addChild(new TitleWidget(OPTIONS_LABEL, Minecraft.getInstance().font));
            Divider divider = layout.addChild(Divider.horizontal(Math.max(MIN_CONTENT_WIDTH, title.getWidth())).colour(0xFFE0CDB7));
            title.setWidth(divider.getWidth());
            layout.addChild(createOption(MODE_LABEL, MODE_LABEL_TOOLTIP, BackpackButtons.values(() -> holder.get().mode(), value -> holder.update(holder.get().switchMode(value)), filterMode -> {}).setSize(60, 18).build(), divider.getWidth()));
            return layout;
        });
    }
}