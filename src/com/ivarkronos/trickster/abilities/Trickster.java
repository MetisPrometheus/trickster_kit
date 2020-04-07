package com.ivarkronos.trickster.abilities;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.ivarkronos.trickster.MainPlugin;

public class Trickster {

	private MainPlugin plugin;
	private Player player;
	
	public Trickster(Player player, MainPlugin mainPlugin) {
		this.player = player;
		this.plugin = mainPlugin;
	}
	
	public void resetCooldowns() {
		if (plugin.stealthCooldown.containsKey(player)) {
			plugin.stealthCooldown.put(player, System.currentTimeMillis());
		} 
		if (plugin.shadowstepCooldown.containsKey(player)) {
			plugin.shadowstepCooldown.put(player, System.currentTimeMillis());
		} 
		if (plugin.decoyCooldown.containsKey(player)) {
			plugin.decoyCooldown.put(player, System.currentTimeMillis());
		}
	}
	
	public boolean daggerEquipped() {
		ItemStack weapon = player.getInventory().getItemInMainHand();
		if (weapon.hasItemMeta()) {
			if (weapon.getItemMeta().hasDisplayName()) {
				if (weapon.getItemMeta().getDisplayName().equals(ChatColor.GOLD+"A Trickster's Dagger")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void teleportPlayerToEntity(Entity victim) {
		// New attacker location based on old attacker location and victim location
		Location vicLoc = victim.getLocation();
		Location atkLoc = player.getLocation();
//		Location tpLoc = vicLoc.add(vicLoc.getDirection().multiply(-2));
		Location tpLoc = vicLoc.add(atkLoc.getDirection().multiply(2));
		tpLoc.setYaw(atkLoc.getYaw()+180);
		
		// Adjust head angle based on relatively how high or low the enemy is in comparison
		if (atkLoc.getPitch() < 0) {
			tpLoc.setPitch(Math.abs(atkLoc.getPitch()));
		} else if (atkLoc.getPitch() < 15) {
			tpLoc.setPitch(15);
		} else {
			tpLoc.setPitch(atkLoc.getPitch());
		}
		
		// If victim stands in front of a wall, check how high the wall is
		int tpHeight = 0;
		Block newLocBlock = player.getWorld().getBlockAt(tpLoc);
		while (newLocBlock.getType() != Material.AIR) {
			tpLoc.add(new Vector(0,1,0));
			newLocBlock = player.getWorld().getBlockAt(tpLoc);
			player.sendMessage(newLocBlock.getType()+"");
			tpHeight++;
		}
		
		// If the wall behind the victim is over 5 blocks tall. The attacker can't teleport
		if (tpHeight <= 5) {
			if (victim instanceof Mob) {
				Creature mob = (Creature) victim;
				mob.setTarget(null);
				player.teleport(tpLoc);
				mob.setTarget(null);
			} else if (victim instanceof Player) {
				Player playerVictim = (Player) victim;
				playerVictim.playSound(playerVictim.getLocation(), Sound.ENTITY_ENDERMAN_AMBIENT, 0.5f, 3f);
				player.teleport(tpLoc);
			} else {
				player.teleport(tpLoc);
			}
			player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.33f, 1f);
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (player.getLocation().distance(p.getLocation()) <= 50) {
					p.spawnParticle(Particle.CLOUD, atkLoc.getX(), atkLoc.getY()+1, atkLoc.getZ(), 50, 0f,0.75f,0f, 0.02f);
				}
			}
		} else {
			player.sendMessage("Can't teleport. The enemy is backed up against a wall");
		}
	}
	
	public void enterStealth() {
		// Make player invisible 5 sec after sneaking with the dagger. The runnable may be interrupted by various causes.
		if (player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("Dagger") && !(player.isSneaking())) {
			if (plugin.stealthCooldown.containsKey(player) && System.currentTimeMillis() < plugin.stealthCooldown.get(player)) {
				long longRemaining = plugin.stealthCooldown.get(player) - System.currentTimeMillis();
				int intRemaining = (int) longRemaining/1000;
				player.sendMessage(ChatColor.GREEN + "Stealth on cooldown. You must wait " + 
									++intRemaining + " seconds!");
			} else {
				BukkitRunnable task = stealthTask();
				task.runTaskLater(plugin, 300L); // 5sec
				plugin.stealthTask.put(player, task);
			}
		}
	}
	
	public BukkitRunnable stealthTask() {
		BukkitRunnable stealth = (BukkitRunnable) new BukkitRunnable() {
			@Override
			public void run() {
				// Make player invisible
				player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 300, 1));
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 1));
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 300, 1));
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 1f, 1f);
				player.sendMessage(ChatColor.GREEN + "You're invisible!");
				// Temporarily store and remove player armor whilst stealth
				ItemStack[] fullArmor = player.getInventory().getArmorContents();  
				plugin.stealthArmor.put(player, fullArmor);
				player.getInventory().setArmorContents(null);
				// Clear nearby mob aggression
				new Trickster(player, plugin).clearAggro();
				// Play a sound and leave a smokebomb for the nearby players
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (player.getLocation().distance(p.getLocation()) <= 30) {
						p.spawnParticle(Particle.CLOUD, player.getLocation().getX(), player.getLocation().getY()+1, player.getLocation().getZ(), 50, 0,0,0, 0.1f);
						p.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 0.75f, 1f);
					}
				}
				// Remove unnecessary hashmap-storing
				if (plugin.stealthTask.containsKey(player)) {
					plugin.stealthTask.remove(player);
				}
			}
		};
		return stealth;
	}
	
	public void clearAggro() {
		new BukkitRunnable() {
			@Override
			public void run() {
				Collection<Entity> nearbyEntities = Bukkit.getServer().getWorld("world").getNearbyEntities(player.getLocation(), 20, 20, 20);
				for (Entity enemy : nearbyEntities) {
					if (enemy instanceof Monster) {
						Monster monster = (Monster) enemy;
						monster.setTarget(null);
					}
				}
			}
		}.runTaskLater(plugin, 6L);
	}
	
	
}
