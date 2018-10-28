package nl.knokko.bosses.plugin;

import java.util.Collection;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
				break;
			}
		}
	}
}