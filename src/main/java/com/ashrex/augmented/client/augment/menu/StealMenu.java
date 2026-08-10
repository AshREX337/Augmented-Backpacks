package com.ashrex.augmented.client.augment.menu;

import com.ashrex.augmented.common.augment.impl.FeedingAugment;
import com.ashrex.augmented.common.augment.impl.StealAugment;
import com.mrcrayfish.backpacked.client.augment.AugmentHolder;
import com.mrcrayfish.backpacked.client.augment.AugmentSettingsMenu;
import com.mrcrayfish.backpacked.client.gui.screen.widget.BackpackButtons;
import com.mrcrayfish.backpacked.client.gui.screen.widget.Divider;
import com.mrcrayfish.backpacked.client.gui.screen.widget.TitleWidget;
import com.mrcrayfish.backpacked.client.gui.screen.widget.popup.PopupMenuHandler;
import com.mrcrayfish.backpacked.util.Utils;
import com.mrcrayfish.framework.api.client.screen.widget.Buttons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;

public class StealMenu extends AugmentSettingsMenu
{
    private static final Component OPTIONS_LABEL = Component.translatable("backpacked.gui.options");
    private static final Component MODE_LABEL = Component.translatable("augment.backpacked.stealing.sound");
    private static final Component MODE_LABEL_TOOLTIP = Component.translatable("augment.backpacked.stealing.sound.tooltip");
    private static final Component PROTECT_LABEL = Component.translatable("augment.backpacked.stealing.protect");
    private static final Component PROTECT_INACTIVE = Component.translatable("augment.backpacked.stealing.protect.inactive");
    private static final Component PROTECT_ACTIVE = Component.translatable("augment.backpacked.stealing.protect.active");

    private static final int MIN_CONTENT_WIDTH = 120;

    public StealMenu(PopupMenuHandler handler, AugmentHolder<StealAugment> holder)
    {
        super(handler, menu -> {
            LinearLayout layout = LinearLayout.vertical().spacing(2);
            LinearLayout header = LinearLayout.horizontal().spacing(3);
            TitleWidget title = layout.addChild(new TitleWidget(OPTIONS_LABEL, Minecraft.getInstance().font));
            Divider divider = layout.addChild(Divider.horizontal(Math.max(MIN_CONTENT_WIDTH, title.getWidth())).colour(0xFFE0CDB7));
            title.setWidth(divider.getWidth());
            header.addChild(createOption(MODE_LABEL, MODE_LABEL_TOOLTIP, BackpackButtons.values(() -> holder.get().sound(), value -> holder.update(holder.get().switchMode(value)), filterMode -> {}).setSize(60, 18).build(), divider.getWidth()));
            header.addChild(Buttons.createToggle(() -> holder.get().evil(), newValue -> holder.update(holder.get().setEvil(newValue)))
                    .setSize(60, 18)
                    .setSpacing(2)
                    .setTooltip(btn -> Tooltip.create(holder.get().evil() ? PROTECT_ACTIVE : PROTECT_INACTIVE))
                    .setLabel(PROTECT_LABEL)
                    .setTexture(new WidgetSprites(
                            Utils.rl("backpack/button_enabled"),
                            Utils.rl("backpack/button_enabled_focused")
                    )).build());
            layout.addChild(header);
            return layout;
        });
    }
}