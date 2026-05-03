package com.ashrex.augmented.client.augment.menu;

import com.ashrex.augmented.client.gui.widget.SimpleCraftingGrid;
import com.ashrex.augmented.common.augment.impl.CraftingAugment;
import com.mrcrayfish.backpacked.client.augment.AugmentHolder;
import com.mrcrayfish.backpacked.client.augment.AugmentSettingsMenu;
import com.mrcrayfish.backpacked.client.gui.screen.widget.Divider;
import com.mrcrayfish.backpacked.client.gui.screen.widget.TitleWidget;
import com.mrcrayfish.backpacked.client.gui.screen.widget.popup.PopupMenuHandler;
import com.mrcrayfish.backpacked.util.Utils;
import com.mrcrayfish.framework.api.client.screen.widget.FrameworkEditBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;

public class CraftingMenu extends AugmentSettingsMenu
{
    private static final Component SEARCH_HINT = Component.translatable("backpacked.gui.search_hint");
    private static final Component CRAFTABLE_ITEMS_LABEL = Component.translatable("augment.augmented.crafting.craftable_items");

    private static final int MIN_CONTENT_WIDTH = 140;

    private static String lastQuery = "";

    public CraftingMenu(PopupMenuHandler handler, AugmentHolder<CraftingAugment> holder)
    {
        super(handler, menu -> {
            LinearLayout layout = LinearLayout.vertical().spacing(2);

            // Title
            TitleWidget title = layout.addChild(new TitleWidget(CRAFTABLE_ITEMS_LABEL, Minecraft.getInstance().font));
            Divider divider = layout.addChild(Divider.horizontal(Math.max(MIN_CONTENT_WIDTH, title.getWidth())).colour(0xFFE0CDB7));
            title.setWidth(divider.getWidth());

            // Create crafting grid
            SimpleCraftingGrid<CraftingAugment> grid = SimpleCraftingGrid.builder(holder::get, holder::update)
                    .setWidth(divider.getWidth())
                    .setHeight(60)
                    .setInitialQuery(lastQuery)
                    .setPredicate(item -> true)
                    .build();

            // Search field
            LinearLayout header = LinearLayout.horizontal().spacing(3);

            FrameworkEditBox searchField = FrameworkEditBox.builder()
                    .setWidth(divider.getWidth())
                    .setPadding(2, 0, 2, 0)
                    .setHeight(16)
                    .setIcon(Utils.rl("backpack/editbox/search"), 12, 12)
                    .setInitialText(lastQuery)
                    .setHint(SEARCH_HINT)
                    .setCallback(s -> {
                        grid.setSearchQuery(s);
                        lastQuery = s;
                    })
                    .setBackground(new WidgetSprites(
                            Utils.rl("backpack/editbox/background"),
                            Utils.rl("backpack/editbox/background_focused")
                    )).build();

            header.addChild(searchField, LayoutSettings::alignVerticallyMiddle);
            layout.addChild(header);
            layout.addChild(grid);

            return layout;
        });
    }
}