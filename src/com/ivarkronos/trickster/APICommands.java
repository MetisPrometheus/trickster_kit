package com.ivarkronos.trickster;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ivarkronos.trickster.database.TricksterRepository;
import com.ivarkronos.trickster.gear.TricksterItemFactory;

public class APICommands {

	MainPlugin plugin;
	
	public APICommands(MainPlugin plugin) {
		this.plugin = plugin;
	}
	
	public void enableKit(Player player) throws SQLException {
		TricksterItemFactory itemFactory = new TricksterItemFactory(plugin);
		TricksterRepository database = new TricksterRepository(plugin);
		database.hasPlayed(player);
		database.toggleKitEnabled(player);
		
		if (itemFactory.sufficientSpace(player)) {
			//GIVE PLAYER TRICKSTER DAGGER
			player.getInventory().addItem(itemFactory.createDagger(player));	
			//EITHER EQUIP ITEMS IF POSSIBLE OR PUT IT INVENTORY
//			if (player.getInventory().getHelmet() == null) {
//				player.getInventory().setHelmet(itemFactory.createArmor("Helmet"));
//			} else {
//				player.getInventory().addItem(itemFactory.createArmor("Helmet"));
//			}
//			if (player.getInventory().getChestplate() == null) {
//				player.getInventory().setChestplate(itemFactory.createArmor("Chestplate"));
//			} else {
//				player.getInventory().addItem(itemFactory.createArmor("Chestplate"));
//			}
//			if (player.getInventory().getLeggings() == null) {
//				player.getInventory().setLeggings(itemFactory.createArmor("Leggings"));
//			} else {
//				player.getInventory().addItem(itemFactory.createArmor("Leggings"));
//			}
//			if (player.getInventory().getBoots() == null) {
//				player.getInventory().setBoots(itemFactory.createArmor("Boots"));
//			} else {
//				player.getInventory().addItem(itemFactory.createArmor("Boots"));
//			}
		} else {
			player.sendMessage(ChatColor.RED + "You do not have sufficient bag space to enable this kit!");
		}				
	}
	
	public void disableKit(Player player) throws SQLException {
		TricksterItemFactory itemFactory = new TricksterItemFactory(plugin);
		TricksterRepository database = new TricksterRepository(plugin);		
		database.toggleKitEnabled(player);
		
		List<ItemStack> kitItems = itemFactory.getAllItems(player);
		for (int i = 0; i < kitItems.size(); i++) {
			player.getInventory().remove(kitItems.get(i));
		}
		
//		if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().equals(itemFactory.createArmor("Helmet"))) {
//			player.getInventory().setHelmet(null);
//		}
//		if (player.getInventory().getChestplate() != null && player.getInventory().getChestplate().equals(itemFactory.createArmor("Chestplate"))) {
//			player.getInventory().setChestplate(null);
//		}
//		if (player.getInventory().getLeggings() != null && player.getInventory().getLeggings().equals(itemFactory.createArmor("Leggings"))) {
//			player.getInventory().setLeggings(null);
//		}
//		if (player.getInventory().getBoots() != null && player.getInventory().getBoots().equals(itemFactory.createArmor("Boots"))) {
//			player.getInventory().setBoots(null);
//		}
	}
	
	
	
	
}
