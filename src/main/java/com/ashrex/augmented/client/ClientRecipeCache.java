// ClientRecipeCache.java
package com.ashrex.augmented.client;

import net.minecraft.resources.Identifier;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class ClientRecipeCache
{
    private static final Set<Identifier> KNOWN_RECIPE_IDS = new HashSet<>();

    public static void update(List<Identifier> ids) {
        KNOWN_RECIPE_IDS.clear();
        KNOWN_RECIPE_IDS.addAll(ids);
    }

    public static boolean hasRecipeFor(Identifier id) {
        return KNOWN_RECIPE_IDS.contains(id);
    }

    public static Set<Identifier> getAllIds() {
        return KNOWN_RECIPE_IDS;
    }
}