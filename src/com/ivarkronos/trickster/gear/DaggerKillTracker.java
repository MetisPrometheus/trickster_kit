package com.ivarkronos.trickster.gear;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ivarkronos.trickster.MainPlugin;
import com.ivarkronos.trickster.database.TricksterRepository;

public class DaggerKillTracker implements Listener {

	private MainPlugin main;
	
	public DaggerKillTracker(MainPlugin mainPlugin) {
		this.main = mainPlugin;
	}

	@EventHandler
	public void onKill(EntityDeathEvent e) throws SQLException {
		TricksterRepository database = new TricksterRepository(main);
		if (e.getEntity() instanceof Player && database.hasPlayedNoInitialize((Player)e.getEntity())) {
			new TricksterRepository(main).resetRecentKills((Player) e.getEntity()); // If victim a trickster, reset his "recently killed" stats
			e.getEntity().sendMessage(e.getEntity().getKiller()+"killeryo");
		}
		if (!(e.getEntity().getKiller() instanceof Player)
		|| e.getEntity().getKiller().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "A Trickster's Dagger") == false
		|| e.getEntity().getName() == e.getEntity().getKiller().getName()) {
			return;
		}
		
		Player killer = e.getEntity().getKiller();
		if (database.hasPlayedNoInitialize(killer)) {
			ItemStack dagger = killer.getInventory().getItemInMainHand();
			ItemMeta daggerMeta = dagger.getItemMeta();
			List<String> daggerLore = daggerMeta.getLore();
			
			int totalPKills = database.getTotalPlayerKills(killer);
			int totalMKills = database.getTotalMobKills(killer);
			int recentPKills = database.getRecentPlayerKills(killer);
			int recentMKills = database.getRecentMobKills(killer);
			
			if (e.getEntity() instanceof Player) {
				database.updatePlayerKills(killer, ++totalPKills, ++recentPKills);
				daggerLore.set(2, ChatColor.RED+"Players: "+ totalPKills + ChatColor.WHITE+ " | " + ChatColor.AQUA+"Mobs: "+ totalMKills);
				daggerLore.set(5, ChatColor.RED+"Players: "+ recentPKills + ChatColor.WHITE+ " | " + ChatColor.AQUA+"Mobs: "+ recentMKills);
			} else if (e.getEntity() instanceof Creature) {
				database.updateMobKills(killer, ++totalMKills, ++recentMKills);
				daggerLore.set(2, ChatColor.RED+"Players: "+ totalPKills + ChatColor.WHITE+ " | " + ChatColor.AQUA+"Mobs: "+ totalMKills);
				daggerLore.set(5, ChatColor.RED+"Players: "+ recentPKills + ChatColor.WHITE+ " | " + ChatColor.AQUA+"Mobs: "+ recentMKills);
			} 

			daggerMeta.setLore(daggerLore);
			dagger.setItemMeta(daggerMeta);
			killer.getInventory().setItemInMainHand(dagger);
		}
	}

	
}
