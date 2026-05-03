package com.ashrex.augmented.client.augment.menu;

import com.ashrex.augmented.AugmentedMod;
import com.ashrex.augmented.common.augment.impl.ExperienceAugment;
import com.mrcrayfish.backpacked.client.augment.AugmentHolder;
import com.mrcrayfish.backpacked.client.augment.AugmentSettingsMenu;
import com.mrcrayfish.backpacked.client.gui.screen.widget.*;
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

public class ExperienceMenu extends AugmentSettingsMenu
{
    private static final Component OPTIONS_LABEL = Component.translatable("augment.backpacked.experience.title");

    private static final int MIN_CONTENT_WIDTH = 130;

    public ExperienceMenu(PopupMenuHandler handler, AugmentHolder<ExperienceAugment> holder)
    {
        super(handler, menu -> {
            LinearLayout layout = LinearLayout.vertical().spacing(2);
            TitleWidget title = layout.addChild(new TitleWidget(OPTIONS_LABEL, Minecraft.getInstance().font));
            Divider divider = layout.addChild(Divider.horizontal(Math.max(MIN_CONTENT_WIDTH, title.getWidth())).colour(0xFFE0CDB7));
            title.setWidth(divider.getWidth());
            layout.addChild(new ExperienceProgress(divider.getWidth(), 20, holder));
            return layout;
        });
    }

    public static class ExperienceProgress extends AbstractWidget
    {
        private final AugmentHolder<ExperienceAugment> holder;
        private static final ResourceLocation BACKGROUND_SPRITE = Utils.rl("backpack/stepper_background");
        private static final ResourceLocation READY_SPRITE = Utils.rl("backpack/immortal_ready");

        public ExperienceProgress(int width, int height, AugmentHolder<ExperienceAugment> holder)
        {
            super(0, 0, width, height, CommonComponents.EMPTY);
            this.holder = holder;
            AugmentedMod.LOGGER.info("" + holder.get().max());
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
        {
            int experience = holder.get().experience();
            int max = holder.get().max();

            // Calculate fill percentage
            float percent = Math.min(1.0f, (float) experience / max);
            int fillWidth = (int) (this.getWidth() * percent);

            // Draw stepper background (full width)
            graphics.blitSprite(BACKGROUND_SPRITE, this.getX(), this.getY(), this.getWidth(), this.getHeight());

            // Draw ready sprite as the fill (clipped to fillWidth)
            if (fillWidth > 0) {
                graphics.blitSprite(READY_SPRITE, this.getX(), this.getY(), fillWidth, this.getHeight());
            }

            // Draw text
            String text = String.valueOf(experience);
            int textWidth = Minecraft.getInstance().font.width(text);
            int textX = this.getX() + (this.getWidth() - textWidth) / 2;
            int textY = this.getY() + (this.getHeight() - 8) / 2;
            graphics.drawString(Minecraft.getInstance().font, text, textX, textY, 0xFFFFFFFF);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {}
    }
}