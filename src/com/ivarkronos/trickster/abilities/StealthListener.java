package com.ivarkronos.trickster.abilities;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.ivarkronos.trickster.MainPlugin;

public class StealthListener implements Listener {

	private MainPlugin plugin;
	public StealthListener(MainPlugin mainPlugin) {
		this.plugin = mainPlugin;
	}
	
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent e) {
		Player player = e.getPlayer();
		Trickster trickster = new Trickster(player, plugin);
		if (player.isSneaking()) { 
			// Cancel stealth attempt if releasing sneak while trying to enter stealth
//			cancelStealthAttempt(player, ChatColor.RED + "DEBUG: Stealth interrupted due to cancelling crouch");
			cancelStealthAttempt(player);
		} else {
			// Can't enter stealth unless dagger is equipped and you're not already invisible
			if (trickster.daggerEquipped() && !player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
				// Executed if ability still on cooldown
				if (plugin.stealthCooldown.containsKey(player) && System.currentTimeMillis() < plugin.stealthCooldown.get(player)) {
					long longRemaining = plugin.stealthCooldown.get(player) - System.currentTimeMillis();
					int intRemaining = (int) longRemaining/1000;
					player.sendMessage(ChatColor.GREEN + "Stealth on cooldown. You must wait " + 
										++intRemaining + " seconds!");
				} else {
					plugin.stealthCooldown.put(player, System.currentTimeMillis());
					// Store stealth task so it's able to be cancelled due cancelling crouch, taking damage, etc.
					BukkitRunnable enterStealth = trickster.stealthTask();
					enterStealth.runTaskLater(plugin, 60L); // 5sec
					plugin.stealthTask.put(player, enterStealth); 
				}
			}
		}
	}
	
	public void cancelStealthAttempt(Player player, String message) {
		if (plugin.stealthTask.containsKey(player)) {
			plugin.stealthTask.get(player).cancel();
			plugin.stealthTask.remove(player);
			player.sendMessage(message);
		}
	}
	
	public void cancelStealthAttempt(Player player) {
		if (plugin.stealthTask.containsKey(player)) {
			plugin.stealthTask.get(player).cancel();
			plugin.stealthTask.remove(player);
		}
	}
	
	@EventHandler
	public void itemChange(PlayerItemHeldEvent e) {
		cancelStealthAttempt(e.getPlayer(), ChatColor.RED + "Stealth interrupted due to changing held item");
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		cancelStealthAttempt(e.getEntity(), ChatColor.RED + "Stealth interrupted due to dying you idiot");
	}
	
	public void returnArmor(Player player) {
		// Return whatever armor has been taken away while the player was invisible
		if (plugin.stealthArmor.containsKey(player)) {
			player.getInventory().setArmorContents(plugin.stealthArmor.get(player));
			plugin.stealthArmor.remove(player);
		}
	}
	
	@EventHandler
	public void onPotion(EntityPotionEffectEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			if (plugin.stealthCooldown.containsKey(player) && e.getModifiedType().equals(PotionEffectType.INVISIBILITY)) {
				if (e.getAction() == EntityPotionEffectEvent.Action.CLEARED || e.getCause() == EntityPotionEffectEvent.Cause.EXPIRATION) {
					player.sendMessage(ChatColor.RED + "Invisibility has worn off and you're now visible again!");
					plugin.stealthCooldown.put(player, System.currentTimeMillis() + (20*1000));
					returnArmor(player);
				}
			}
		}
	}
	
	@EventHandler
	public void onStealthDamage(EntityDamageByEntityEvent e) {
		// If the victim is invisible when attacked, make him visible and re-equip his armor
		if (e.getEntity() instanceof Player) {
			Player victim = (Player) e.getEntity();
			cancelStealthAttempt(victim, ChatColor.RED + "Stealth interrupted due to being damaged");
			if (victim.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
				victim.removePotionEffect(PotionEffectType.INVISIBILITY);
				victim.removePotionEffect(PotionEffectType.SPEED);
				victim.removePotionEffect(PotionEffectType.REGENERATION);
				e.setDamage(4); // Invisible targets have no armor on. Set damage taken to 2 hearts if hit by an entity
				victim.sendMessage(ChatColor.RED + "You've been damaged and you're now visible again!");
				plugin.stealthCooldown.put(victim, System.currentTimeMillis() + (20*1000));
				returnArmor(victim);
			} 
		}
		// If the attacker is invisible when attacking, make him visible and re-equip his armor
		if (e.getDamager() instanceof Player) {
			Player attacker = (Player) e.getDamager();
			cancelStealthAttempt(attacker, ChatColor.RED + "Stealth interrupted due to attacking something");
			if (attacker.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
				attacker.removePotionEffect(PotionEffectType.INVISIBILITY);
				attacker.removePotionEffect(PotionEffectType.SPEED);
				attacker.removePotionEffect(PotionEffectType.REGENERATION);
				attacker.sendMessage(ChatColor.RED + "You've entered combat and you're now visible again!");
				plugin.stealthCooldown.put(attacker, System.currentTimeMillis() + (20*1000));
				returnArmor(attacker);
			}
		}
	}
	
	@EventHandler
	public void onShoot(EntityShootBowEvent e) {
		if (e.getEntity() instanceof Player && plugin.stealthCooldown.containsKey(e.getEntity())) {
			Player player = (Player) e.getEntity();
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
			player.removePotionEffect(PotionEffectType.SPEED);
			player.removePotionEffect(PotionEffectType.REGENERATION);
			player.sendMessage(ChatColor.RED + "You've entered combat and is now visible again!");
			plugin.stealthCooldown.put(player, System.currentTimeMillis() + (20*1000));
			returnArmor(player);
		}
	}
	
		
}
	
	
	

