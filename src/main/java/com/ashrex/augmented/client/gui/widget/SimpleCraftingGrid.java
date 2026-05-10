package com.ashrex.augmented.client.gui.widget;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.backpacked.client.gui.StateSprites;
import com.mrcrayfish.backpacked.common.FilterableItems;
import com.mrcrayfish.backpacked.util.ScreenUtil;
import com.mrcrayfish.backpacked.util.Utils;
import com.mrcrayfish.framework.api.client.screen.widget.FrameworkSelectionList;
import com.mrcrayfish.framework.api.client.screen.widget.layout.Border;
import com.mrcrayfish.framework.api.client.screen.widget.layout.Padding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SimpleCraftingGrid<T extends FilterableItems<T>> extends FrameworkSelectionList
{
    private static final Identifier LIST_BACKGROUND_SPRITE = Utils.id("backpack/list/background");
    private static final StateSprites ITEM_SPRITES = new StateSprites(
            Utils.id("backpack/list/item"),
            Utils.id("backpack/list/item_hovered"),
            Utils.id("backpack/list/item_selected"),
            Utils.id("backpack/list/item_selected")
    );
    private static final ScrollerSprites SCROLLER_SPRITES = ScrollerSprites.of(
            Utils.id("backpack/list/scroll_bar"),
            Utils.id("backpack/list/scroll_bar"),
            Utils.id("backpack/list/scroll_bar_hovered"),
            Utils.id("backpack/list/scroll_bar_selected")
    );
    private static final int CLICK_ANIMATION_DURATION = 10;
    private static final int UPDATE_INTERVAL = 40;

    private final Supplier<T> supplier;
    private final Consumer<T> updater;
    private final Minecraft mc;
    private final int itemSize;
    private final int spacing;
    private final List<net.minecraft.world.item.Item> allItems;
    private String searchQuery;
    private @Nullable ItemStack hoveredStack;
    private final Map<net.minecraft.world.item.Item, Integer> inventoryCounts = new HashMap<>();
    private @Nullable net.minecraft.world.item.Item clickedItem;
    private int clickAnimationTicks;
    private int updateCounter = 0;
    private boolean needsFullRebuild = true;
    private List<net.minecraft.world.item.Item> craftableItemsCache = new ArrayList<>();
    private String lastSearchQuery = "";
    private int totalInventoryItems = 0;

    public SimpleCraftingGrid(Supplier<T> supplier, Consumer<T> updater, int width, int height,
                              String lastQuery, Predicate<net.minecraft.world.item.Item> predicate)
    {
        super(width, height, 0, 0, 18);
        this.supplier = supplier;
        this.updater = updater;
        this.mc = Minecraft.getInstance();
        this.itemSize = 18;
        this.spacing = 2;
        this.allItems = BuiltInRegistries.ITEM.stream().filter(predicate).collect(ImmutableList.toImmutableList());
        this.searchQuery = lastQuery;

        this.listBackground = LIST_BACKGROUND_SPRITE;
        this.scrollBarBackground = LIST_BACKGROUND_SPRITE;
        this.scrollBarBorder = Border.of(1);
        this.scrollBarPadding = Padding.of(spacing);
        this.scrollBarSpacing = spacing;
        this.scrollerSprites = SCROLLER_SPRITES;
        this.listBorder = Border.of(1);
        this.listPadding = Padding.of(spacing);
        this.itemSpacing = spacing;
        this.scrollerWidth = 10;
        this.scrollBarStyle = ScrollBarStyle.DETACHED;
        this.scrollBarAlwaysVisible = true;

        updateRecipeCache();
        updateInventoryCounts();
        rebuildCraftableCache();
        updateList();
    }

    private void updateRecipeCache()
    {
    }

    private void updateInventoryCounts()
    {
        int oldTotal = totalInventoryItems;
        inventoryCounts.clear();
        totalInventoryItems = 0;

        if (mc.player == null) return;

        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                net.minecraft.world.item.Item item = stack.getItem();
                int count = stack.getCount();
                inventoryCounts.put(item, inventoryCounts.getOrDefault(item, 0) + count);
                totalInventoryItems += count;
            }
        }

        if (Math.abs(totalInventoryItems - oldTotal) > 1) {
            needsFullRebuild = true;
        }
    }

    private void rebuildCraftableCache() {
        craftableItemsCache.clear();
        Set<net.minecraft.world.item.Item> knownItems = new HashSet<>();
        for (Identifier id : com.ashrex.augmented.client.ClientRecipeCache.getAllIds()) {
            net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item == null || item == Items.AIR || knownItems.contains(item)) continue;
            if (canCraftItem(item)) {
                craftableItemsCache.add(item);
                knownItems.add(item);
            }
        }
        craftableItemsCache.sort(Comparator.comparing(item -> item.getName().getString()));
    }

    private void updateList()
    {
        String search = this.searchQuery.toLowerCase().trim();
        boolean empty = search.isBlank();
        this.clearEntries();

        if (needsFullRebuild || !search.equals(lastSearchQuery)) {
            if (needsFullRebuild) {
                rebuildCraftableCache();
                needsFullRebuild = false;
            }
            lastSearchQuery = search;
        }

        List<net.minecraft.world.item.Item> filteredItems = new ArrayList<>();

        for (net.minecraft.world.item.Item item : craftableItemsCache) {
            if (empty || item.getName().getString().toLowerCase(Locale.ROOT).contains(search)) {
                filteredItems.add(item);
            }
        }

        filteredItems.sort(Comparator.comparing(item -> item.getName().getString()));

        int chunkSize = (this.getRowWidth() + this.spacing) / (this.itemSize + this.spacing);
        for (int i = 0; i < Mth.positiveCeilDiv(filteredItems.size(), chunkSize); i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, filteredItems.size());
            this.addEntry(new CraftingRow(this, filteredItems.subList(start, end)));
        }
    }

    private boolean canCraftItem(net.minecraft.world.item.Item item)
    {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        List<List<List<Identifier>>> recipes = com.ashrex.augmented.client.ClientRecipeCache.getRecipes(itemId);
        if (recipes.isEmpty()) return false;

        Map<Identifier, Integer> available = new HashMap<>();
        for (Map.Entry<net.minecraft.world.item.Item, Integer> entry : inventoryCounts.entrySet()) {
            available.put(BuiltInRegistries.ITEM.getKey(entry.getKey()), entry.getValue());
        }

        for (List<List<Identifier>> recipe : recipes) {
            Map<Identifier, Integer> copy = new HashMap<>(available);
            boolean canCraft = true;
            for (List<Identifier> slot : recipe) {
                // Find any valid item from this slot in inventory
                boolean slotSatisfied = false;
                for (Identifier validItem : slot) {
                    int count = copy.getOrDefault(validItem, 0);
                    if (count > 0) {
                        copy.put(validItem, count - 1);
                        slotSatisfied = true;
                        break;
                    }
                }
                if (!slotSatisfied) { canCraft = false; break; }
            }
            if (canCraft) return true;
        }
        return false;
    }

    public void setSearchQuery(String searchQuery)
    {
        if (!this.searchQuery.equals(searchQuery)) {
            this.searchQuery = searchQuery;
            this.updateList();
            this.refreshScrollAmount();
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        if (clickAnimationTicks > 0) {
            clickAnimationTicks--;
            if (clickAnimationTicks == 0) {
                clickedItem = null;
            }
        }

        if (++updateCounter >= UPDATE_INTERVAL) {
            updateCounter = 0;

            if (mc.level != null) {
                updateRecipeCache();
            }

            boolean inventoryChanged = false;
            if (mc.player != null) {
                int tempTotal = 0;
                for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = mc.player.getInventory().getItem(i);
                    if (!stack.isEmpty()) {
                        tempTotal += stack.getCount();
                    }
                }

                if (tempTotal != totalInventoryItems) {
                    updateInventoryCounts();
                    inventoryChanged = true;
                }
            }

            if (inventoryChanged || needsFullRebuild) {
                updateList();
            }
        }

        this.hoveredStack = null;
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        if (ScreenUtil.isPointInArea(mouseX, mouseY, this.getX(), this.getY() + this.listBorder.top(),
                this.getWidth(), this.getHeight() - this.listBorder.top() - this.listBorder.bottom()) &&
                this.hoveredStack != null)
        {
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font, this.hoveredStack, mouseX, mouseY);
        }
    }

    @Override
    public void setSelected(@Nullable Item item) {}

    private void setClickedItem(net.minecraft.world.item.Item item)
    {
        this.clickedItem = item;
        this.clickAnimationTicks = CLICK_ANIMATION_DURATION;
    }

    public class CraftingRow extends Item
    {
        private final SimpleCraftingGrid<T> parent;
        private final List<ItemStack> display;

        public CraftingRow(SimpleCraftingGrid<T> parent, List<net.minecraft.world.item.Item> items)
        {
            this.parent = parent;
            this.display = items.stream().map(ItemStack::new).collect(ImmutableList.toImmutableList());
        }

        @Override
        protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, boolean selected, float partialTick)
        {
            int itemSize = this.parent.itemSize;
            int spacing = this.parent.spacing;
            int halfSpacing = spacing / 2;
            boolean active = this.parent.isActive();

            for (int i = 0; i < this.display.size(); i++) {
                ItemStack stack = this.display.get(i);
                int offset = i * (itemSize + spacing);

                boolean itemClicked = parent.clickedItem != null && parent.clickedItem == stack.getItem();
                boolean itemHovered = active && ScreenUtil.isPointInArea(mouseX, mouseY,
                        this.getContentX() + offset - halfSpacing, this.getContentY() - halfSpacing,
                        itemSize + spacing, itemSize + spacing);

                int alpha = ARGB.white(active ? 1.0F : 0.5F);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ITEM_SPRITES.get(itemClicked, itemHovered),
                        this.getContentX() + offset, this.getContentY(), itemSize, itemSize, alpha);
                graphics.renderFakeItem(stack, this.getContentX() + offset + (itemSize - 16) / 2,
                        this.getContentY() + (itemSize - 16) / 2);

                if (itemHovered) {
                    parent.hoveredStack = stack;
                }
            }
        }

        @Override
        public Component getNarration()
        {
            return CommonComponents.EMPTY;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
        {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                int itemSize = parent.itemSize;
                int spacing = parent.spacing;
                int halfSpacing = spacing / 2;

                for (int i = 0; i < this.display.size(); i++) {
                    int offset = i * (itemSize + spacing);
                    if (!ScreenUtil.isPointInArea((int) event.x(), (int) event.y(),
                            this.getContentX() + offset - halfSpacing, this.getContentY() - halfSpacing,
                            itemSize + spacing, itemSize + spacing))
                        continue;

                    ItemStack stack = this.display.get(i);
                    parent.setClickedItem(stack.getItem());
                    tryCraftItem(stack.getItem());

                    Minecraft.getInstance().getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }
            return false;
        }

        private void tryCraftItem(net.minecraft.world.item.Item item)
        {
            if (parent.mc.level == null || parent.mc.player == null) return;

            Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
            com.ashrex.augmented.network.CraftItemPacket packet =
                    new com.ashrex.augmented.network.CraftItemPacket(itemId);
            parent.mc.getConnection().send(packet);
            parent.needsFullRebuild = true;
        }
    }

    public static <T extends FilterableItems<T>> Builder<T> builder(Supplier<T> supplier, Consumer<T> updater)
    {
        return new Builder<>(supplier, updater);
    }

    public static class Builder<T extends FilterableItems<T>>
    {
        private final Supplier<T> supplier;
        private final Consumer<T> updater;
        private int width = 140;
        private int height = 60;
        private String initialQuery = "";
        private Predicate<net.minecraft.world.item.Item> predicate = item -> true;

        public Builder(Supplier<T> supplier, Consumer<T> updater)
        {
            this.supplier = supplier;
            this.updater = updater;
        }

        public Builder<T> setWidth(int width)
        {
            this.width = width;
            return this;
        }

        public Builder<T> setHeight(int height)
        {
            this.height = height;
            return this;
        }

        public Builder<T> setInitialQuery(String initialQuery)
        {
            this.initialQuery = initialQuery;
            return this;
        }

        public Builder<T> setPredicate(Predicate<net.minecraft.world.item.Item> predicate)
        {
            this.predicate = predicate;
            return this;
        }

        public SimpleCraftingGrid<T> build()
        {
            return new SimpleCraftingGrid<>(this.supplier, this.updater,
                    this.width, this.height, this.initialQuery, this.predicate);
        }
    }
}