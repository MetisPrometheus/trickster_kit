package com.ivarkronos.trickster.abilities;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.ivarkronos.trickster.MainPlugin;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;


public class DecoyListener implements Listener {

	private MainPlugin plugin;
	public DecoyListener(MainPlugin mainPlugin) {
		this.plugin = mainPlugin;
	}
	
	@EventHandler
	public void checkCooldown(PlayerInteractEvent e) {
		Trickster trickster = new Trickster(e.getPlayer(), plugin);
		if (trickster.daggerEquipped() && e.getPlayer().isSneaking() && (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR)) {
			if (plugin.decoyCooldown.containsKey(e.getPlayer()) && System.currentTimeMillis() < plugin.decoyCooldown.get(e.getPlayer())) {
				long longRemaining = plugin.decoyCooldown.get(e.getPlayer()) - System.currentTimeMillis();
				int intRemaining = (int) longRemaining/1000;
//				e.getPlayer().sendMessage(ChatColor.GREEN + "Decoy on cooldown. You must wait " + 
//									++intRemaining + " seconds!");
			}
		}
	}
	
	@EventHandler
	public void onClick(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player) {
			Player attacker = (Player) e.getDamager();
			Trickster trickster = new Trickster(attacker, plugin);
			if (attacker.isSneaking() && trickster.daggerEquipped()) {
				// Check if still on cooldown and inform the player
				if (plugin.decoyCooldown.containsKey(attacker) && System.currentTimeMillis() < plugin.decoyCooldown.get(attacker)) {
//					attacker.sendMessage("DEBUG: Decoy ability is on cooldown...");
					long longRemaining = plugin.decoyCooldown.get(attacker) - System.currentTimeMillis();
					int intRemaining = (int) longRemaining/1000;
					attacker.sendMessage(ChatColor.GREEN + "Decoy on cooldown. You must wait " + 
										++intRemaining + " seconds!");
					return;
				// Ability is off cooldown, perform the ability
				} else {
					NPC npc = spawnDecoy(attacker);
					
					// Creating a runnable that will trigger the decoy's explosion
					BukkitRunnable explodeRunnable = new BukkitRunnable() {
						@Override
						public void run() {
							Location npcLoc = npc.getStoredLocation();
							attacker.getWorld().createExplosion(npcLoc.getX(), npcLoc.getY(), npcLoc.getZ(), 5, false, false);
							npc.destroy();
							// Stop task from trying to distract mobs once decoy is destroyed
							plugin.decoyDistraction.get(npc.getUniqueId()).cancel();
							plugin.decoyDistraction.remove(npc.getUniqueId());
							// Clear target for nearby enemies
							Collection<Entity> nearbyEntities = attacker.getWorld().getNearbyEntities(npcLoc, 20, 20, 20);
							for (Entity enemy : nearbyEntities) {
								if (enemy instanceof Monster) {
									Monster monster = (Monster) enemy;
									monster.setTarget(null);
								}
							}
						}};
					
					// Set explosion timer of decoy depending on if used on player or mob
					if (e.getEntity() instanceof Player) {
//						npc.getNavigator().getDefaultParameters().baseSpeed(1.2f);
						Player var = (Player) e.getEntity();
						var.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 3));
						npc.getNavigator().setTarget(e.getEntity(), true);
						explodeRunnable.runTaskLater(plugin, 40L);
					} else {
						explodeRunnable.runTaskLater(plugin, 100L);
					}
					plugin.decoyExplode.put(npc.getUniqueId(), explodeRunnable);
					
					// Continuously make sure nearby enemies are targetting the decoy
					BukkitTask distractionTask = new BukkitRunnable() {
						@Override
						public void run() {
							Location npcLoc = npc.getStoredLocation();
							Collection<Entity> nearbyEntities = Bukkit.getServer().getWorld("world").getNearbyEntities(npcLoc, 20, 20, 20);
							for (Entity enemy : nearbyEntities) {
								if (enemy instanceof Monster) {
									Monster monster = (Monster) enemy;
									monster.setTarget((LivingEntity) npc.getEntity());
								}
							}
						}}.runTaskTimer(plugin, 10L, 40L);
					plugin.decoyDistraction.put(npc.getUniqueId(), distractionTask);
					
					plugin.decoyCooldown.put(attacker, System.currentTimeMillis() + (2*1000));
					trickster.teleportPlayerToEntity(e.getEntity());
					e.setCancelled(true);
				}
			}
		}
	}
	
	private NPC spawnDecoy(Player player) {
	// Create a decoy of the player wearing the same equipment
		NPC npc = plugin.getCitizensAPI().getNPCRegistry().createNPC(EntityType.PLAYER, player.getName());
		npc.getTrait(Equipment.class).set(EquipmentSlot.HAND, player.getInventory().getItemInMainHand());
		npc.getTrait(Equipment.class).set(EquipmentSlot.HELMET, player.getInventory().getHelmet());
		npc.getTrait(Equipment.class).set(EquipmentSlot.CHESTPLATE, player.getInventory().getChestplate());
		npc.getTrait(Equipment.class).set(EquipmentSlot.LEGGINGS, player.getInventory().getLeggings());
		npc.getTrait(Equipment.class).set(EquipmentSlot.BOOTS, player.getInventory().getBoots());
		npc.spawn(player.getLocation());
		return npc;
	}
	
	
	
	
}
