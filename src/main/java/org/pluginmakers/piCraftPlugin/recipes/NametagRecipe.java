package org.pluginmakers.piCraftPlugin.recipes;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class NametagRecipe {
    
    public static void registerRecipe(JavaPlugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "craftable_nametag");
        ItemStack nametag = new ItemStack(Material.NAME_TAG);
        
        ShapedRecipe recipe = new ShapedRecipe(key, nametag);
        recipe.shape(" S ", " P ", "   ");
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('P', Material.PAPER);
        
        plugin.getServer().addRecipe(recipe);
    }
}