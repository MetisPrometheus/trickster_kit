package com.ivarkronos.trickster.abilities;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.ivarkronos.trickster.MainPlugin;
import com.ivarkronos.trickster.database.TricksterRepository;

public class ShadowstepListener implements Listener {

	private MainPlugin plugin;
	public ShadowstepListener(MainPlugin mainPlugin) {
		this.plugin = mainPlugin;
	}

	@EventHandler
	public void shadowstepToggle(PlayerInteractEvent e) throws SQLException {
		Player player = e.getPlayer();
		Trickster trickster = new Trickster(player, plugin);
		if (!player.isSneaking() && trickster.daggerEquipped() && (e.getAction() == Action.RIGHT_CLICK_AIR)) { 
			TricksterRepository database = new TricksterRepository(plugin);
			database.toggleShadowStep(player);
			boolean isTargetPlayer = database.isShadowPlayer(player);
			player.sendMessage(ChatColor.GREEN + "Shadowstep Target: " + ChatColor.GOLD + (isTargetPlayer ? "Mob/Player" : "Blocks"));
		}
	}
	
	
	@EventHandler
	public void onDaggerRightClick(PlayerInteractEvent e) throws InterruptedException, SQLException {
		// Function: Teleport the player behind and target in range
		Player player = e.getPlayer();
		Trickster trickster = new Trickster(player, plugin);
		if (trickster.daggerEquipped() && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && player.isSneaking()) {
			// Exit the function if shadowstep is on cooldown
			if (plugin.shadowstepCooldown.containsKey(player) && System.currentTimeMillis() < plugin.shadowstepCooldown.get(player)) {
				long longRemaining = plugin.shadowstepCooldown.get(player) - System.currentTimeMillis();
				int intRemaining = (int) longRemaining/1000;
				player.sendMessage(ChatColor.GREEN + "Shadowstep on cooldown. You must wait " + 
									++intRemaining + " seconds!");
//				player.sendMessage(ChatColor.RED + "DEBUG: STILL ON COOLDOWN --> RETURN");
			} else {
				// Draw a raytrace from the player to where it is looking and return the first entity, if hit
				World world = player.getWorld();
				Location eyeLocation = player.getEyeLocation();
				Vector eyeDirection = eyeLocation.getDirection();
				
				TricksterRepository database = new TricksterRepository(plugin);
				boolean isTargetPlayer = database.isShadowPlayer(player);
				// Teleport to an entity or block depending on what mode is enabled
				if (isTargetPlayer) {
					RayTraceResult raytraceHit = world.rayTraceEntities(eyeLocation, eyeDirection, 50, 2, (x) -> !x.getName().contains(player.getName()));
					// Make sure shadowstep does not go on cooldown if one misses and hits no target
					if (raytraceHit == null || !(raytraceHit.getHitEntity() instanceof Creature || raytraceHit.getHitEntity() instanceof Player)) {
//						player.sendMessage(ChatColor.RED + "DEBUG: RAYTRACE DID NOT HIT A MOB OR A PLAYER --> RETURN");
						return;
					}
					Entity victim = raytraceHit.getHitEntity();
					trickster.teleportPlayerToEntity(victim);
					plugin.shadowstepCooldown.put(player, System.currentTimeMillis() + (10*1000)); // CHANGE SHADOWSTEP COOLDOWN
				} else {
					RayTraceResult raytraceBlock = world.rayTraceBlocks(eyeLocation, eyeDirection, 50);
					// Teleport player to block location and make the trickster hear a teleport sound-effect
					if (raytraceBlock != null) {
						Location newLoc = raytraceBlock.getHitBlock().getLocation().add(new Vector(0.5, 1, 0.5));
						newLoc.setDirection(player.getLocation().getDirection());
						Block newLocBlock = player.getWorld().getBlockAt(newLoc);
						
						int tpHeight = 0;
						while (newLocBlock.getType() != Material.AIR) {
							newLoc.add(new Vector(0,1,0));
							newLocBlock = player.getWorld().getBlockAt(newLoc);
//							player.sendMessage(newLocBlock.getType()+""); // DEBUG
							tpHeight++;
							if (tpHeight >= 3) {
								player.sendMessage(ChatColor.RED + "You can't teleport into a wall");
								return;
							} 
						}
						
						// Make nearby players see a cloud from where a trickster has teleported from
						for (Player p : Bukkit.getOnlinePlayers()) {
							if (player.getLocation().distance(p.getLocation()) <= 50) {
								p.spawnParticle(Particle.CLOUD, player.getLocation().getX(), player.getLocation().getY()+1, player.getLocation().getZ(), 50, 0f,0.75f,0f, 0.02f);
							}
						}
						
						player.teleport(newLoc);
						player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.33f, 1f);
						plugin.shadowstepCooldown.put(player, System.currentTimeMillis() + (10*1000)); // CHANGE SHADOWSTEP COOLDOWN
					}
				}
			}
		}
	}
	
	
	
	
	
}
