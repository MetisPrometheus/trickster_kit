package com.ivarkronos.trickster;

import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.ivarkronos.trickster.gear.TricksterItemFactory;

public class KitMenuListener implements Listener {

	private MainPlugin main;
	public KitMenuListener(MainPlugin main) {
		this.main = main;
	}
	
	@EventHandler
	public void onClick (InventoryClickEvent e) throws SQLException {
		Player player = (Player) e.getWhoClicked();
		
		if (ChatColor.translateAlternateColorCodes('&', e.getView().getTitle()).equals(ChatColor.DARK_AQUA + "Player Kits!")) {
			if (e.getCurrentItem() != null) {
				e.setCancelled(true);
				
				TricksterItemFactory itemFactory = new TricksterItemFactory(main);
				// Green block -> Enabled --> 
				if (e.getCurrentItem().getType().equals(Material.EMERALD_BLOCK)) {
					if (itemFactory.sufficientSpace(player)) {
						main.getAPI().enableKit(player);
					}
				} else if (e.getCurrentItem().getType().equals(Material.REDSTONE_BLOCK)) {
					main.getAPI().disableKit(player);
				}
				player.closeInventory();
			}
		}
	}
	
	

}
