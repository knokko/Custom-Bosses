package nl.knokko.bosses.plugin;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import nl.knokko.bosses.plugin.boss.CustomBoss;

public class BossesEventHandler implements Listener {
	
	@EventHandler
	public void onDeath(EntityDeathEvent event) {
		Collection<CustomBoss> bosses = BossesPlugin.getInstance().getBosses();
		for (CustomBoss boss : bosses) {
			if (event.getEntity().getUniqueId().equals(boss.getCurrentID())) {
				boss.setDead();
				event.getDrops().clear();
				event.getDrops().addAll(boss.getDrops());
				event.setDroppedExp(boss.getDroppedXP());
				Player killer = event.getEntity().getKiller();
				String killerName;
				if (killer != null)
					killerName = killer.getName();
				else
					killerName = "nobody";
				Bukkit.broadcastMessage(BossesPlugin.getInstance().getDefeatBossMessage().replaceAll("%boss%", boss.getName()).replaceAll("%player%", killerName));
				break;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Damageable) {
			Damageable d = (Damageable) event.getEntity();
			Collection<CustomBoss> bosses = BossesPlugin.getInstance().getBosses();
			for (CustomBoss boss : bosses) {
				if (d.getUniqueId().equals(boss.getCurrentID())) {
					d.setCustomName(boss.getName() + " " + Math.round(100 * (d.getHealth() - event.getFinalDamage()) / d.getMaxHealth()) + "%");
					return;
				}
			}
		}
	}
}