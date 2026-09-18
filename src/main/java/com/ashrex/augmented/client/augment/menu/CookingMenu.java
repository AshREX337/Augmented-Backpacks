package com.ashrex.augmented.client.augment.menu;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.common.augment.impl.CookingAugment;
import com.ashrex.augmented.common.augment.impl.ExperienceAugment;
import com.mrcrayfish.backpacked.client.augment.AugmentHolder;
import com.mrcrayfish.backpacked.client.augment.AugmentSettingsMenu;
import com.mrcrayfish.backpacked.client.gui.screen.widget.Divider;
import com.mrcrayfish.backpacked.client.gui.screen.widget.TitleWidget;
import com.mrcrayfish.backpacked.client.gui.screen.widget.popup.PopupMenuHandler;
import com.mrcrayfish.backpacked.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CookingMenu extends AugmentSettingsMenu
{
    private static final Component OPTIONS_LABEL = Component.translatable("augment.backpacked.cooking.title");

    private static final int MIN_CONTENT_WIDTH = 130;

    public CookingMenu(PopupMenuHandler handler, AugmentHolder<CookingAugment> holder)
    {
        super(handler, menu -> {
            LinearLayout layout = LinearLayout.vertical().spacing(2);
            TitleWidget title = layout.addChild(new TitleWidget(OPTIONS_LABEL, Minecraft.getInstance().font));
            Divider divider = layout.addChild(Divider.horizontal(Math.max(MIN_CONTENT_WIDTH, title.getWidth())).colour(0xFFE0CDB7));
            title.setWidth(divider.getWidth());
            layout.addChild(new CookingProgress(divider.getWidth(), 20, holder));
            return layout;
        });
    }

    public static class CookingProgress extends AbstractWidget
    {
        private final AugmentHolder<CookingAugment> holder;
        private static final ResourceLocation BACKGROUND_SPRITE = Utils.rl("backpack/stepper_background");
        private static final ResourceLocation READY_SPRITE = Utils.rl("backpack/immortal_on_cooldown");

        public CookingProgress(int width, int height, AugmentHolder<CookingAugment> holder)
        {
            super(0, 0, width, height, CommonComponents.EMPTY);
            this.holder = holder;
            AugmentedMod.LOGGER.info("" + holder.get().max());
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
        {
            int steps = holder.get().steps();
            int max = holder.get().max();

            float percent = Math.min(1.0f, (float) steps / max);
            int fillWidth = (int) (this.getWidth() * percent);

            graphics.blitSprite(BACKGROUND_SPRITE, this.getX(), this.getY(), this.getWidth(), this.getHeight());

            if (fillWidth > 0) {
                graphics.blitSprite(READY_SPRITE, this.getX(), this.getY(), fillWidth, this.getHeight());
            }

            String text = String.valueOf(steps);
            int textWidth = Minecraft.getInstance().font.width(text);
            int textX = this.getX() + (this.getWidth() - textWidth) / 2;
            int textY = this.getY() + (this.getHeight() - 8) / 2;
            graphics.drawString(Minecraft.getInstance().font, text, textX, textY, 0xFFFFFFFF);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {}
    }
}