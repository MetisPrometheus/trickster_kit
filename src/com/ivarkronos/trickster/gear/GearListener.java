package com.ivarkronos.trickster.gear;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.ivarkronos.trickster.MainPlugin;
import com.ivarkronos.trickster.database.TricksterRepository;

public class GearListener implements Listener {
	
	private MainPlugin plugin;
	public GearListener(MainPlugin mainPlugin) {
		this.plugin = mainPlugin;
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) throws SQLException {
		TricksterRepository database = new TricksterRepository(plugin);
		if (database.hasPlayedNoInitialize(e.getEntity())) {
			// Prevent kit items from being dropped upon death
			TricksterItemFactory itemFactory = new TricksterItemFactory(plugin);
			List<ItemStack> kitItems = itemFactory.getAllItems(e.getEntity());
			e.getDrops().removeAll(kitItems);
		}
	}
	
	@EventHandler
	public void onDeath(PlayerRespawnEvent e) throws SQLException {
		Player player = e.getPlayer();
		TricksterRepository database = new TricksterRepository(plugin);
		
		if (database.hasPlayedNoInitialize(player) && database.isKitEnabled(player)) {
			TricksterItemFactory itemFactory = new TricksterItemFactory(plugin);
			
			//GIVE PLAYER TRICKSTER DAGGER
			player.getInventory().addItem(itemFactory.createDagger(player));
			
			//EQUIP HELMET IF POSSIBLE
			if (player.getInventory().getHelmet() == null) {
				player.getInventory().setHelmet(itemFactory.createArmor("Helmet"));
			} else {
				player.getInventory().addItem(itemFactory.createArmor("Helmet"));
			}
			//EQUIP CHESTPLATE IF POSSIBLE
			if (player.getInventory().getChestplate() == null) {
				player.getInventory().setChestplate(itemFactory.createArmor("Chestplate"));
			} else {
				player.getInventory().addItem(itemFactory.createArmor("Chestplate"));
			}
			//EQUIP LEGGINGS IF POSSIBLE
			if (player.getInventory().getLeggings() == null) {
				player.getInventory().setLeggings(itemFactory.createArmor("Leggings"));
			} else {
				player.getInventory().addItem(itemFactory.createArmor("Leggings"));
			}
			//EQUIP BOOTS IF POSSIBLE
			if (player.getInventory().getBoots() == null) {
				player.getInventory().setBoots(itemFactory.createArmor("Boots"));
			} else {
				player.getInventory().addItem(itemFactory.createArmor("Boots"));
			}
		}
	}
	
	@EventHandler
	public void onDrop (PlayerDropItemEvent e) throws SQLException {
		TricksterRepository database = new TricksterRepository(plugin);
		if (database.hasPlayedNoInitialize(e.getPlayer())) {
			// Cancel inventory dropping of kit items
			TricksterItemFactory itemFactory = new TricksterItemFactory(plugin);
			List<ItemStack> kitItems = itemFactory.getAllItems(e.getPlayer());
			for (ItemStack item : kitItems) {
				if (e.getItemDrop().getItemStack().equals(item)) {
					e.setCancelled(true);
				}
			}
		}
	}
	
}
