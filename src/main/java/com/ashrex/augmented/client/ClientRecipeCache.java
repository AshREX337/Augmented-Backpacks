package com.ashrex.augmented.client;

import net.minecraft.resources.Identifier;

import java.util.*;

public class ClientRecipeCache
{
    private static final Map<Identifier, List<List<List<Identifier>>>> RECIPE_INGREDIENTS = new HashMap<>();

    public static void update(Map<Identifier, List<List<List<Identifier>>>> data)
    {
        RECIPE_INGREDIENTS.clear();
        RECIPE_INGREDIENTS.putAll(data);
    }

    public static Set<Identifier> getAllIds()
    {
        return RECIPE_INGREDIENTS.keySet();
    }

    public static List<List<List<Identifier>>> getRecipes(Identifier itemId)
    {
        return RECIPE_INGREDIENTS.getOrDefault(itemId, List.of());
    }
}