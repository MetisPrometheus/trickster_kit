package com.ivarkronos.trickster;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.ivarkronos.trickster.abilities.BackstabListener;
import com.ivarkronos.trickster.abilities.DecoyListener;
import com.ivarkronos.trickster.abilities.ShadowstepListener;
import com.ivarkronos.trickster.abilities.StealthListener;
import com.ivarkronos.trickster.database.DBController;
import com.ivarkronos.trickster.database.TricksterRepository;
import com.ivarkronos.trickster.gear.DaggerKillTracker;
import com.ivarkronos.trickster.gear.GearListener;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensPlugin;

public class MainPlugin extends JavaPlugin {
	
	private final APICommands api = new APICommands(this);
	public final APICommands getAPI() { return api; }
	
	private final DBController dbc = new DBController();
	public final DBController getDbc() { return dbc; }
	
	public HashMap<Player, Long> stealthCooldown = new HashMap<>();
	public HashMap<Player, BukkitRunnable> stealthTask = new HashMap<>();
	public HashMap<Player, ItemStack[]> stealthArmor = new HashMap<>();
	
	public HashMap<Player, Long> decoyCooldown = new HashMap<>();
	public HashMap<UUID, BukkitRunnable> decoyExplode = new HashMap<>();
	public HashMap<UUID, BukkitTask> decoyDistraction = new HashMap<>();
	
	public HashMap<Player, Long> shadowstepCooldown = new HashMap<>();
	public HashMap<Player, Boolean> shadowstepToggle = new HashMap<>();
	
	@Override
	public void onEnable() {
		System.out.println(":::::::::::::::::::: Trickster Kit Enabled! ::::::::::::::::::::");
		final PluginManager manager = this.getServer().getPluginManager();
		this.getConfig().options().copyDefaults();
		
		// KIT MENU
//		getCommand("tkit").setExecutor(new KitMenuCommand(this));
		manager.registerEvents(new KitMenuListener(this), this);
		
		// ABILITIES
		manager.registerEvents(new StealthListener(this), this);
		manager.registerEvents(new ShadowstepListener(this), this);
		manager.registerEvents(new DecoyListener(this), this);
		manager.registerEvents(new BackstabListener(this), this);
		
		// KIT ITEMS
		manager.registerEvents(new DaggerKillTracker(this), this);
		manager.registerEvents(new GearListener(this), this);
		
	}
	
	public Citizens getCitizensAPI() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Citizens");
		if (plugin instanceof CitizensPlugin) {
			return (Citizens) plugin;
		}
		return null;
	}
	
	
	public void enableTrickster(Player player) throws SQLException {
		api.enableKit(player);
	}
	
	public void disableTrickster(Player player) throws SQLException {
		api.disableKit(player);
	}
	
	public void deleteTrickster(Player player) throws SQLException {
		TricksterRepository repository = new TricksterRepository(this);
		repository.deleteTricksterStats(player);
	}
	
}
