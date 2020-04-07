package com.ivarkronos.trickster;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ivarkronos.trickster.database.TricksterRepository;

public class KitMenuCommand implements CommandExecutor {
	
	private MainPlugin main;
	public KitMenuCommand(MainPlugin mainPlugin) {
		this.main = mainPlugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			createKitsUI((Player) sender);
		}
		return false;
	}
	
	public void createKitsUI (Player player) {
		
		//CREATE MENU
		Inventory gui = Bukkit.createInventory(null, 9, ChatColor.DARK_AQUA + "Player Kits!");
		
		//IS THE KIT ALREADY ENABLED?
		TricksterRepository repo = new TricksterRepository(main);
		boolean isEnabled = false;
		try {
			repo.hasPlayed(player); // Also initializes player in database if first time playing
			isEnabled = repo.isKitEnabled(player);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//CREATE MENU TOGGLE-ITEM
		ItemStack tricksterToggleItem;
		ItemMeta tricksterToggleMeta;
		if (isEnabled) {
			tricksterToggleItem = new ItemStack(Material.REDSTONE_BLOCK);
			tricksterToggleMeta = tricksterToggleItem.getItemMeta();
			tricksterToggleMeta.setDisplayName(ChatColor.DARK_RED + "Disable Trickster!");
		} else {
			tricksterToggleItem = new ItemStack(Material.EMERALD_BLOCK);
			tricksterToggleMeta = tricksterToggleItem.getItemMeta();
			tricksterToggleMeta.setDisplayName(ChatColor.GREEN + "Enable Trickster!");
		}
		tricksterToggleItem.setItemMeta(tricksterToggleMeta);
		
		//PLACE TOGGLE-ITEM IN MENU
		gui.setItem(4, tricksterToggleItem);
		
		//OPEN MENU
		player.openInventory(gui);
	}

	
	
}
