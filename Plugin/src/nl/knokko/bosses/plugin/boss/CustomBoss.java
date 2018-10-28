package nl.knokko.bosses.plugin.boss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class CustomBoss {
	
	private UUID currentID;
	private long timeOfDeath;
	
	private final String name;
	private final EntityType type;
	
	private final String spawnWorld;
	private final int spawnX;
	private final int spawnY;
	private final int spawnZ;
	private final int respawnTime;
	
	private final Collection<AttributeEntry> attributes;
	
	private final ItemStack weapon;
	private final ItemStack helmet;
	private final ItemStack chestplate;
	private final ItemStack leggings;
	private final ItemStack boots;
	
	public CustomBoss(String name, EntityType type, String world, int x, int y, int z, int respawnTime, 
			Collection<AttributeEntry> attributes, ItemStack weapon, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
		this.name = name;
		this.type = type;
		spawnWorld = world;
		spawnX = x;
		spawnY = y;
		spawnZ = z;
		this.respawnTime = respawnTime;
		this.attributes = attributes;
		this.weapon = weapon;
		this.helmet = helmet;
		this.chestplate = chestplate;
		this.leggings = leggings;
		this.boots = boots;
	}
	
	public CustomBoss(ConfigurationSection section) {
		name = section.getName();
		type = EntityType.valueOf(section.getString("type"));
		spawnWorld = section.getString("spawnWorld");
		spawnX = section.getInt("spawnX");
		spawnY = section.getInt("spawnY");
		spawnZ = section.getInt("spawnZ");
		respawnTime = section.getInt("respawnTime");
		ConfigurationSection attSection = section.getConfigurationSection("attributes");
		Set<String> attKeys = attSection.getKeys(false);
		attributes = new ArrayList<AttributeEntry>(attKeys.size());
		for (String attKey : attKeys)
			attributes.add(new AttributeEntry(Attribute.valueOf(attKey), attSection.getDouble(attKey)));
		weapon = section.getItemStack("weapon");
		helmet = section.getItemStack("helmet");
		chestplate = section.getItemStack("chestplate");
		leggings = section.getItemStack("leggings");
		boots = section.getItemStack("boots");
		if (section.contains("mostID"))
			currentID = new UUID(section.getLong("mostID"), section.getLong("leastID"));
		timeOfDeath = section.getLong("timeOfDeath");
	}
	
	public void save(ConfigurationSection section) {
		section.set("type", type.name());
		section.set("spawnWorld", spawnWorld);
		section.set("spawnX", spawnX);
		section.set("spawnY", spawnY);
		section.set("spawnZ", spawnZ);
		section.set("respawnTime", respawnTime);
		ConfigurationSection attSection = section.createSection("attributes");
		for (AttributeEntry attribute : attributes)
			attSection.set(attribute.getAttribute().name(), attribute.getValue());
		section.set("weapon", weapon);
		section.set("helmet", helmet);
		section.set("chestplate", chestplate);
		section.set("leggings", leggings);
		section.set("boots", boots);
		if (currentID != null) {
			section.set("mostID", currentID.getMostSignificantBits());
			section.set("leastID", currentID.getLeastSignificantBits());
		} else {
			section.set("mostID", null);
			section.set("leastID", null);
		}
		section.set("timeOfDeath", timeOfDeath);
	}
	
	public void spawn() {
		World world = Bukkit.getWorld(spawnWorld);
		if (world == null) {
			Bukkit.getLogger().warning("Can't load world " + spawnWorld + " where boss " + name + " is supposed to spawn.");
			return;
		}
		Entity entity = world.spawnEntity(new Location(world, spawnX, spawnY, spawnZ), type);
		entity.setCustomNameVisible(true);
		entity.setCustomName(name);
		double maxHealth = -1;
		currentID = entity.getUniqueId();
		if (entity instanceof Attributable) {
			Attributable a = (Attributable) entity;
			for (AttributeEntry attribute : attributes)
				a.getAttribute(attribute.getAttribute()).setBaseValue(attribute.getValue());
			maxHealth = a.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
		}
		if (entity instanceof LivingEntity) {
			LivingEntity le = (LivingEntity) entity;
			if (maxHealth != -1)
				le.setHealth(maxHealth);
			EntityEquipment eq = le.getEquipment();
			le.setRemoveWhenFarAway(false);
			// Even if they happen to be null, the entity should be forced not to have any equipment there
			eq.setItemInMainHand(weapon);
			eq.setHelmet(helmet);
			eq.setChestplate(chestplate);
			eq.setLeggings(leggings);
			eq.setBoots(boots);
			eq.setItemInMainHandDropChance(0);
			eq.setHelmetDropChance(0);
			eq.setChestplateDropChance(0);
			eq.setLeggingsDropChance(0);
			eq.setBootsDropChance(0);
		}
		timeOfDeath = 0;
	}
	
	public UUID getCurrentID() {
		return currentID;
	}
	
	public String getName() {
		return name;
	}
	
	public void update() {
		if (timeOfDeath != 0 && currentID == null && System.currentTimeMillis() - timeOfDeath >= respawnTime)
			spawn();
	}
	
	public void setDead() {
		timeOfDeath = System.currentTimeMillis();
		currentID = null;
	}
}