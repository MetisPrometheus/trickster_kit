package com.ivarkronos.trickster.abilities;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.ivarkronos.trickster.MainPlugin;

public class BackstabListener implements Listener {
	
	private MainPlugin plugin;
	public BackstabListener(MainPlugin mainPlugin) {
		this.plugin = mainPlugin;
	}
	
	@EventHandler
	public void onClick(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity)) { return; }			
		Player attacker = (Player) e.getDamager();
		LivingEntity victim = (LivingEntity) e.getEntity();
		Trickster trickster = new Trickster(attacker, plugin);
		
		if (trickster.daggerEquipped()) {
			double xAttacker = attacker.getLocation().getDirection().getX();
			double yAttacker = attacker.getLocation().getDirection().getZ(); // MC Z --> Y
			double xVictim = victim.getLocation().getDirection().getX();
			double yVictim = victim.getLocation().getDirection().getZ();
			
			Vector2D vec1 = new Vector2D(xAttacker, yAttacker);
			Vector2D vec2 = new Vector2D(xVictim, yVictim);
			long angle = vec1.vectorAngleWith(vec2);
//			attacker.sendMessage("Angle: "+angle); // FOR DEBUGGING
			
			if (angle <= 60) {
				victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1)); // 3sec poison
				double dmg = e.getDamage();
				e.setDamage(dmg*2.5);
//				attacker.sendMessage(ChatColor.LIGHT_PURPLE + "You crit an enemy!");
				attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1f);
				attacker.spawnParticle(Particle.CRIT_MAGIC, victim.getLocation().getX(), victim.getLocation().getY()+1, victim.getLocation().getZ(), 50, 0,0,0, 0.5f);
			}
		}
	}
	
}
