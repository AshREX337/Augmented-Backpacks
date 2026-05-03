// SimpleCraftingGrid.java
package com.ashrex.augmented.client.gui.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.backpacked.client.gui.StateSprites;
import com.mrcrayfish.backpacked.common.FilterableItems;
import com.mrcrayfish.backpacked.util.ScreenUtil;
import com.mrcrayfish.backpacked.util.Utils;
import com.mrcrayfish.framework.api.client.screen.widget.FrameworkSelectionList;
import com.mrcrayfish.framework.api.client.screen.widget.layout.Border;
import com.mrcrayfish.framework.api.client.screen.widget.layout.Padding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class SimpleCraftingGrid<T extends FilterableItems<T>> extends FrameworkSelectionList
{
    private static final ResourceLocation LIST_BACKGROUND_SPRITE = Utils.rl("backpack/list/background");
    private static final StateSprites ITEM_SPRITES = new StateSprites(
            Utils.rl("backpack/list/item"),
            Utils.rl("backpack/list/item_hovered"),
            Utils.rl("backpack/list/item_selected"),
            Utils.rl("backpack/list/item_selected")
    );
    private static final ScrollerSprites SCROLLER_SPRITES = ScrollerSprites.of(
            Utils.rl("backpack/list/scroll_bar"),
            Utils.rl("backpack/list/scroll_bar"),
            Utils.rl("backpack/list/scroll_bar_hovered"),
            Utils.rl("backpack/list/scroll_bar_selected")
    );
    private static final int CLICK_ANIMATION_DURATION = 10; // ticks
    private static final int UPDATE_INTERVAL = 40; // Update every 40 ticks (2 seconds)

    // Static recipe caches - shared across all instances
    private static List<RecipeHolder<CraftingRecipe>> ALL_RECIPES_CACHE = null;
    private static Map<net.minecraft.world.item.Item, List<RecipeHolder<CraftingRecipe>>> RECIPES_BY_RESULT_CACHE = null;
    private static long LAST_RECIPE_UPDATE = 0;

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

        // Setup list styling
        this.setRenderHeader(false, 0);
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

    private void updateRecipeCache() {
        if (mc.level == null) return;

        // Only update recipe cache if it's null or it's been a while
        if (ALL_RECIPES_CACHE == null || System.currentTimeMillis() - LAST_RECIPE_UPDATE > 30000) {
            ALL_RECIPES_CACHE = mc.level.getRecipeManager()
                    .getAllRecipesFor(RecipeType.CRAFTING);

            RECIPES_BY_RESULT_CACHE = new HashMap<>();
            for (RecipeHolder<CraftingRecipe> recipeHolder : ALL_RECIPES_CACHE) {
                ItemStack result = recipeHolder.value().getResultItem(mc.level.registryAccess());
                if (!result.isEmpty()) {
                    net.minecraft.world.item.Item item = result.getItem();
                    RECIPES_BY_RESULT_CACHE
                            .computeIfAbsent(item, k -> new ArrayList<>())
                            .add(recipeHolder);
                }
            }

            LAST_RECIPE_UPDATE = System.currentTimeMillis();
            needsFullRebuild = true;
        }
    }

    private void updateInventoryCounts()
    {
        int oldTotal = totalInventoryItems;
        inventoryCounts.clear();
        totalInventoryItems = 0;

        if(mc.player == null) return;

        // Count items in player inventory
        for(int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if(!stack.isEmpty()) {
                net.minecraft.world.item.Item item = stack.getItem();
                int count = stack.getCount();
                inventoryCounts.put(item, inventoryCounts.getOrDefault(item, 0) + count);
                totalInventoryItems += count;
            }
        }

        // Mark for rebuild if inventory changed significantly
        if (Math.abs(totalInventoryItems - oldTotal) > 1) {
            needsFullRebuild = true;
        }
    }

    private void rebuildCraftableCache() {
        if (mc.level == null || ALL_RECIPES_CACHE == null) return;

        craftableItemsCache.clear();
        Set<net.minecraft.world.item.Item> processedItems = new HashSet<>();

        // Instead of checking each item, check each recipe to see if it's craftable
        // This is O(n) where n = number of recipes, instead of O(n*m) where n = items, m = recipes
        for (RecipeHolder<CraftingRecipe> recipeHolder : ALL_RECIPES_CACHE) {
            CraftingRecipe recipe = recipeHolder.value();
            ItemStack result = recipe.getResultItem(mc.level.registryAccess());
            net.minecraft.world.item.Item item = result.getItem();

            if (result.isEmpty() || processedItems.contains(item)) {
                continue;
            }

            // Skip problematic items
            if (item == Items.FIREWORK_ROCKET || item == Items.FIREWORK_STAR ||
                    item == Items.FIRE_CHARGE || item == Items.TIPPED_ARROW ||
                    item == Items.SPECTRAL_ARROW) {
                continue;
            }

            if (canCraftRecipe(recipe)) {
                craftableItemsCache.add(item);
                processedItems.add(item);
            }
        }

        // Sort by name
        craftableItemsCache.sort(Comparator.comparing(item -> item.getDescription().getString()));
    }

    private void updateList()
    {
        String search = this.searchQuery.toLowerCase().trim();
        boolean empty = search.trim().isBlank();
        this.clearEntries();

        // Rebuild cache if needed
        if (needsFullRebuild || !search.equals(lastSearchQuery)) {
            if (needsFullRebuild) {
                rebuildCraftableCache();
                needsFullRebuild = false;
            }
            lastSearchQuery = search;
        }

        List<net.minecraft.world.item.Item> filteredItems = new ArrayList<>();

        // Filter by search
        for (net.minecraft.world.item.Item item : craftableItemsCache) {
            if (empty || item.getDescription().getString().toLowerCase(Locale.ROOT).contains(search)) {
                filteredItems.add(item);
            }
        }

        // Sort by name (already sorted in cache, but re-sort filtered results)
        filteredItems.sort(Comparator.comparing(item -> item.getDescription().getString()));

        // Create rows
        int chunkSize = (this.getRowWidth() + this.spacing) / (this.itemSize + this.spacing);
        for(int i = 0; i < Mth.positiveCeilDiv(filteredItems.size(), chunkSize); i++)
        {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, filteredItems.size());
            this.addEntry(new CraftingRow(this, filteredItems.subList(start, end)));
        }
    }

    private boolean isItemCraftable(net.minecraft.world.item.Item item)
    {
        if (RECIPES_BY_RESULT_CACHE == null) return false;

        List<RecipeHolder<CraftingRecipe>> recipes = RECIPES_BY_RESULT_CACHE.get(item);
        if (recipes == null) return false;

        for (RecipeHolder<CraftingRecipe> recipeHolder : recipes) {
            if (canCraftRecipe(recipeHolder.value())) {
                return true;
            }
        }

        return false;
    }

    private boolean canCraftRecipe(CraftingRecipe recipe)
    {
        Map<net.minecraft.world.item.Item, Integer> availableCounts = new HashMap<>(inventoryCounts);

        for(Ingredient ingredient : recipe.getIngredients()) {
            if(ingredient.isEmpty()) continue;

            ItemStack[] matching = ingredient.getItems();
            if(matching.length == 0) continue;

            boolean found = false;
            for(ItemStack stack : matching) {
                net.minecraft.world.item.Item item = stack.getItem();
                Integer count = availableCounts.get(item);

                if(count != null && count > 0) {
                    availableCounts.put(item, count - 1);
                    found = true;
                    break;
                }
            }

            if(!found) {
                return false;
            }
        }

        return true;
    }

    public void setSearchQuery(String searchQuery)
    {
        if(!this.searchQuery.equals(searchQuery))
        {
            this.searchQuery = searchQuery;
            this.updateList();
            this.clampScrollAmount();
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        // Update click animation
        if(clickAnimationTicks > 0) {
            clickAnimationTicks--;
            if(clickAnimationTicks == 0) {
                clickedItem = null;
            }
        }

        // Throttle updates
        if(++updateCounter >= UPDATE_INTERVAL) {
            updateCounter = 0;

            if (mc.level != null) {
                updateRecipeCache();
            }

            // Quick inventory check
            boolean inventoryChanged = false;
            if(mc.player != null) {
                int tempTotal = 0;
                for(int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = mc.player.getInventory().getItem(i);
                    if(!stack.isEmpty()) {
                        tempTotal += stack.getCount();
                    }
                }

                // Only do full update if total item count changed
                if (tempTotal != totalInventoryItems) {
                    updateInventoryCounts();
                    inventoryChanged = true;
                }
            }

            // Update list if needed
            if (inventoryChanged || needsFullRebuild) {
                updateList();
            }
        }

        this.hoveredStack = null;
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        if(ScreenUtil.isPointInArea(mouseX, mouseY, this.getX(), this.getY() + this.listBorder.top(),
                this.getWidth(), this.getHeight() - this.listBorder.top() - this.listBorder.bottom()) &&
                this.hoveredStack != null)
        {
            graphics.renderTooltip(Minecraft.getInstance().font, this.hoveredStack, mouseX, mouseY);
        }
    }

    @Override
    public void setSelected(@Nullable FrameworkSelectionList.Item item) {}

    private void setClickedItem(net.minecraft.world.item.Item item)
    {
        this.clickedItem = item;
        this.clickAnimationTicks = CLICK_ANIMATION_DURATION;
    }

    // Custom Row class
    public class CraftingRow extends FrameworkSelectionList.Item
    {
        private final SimpleCraftingGrid<T> parent;
        private final List<ItemStack> display;
        private int top, left;

        public CraftingRow(SimpleCraftingGrid<T> parent, List<net.minecraft.world.item.Item> items)
        {
            this.parent = parent;
            this.display = items.stream().map(ItemStack::new).collect(ImmutableList.toImmutableList());
        }

        @Override
        protected void renderContent(GuiGraphics graphics, int index, int x, int y, int width, int height,
                                     int mouseX, int mouseY, boolean hovered, boolean selected, float partialTick)
        {
            this.top = y;
            this.left = x;

            boolean active = this.parent.isActive();
            for(int i = 0; i < this.display.size(); i++)
            {
                ItemStack stack = this.display.get(i);
                int offset = i * (parent.itemSize + parent.spacing);

                // Check if this item is in the click animation
                boolean itemClicked = parent.clickedItem != null && parent.clickedItem == stack.getItem();
                boolean itemHovered = active && ScreenUtil.isPointInArea(mouseX, mouseY,
                        left + offset - parent.spacing/2, top - parent.spacing/2,
                        parent.itemSize + parent.spacing, parent.itemSize + parent.spacing);

                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
                graphics.setColor(1, 1, 1, active ? 1.0F : 0.5F);
                // Show clicked state during animation, otherwise show hover state
                graphics.blitSprite(ITEM_SPRITES.get(itemClicked, itemHovered),
                        left + offset, top, parent.itemSize, parent.itemSize);
                graphics.setColor(1, 1, 1, 1);
                RenderSystem.disableBlend();

                graphics.renderFakeItem(stack, left + offset + (parent.itemSize - 16) / 2,
                        top + (parent.itemSize - 16) / 2);

                if(itemHovered)
                {
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
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                int itemSize = parent.itemSize;
                int spacing = parent.spacing;
                int halfSpacing = spacing / 2;

                for(int i = 0; i < this.display.size(); i++)
                {
                    int offset = i * (itemSize + spacing);
                    if(!ScreenUtil.isPointInArea((int) mouseX, (int) mouseY,
                            this.left + offset - halfSpacing, this.top - halfSpacing,
                            itemSize + spacing, itemSize + spacing))
                        continue;

                    ItemStack stack = this.display.get(i);

                    // Show click animation
                    parent.setClickedItem(stack.getItem());

                    // Try to craft the item - optimized version
                    tryCraftItemOptimized(stack.getItem());

                    Minecraft.getInstance().getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                    return true;
                }
            }
            return false;
        }

        private void tryCraftItemOptimized(net.minecraft.world.item.Item item)
        {
            if(parent.mc.level == null) {
                return;
            }

            if(parent.mc.player == null) {
                return;
            }

            parent.updateRecipeCache();

            if (RECIPES_BY_RESULT_CACHE == null) {
                parent.mc.player.displayClientMessage(
                        Component.translatable("augment.ashrex_augmented.crafting.no_recipe"), true);
                return;
            }

            List<RecipeHolder<CraftingRecipe>> recipes = RECIPES_BY_RESULT_CACHE.get(item);
            if(recipes == null || recipes.isEmpty()) {
                parent.mc.player.displayClientMessage(
                        Component.translatable("augment.ashrex_augmented.crafting.no_recipe"), true);
                return;
            }

            // Find the first craftable recipe
            RecipeHolder<CraftingRecipe> craftableRecipe = null;
            for(RecipeHolder<CraftingRecipe> recipeHolder : recipes) {
                if(parent.canCraftRecipe(recipeHolder.value())) {
                    craftableRecipe = recipeHolder;
                    break;
                }
            }

            if(craftableRecipe == null) {
                parent.mc.player.displayClientMessage(
                        Component.translatable("augment.ashrex_augmented.crafting.cannot_craft"), true);
                return;
            }

            // Send packet to server to craft
            try {
                com.ashrex.augmented.network.CraftItemPacket packet =
                        new com.ashrex.augmented.network.CraftItemPacket(craftableRecipe.id());
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(packet);

                // Mark for rebuild after crafting
                parent.needsFullRebuild = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
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