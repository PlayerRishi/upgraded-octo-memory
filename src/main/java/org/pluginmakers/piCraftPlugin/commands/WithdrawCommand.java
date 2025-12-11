package org.pluginmakers.piCraftPlugin.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;
import org.pluginmakers.piCraftPlugin.utils.ColorUtil;

public class WithdrawCommand implements CommandExecutor {
    private final PiCraftPlugin plugin;
    private static final int XP_PER_BOTTLE = 11; // XP bottles give 3-11 XP, using 11 as standard
    
    public WithdrawCommand(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.colorize("&cOnly players can use this command."));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length != 1) {
            player.sendMessage(ColorUtil.colorize("&cUsage: /withdraw <amount>"));
            player.sendMessage(ColorUtil.colorize("&7Withdraw XP bottles from your levels (1 bottle = " + XP_PER_BOTTLE + " XP)"));
            return true;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtil.colorize("&cInvalid number: " + args[0]));
            return true;
        }
        
        if (amount <= 0) {
            player.sendMessage(ColorUtil.colorize("&cAmount must be positive."));
            return true;
        }
        
        if (amount > 64) {
            player.sendMessage(ColorUtil.colorize("&cMaximum 64 bottles at once."));
            return true;
        }
        
        int totalXpNeeded = amount * XP_PER_BOTTLE;
        int playerTotalXp = getTotalExperience(player);
        
        if (playerTotalXp < totalXpNeeded) {
            player.sendMessage(ColorUtil.colorize("&cNot enough XP! You need " + totalXpNeeded + " XP but only have " + playerTotalXp + " XP."));
            return true;
        }
        
        // Check inventory space
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ColorUtil.colorize("&cInventory full! Make space first."));
            return true;
        }
        
        // Remove XP from player
        setTotalExperience(player, playerTotalXp - totalXpNeeded);
        
        // Give XP bottles
        ItemStack bottles = new ItemStack(Material.EXPERIENCE_BOTTLE, amount);
        player.getInventory().addItem(bottles);
        
        player.sendMessage(ColorUtil.colorize("&aWithdrew " + amount + " XP bottles (" + totalXpNeeded + " XP)"));
        
        return true;
    }
    
    private int getTotalExperience(Player player) {
        int exp = Math.round(getExpAtLevel(player.getLevel()) * player.getExp());
        int currentLevel = player.getLevel();
        
        for (int level = 0; level < currentLevel; level++) {
            exp += getExpAtLevel(level);
        }
        return exp;
    }
    
    private void setTotalExperience(Player player, int exp) {
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0);
        
        int level = 0;
        int expForLevel = getExpAtLevel(level);
        
        while (exp >= expForLevel) {
            exp -= expForLevel;
            level++;
            expForLevel = getExpAtLevel(level);
        }
        
        player.setLevel(level);
        if (expForLevel > 0) {
            player.setExp((float) exp / expForLevel);
        }
    }
    
    private int getExpAtLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }
}