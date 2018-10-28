package nl.knokko.bosses.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import nl.knokko.bosses.plugin.boss.CustomBoss;
import nl.knokko.bosses.plugin.command.CommandBosses;

public class BossesPlugin extends JavaPlugin {
	
	private static BossesPlugin instance;
	
	public static BossesPlugin getInstance() {
		return instance;
	}
	
	private Collection<CustomBoss> bosses;
	
	@Override
	public void onEnable() {
		instance = this;
		load();
		getCommand("custombosses").setExecutor(new CommandBosses());
		Bukkit.getPluginManager().registerEvents(new BossesEventHandler(), this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for (CustomBoss boss : bosses)
				boss.update();
		}, 0, 100);
	}
	
	@Override
	public void onDisable() {
		save();
		instance = null;
	}
	
	public void save() {
		FileConfiguration config = getConfig();
		for (CustomBoss boss : bosses)
			boss.save(config.createSection(boss.getName()));
		saveConfig();
	}
	
	public void reload() {
		reloadConfig();
		load();
	}
	
	private void load() {
		FileConfiguration config = getConfig();
		Set<String> keys = config.getKeys(false);
		bosses = new ArrayList<CustomBoss>(keys.size());
		for (String key : keys) {
			try {
				bosses.add(new CustomBoss(config.getConfigurationSection(key)));
			} catch (Exception ex) {
				Bukkit.getLogger().log(Level.SEVERE, "Failed to load boss " + key, ex);
			}
		}
		Bukkit.getLogger().info("Loaded " + bosses.size() + " custom bosses");
	}
	
	public Collection<CustomBoss> getBosses(){
		return bosses;
	}
}