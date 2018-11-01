package nl.knokko.bosses.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
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
	
	private String defeatBossMessage;
	private String spawnBossMessage;
	private String almostSpawnBossMessage;
	
	@Override
	public void onEnable() {
		instance = this;
		load();
		getCommand("custombosses").setExecutor(new CommandBosses());
		Bukkit.getPluginManager().registerEvents(new BossesEventHandler(), this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for (CustomBoss boss : bosses)
				boss.update();
		}, 40, 20);
	}
	
	@Override
	public void onDisable() {
		save();
		instance = null;
	}
	
	public void save() {
		FileConfiguration config = getConfig();
		ConfigurationSection bossesSection = config.createSection("bosses");
		for (CustomBoss boss : bosses)
			boss.save(bossesSection.createSection(boss.getName()));
		config.set("defeat-boss", defeatBossMessage);
		config.set("just-spawned-boss", spawnBossMessage);
		config.set("time-spawn-message", almostSpawnBossMessage);
		saveConfig();
	}
	
	public void reload() {
		reloadConfig();
		load();
	}
	
	private void load() {
		FileConfiguration config = getConfig();
		ConfigurationSection bossesSection = config.getConfigurationSection("bosses");
		if (bossesSection != null) {
			Set<String> keys = bossesSection.getKeys(false);
			bosses = new ArrayList<CustomBoss>(keys.size());
			for (String key : keys) {
				try {
					bosses.add(new CustomBoss(bossesSection.getConfigurationSection(key)));
				} catch (Exception ex) {
					Bukkit.getLogger().log(Level.SEVERE, "Failed to load boss " + key, ex);
				}
			}
		} else
			bosses = new ArrayList<CustomBoss>(0);
		defeatBossMessage = config.getString("defeat-boss", "The boss %boss% has been defeated by %player% !");
		spawnBossMessage = config.getString("just-spawned-boss", "The boss %boss% has spawned !");
		almostSpawnBossMessage = config.getString("time-spawn-message", "Boss %boss% will spawn in %time% seconds ! ");
		Bukkit.getLogger().info("Loaded " + bosses.size() + " custom bosses");
	}
	
	public Collection<CustomBoss> getBosses(){
		return bosses;
	}
	
	public String getDefeatBossMessage() {
		return defeatBossMessage;
	}
	
	public String getSpawnBossMessage() {
		return spawnBossMessage;
	}
	
	public String getAlmostSpawnBossMessage() {
		return almostSpawnBossMessage;
	}
}