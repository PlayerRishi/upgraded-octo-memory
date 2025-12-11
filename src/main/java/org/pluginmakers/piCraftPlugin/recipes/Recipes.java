package org.pluginmakers.piCraftPlugin.recipes;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;

public class Recipes {
    
    public static void registerRecipe(JavaPlugin plugin) {
        // Cobweb recipe: 9 strings = 1 cobweb
        NamespacedKey cobwebKey = new NamespacedKey(plugin, "craftable_cobweb");
        ItemStack cobweb = new ItemStack(Material.COBWEB);
        
        ShapedRecipe cobwebRecipe = new ShapedRecipe(cobwebKey, cobweb);
        cobwebRecipe.shape("SSS", "SSS", "SSS");
        cobwebRecipe.setIngredient('S', Material.STRING);
        
        plugin.getServer().addRecipe(cobwebRecipe);
        
        // Heart recipe: 4 totems, 4 diamond blocks, 1 nether star
        NamespacedKey heartKey = new NamespacedKey(plugin, "craftable_heart");
        ItemStack heart = ((PiCraftPlugin) plugin).getLifestealManager().createHeartItem();
        
        ShapedRecipe heartRecipe = new ShapedRecipe(heartKey, heart);
        heartRecipe.shape("TBT", "BNT", "TBT");
        heartRecipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        heartRecipe.setIngredient('B', Material.DIAMOND_BLOCK);
        heartRecipe.setIngredient('N', Material.NETHER_STAR);
        
        plugin.getServer().addRecipe(heartRecipe);
    }
}