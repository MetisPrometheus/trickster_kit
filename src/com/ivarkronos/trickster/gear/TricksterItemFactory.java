package com.ivarkronos.trickster.gear;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ivarkronos.trickster.MainPlugin;
import com.ivarkronos.trickster.database.TricksterRepository;

public class TricksterItemFactory {

	private MainPlugin main;
	public TricksterItemFactory(MainPlugin mainPlugin) {
		this.main = mainPlugin;
	}

	public boolean sufficientSpace(Player player) {
		ItemStack[] inv = player.getInventory().getContents();
		int freeSpace = 0;
		for (ItemStack item : inv) {
			if (item == null) {
				freeSpace++;
			}
		}
		if (freeSpace >= getAllItems(player).size()) {
			return true;
		}
		return false;
	}
	
	public List<ItemStack> getAllItems(Player player) {
		List<ItemStack> items = new ArrayList<>();
//		items.add(this.createArmor("Helmet"));
//		items.add(this.createArmor("Chestplate"));
//		items.add(this.createArmor("Leggings"));
//		items.add(this.createArmor("Boots"));
		items.add(createDagger(player));
		return items;
	}
	
	public ItemStack createArmor(String slotType) {
		ItemStack item = null;
		switch(slotType) {
		case "Helmet":
			item = new ItemStack(Material.DIAMOND_HELMET);
			break;
		case "Chestplate":
			item = new ItemStack(Material.DIAMOND_CHESTPLATE);
			break;
		case "Leggings":
			item = new ItemStack(Material.DIAMOND_LEGGINGS);
			break;
		case "Boots":
			item = new ItemStack(Material.DIAMOND_BOOTS);
			item.addEnchantment(Enchantment.PROTECTION_FALL, 4);
			break;
		}
		item.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
		ItemMeta meta = item.getItemMeta();
//		meta.setDisplayName("Trickster Helmet");
		meta.setUnbreakable(true);
		item.setItemMeta(meta);
		return item;
	}
	
	public ItemStack createDagger(Player player) {
		try {
			TricksterRepository repo = new TricksterRepository(main);
			repo.hasPlayed(player); // Also initializes player in database if first time playing
			int totalP = repo.getTotalPlayerKills(player);
			int totalM = repo.getTotalMobKills(player);
			int recentP = repo.getRecentPlayerKills(player);
			int recentM = repo.getRecentMobKills(player);
			
			ItemStack dagger = new ItemStack(Material.GOLDEN_AXE);
			ItemMeta daggerMeta = dagger.getItemMeta();
			daggerMeta.setDisplayName(ChatColor.GOLD+"A Trickster's Dagger");
			daggerMeta.setUnbreakable(true);
			List<String> daggerLore = new ArrayList<>(); // Can get this text from a file, is it even necessary?, idk where in the server structure it would be stored tho, yml?
			
			daggerLore.add(" ");
			daggerLore.add(ChatColor.WHITE + "Total Kills:");
			daggerLore.add(ChatColor.RED+"Players: "+ totalP + ChatColor.WHITE+ " | " + ChatColor.AQUA + "Mobs: "+ totalM);
			daggerLore.add(" ");
			daggerLore.add(ChatColor.WHITE + "Kills Since Last Death:");
			daggerLore.add(ChatColor.RED+"Players: "+ recentP + ChatColor.WHITE+ " | " + ChatColor.AQUA + "Mobs: "+ recentM);
			daggerLore.add(" ");
			daggerLore.add(ChatColor.GOLD + "-------------------------");
			daggerLore.add(ChatColor.GOLD + "Active Abilities:");
			daggerLore.add(ChatColor.GOLD + "-------------------------");
			daggerLore.add(ChatColor.GREEN + "Stealth:");
			daggerLore.add(ChatColor.AQUA + "" + ChatColor.ITALIC + "Sneak for 3 seconds");
			daggerLore.add(ChatColor.AQUA + "" + ChatColor.ITALIC + "to enter stealth.");
			daggerLore.add(ChatColor.WHITE + "" + ChatColor.ITALIC + "(20 seconds)");
			daggerLore.add(" ");
			daggerLore.add(ChatColor.GREEN + "Shadowstep: (Sneak + RMB)");
			daggerLore.add(ChatColor.AQUA + "" + ChatColor.ITALIC + "Teleport behind an enemy");
			daggerLore.add(ChatColor.AQUA + "" + ChatColor.ITALIC + "or to a specific location");
			daggerLore.add(ChatColor.AQUA + "" + ChatColor.ITALIC + "Toggle button: " + ChatColor.GOLD + ChatColor.ITALIC + "RMB");
			daggerLore.add(ChatColor.WHITE + "" + ChatColor.ITALIC + "(10 seconds)");
			daggerLore.add(" ");
			daggerLore.add(ChatColor.GREEN + "Decoy: (Sneak + LMB)");
			daggerLore.add(ChatColor.AQUA + "" + ChatColor.ITALIC + "Teleport behind an enemy");
			daggerLore.add(ChatColor.AQUA + "" + ChatColor.ITALIC + "and leave an explosive clone");
			daggerLore.add(ChatColor.AQUA + "" + ChatColor.ITALIC + "at the location you left.");
			daggerLore.add(ChatColor.WHITE + "" + ChatColor.ITALIC + "(20 seconds)");
			daggerLore.add(" ");
			daggerLore.add(ChatColor.GREEN + "Corona Stab:");
			daggerLore.add(ChatColor.AQUA + "" + ChatColor.ITALIC + "Deal double damange when");
			daggerLore.add(ChatColor.AQUA + "" + ChatColor.ITALIC + "stabbing someone in the back.");
			daggerLore.add(ChatColor.WHITE + "" + ChatColor.ITALIC + "(Passive)");
			daggerLore.add(" ");
			daggerMeta.setLore(daggerLore);
			dagger.setItemMeta(daggerMeta);
			return dagger;
		} catch (SQLException e) {
			player.sendMessage("[ERROR:TRICKSTER:CREATEDAGGER] An error has occured! Please contact an admin and provide the error message.");
			e.printStackTrace();
			return null;
		}
	}

	

}
