package nl.knokko.bosses.plugin.boss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.IAttribute;

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
		for (String attKey : attKeys) {
			IAttribute iat = null;
			if (attKey.equals("MAX_HEALTH"))
				iat = GenericAttributes.maxHealth;
			else if (attKey.equals("ATTACK_DAMAGE"))
				iat = GenericAttributes.ATTACK_DAMAGE;
			else if (attKey.equals("MOVEMENT_SPEED"))
				iat = GenericAttributes.MOVEMENT_SPEED;
			else if (attKey.equals("FOLLOW_RANGE"))
				iat = GenericAttributes.FOLLOW_RANGE;
			else if (attKey.equals("KNOCKBACK_RESISTANCE"))
				iat = GenericAttributes.c;
			else
				Bukkit.getLogger().warning("Unsupported attribute: " + attKey);
			if (iat != null)
				attributes.add(new AttributeEntry(iat, attSection.getDouble(attKey)));
		}
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
		for (AttributeEntry attribute : attributes) {
			String attributeName = null;
			IAttribute a = attribute.getAttribute();
			if (a == GenericAttributes.maxHealth)
				attributeName = "MAX_HEALTH";
			else if (a == GenericAttributes.ATTACK_DAMAGE)
				attributeName = "ATTACK_DAMAGE";
			else if (a == GenericAttributes.MOVEMENT_SPEED)
				attributeName = "MOVEMENT_SPEED";
			else if (a == GenericAttributes.FOLLOW_RANGE)
				attributeName = "FOLLOW_RANGE";
			else if (a == GenericAttributes.c)
				attributeName = "KNOCKBACK_RESISTANCE";
			else
				Bukkit.getLogger().severe("Unknown entity attribute: " + a.getName());
			if (attributeName != null)
				attSection.set(attributeName, attribute.getValue());
		}
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
		currentID = entity.getUniqueId();
		if (entity instanceof LivingEntity) {
			LivingEntity le = (LivingEntity) entity;
			EntityLiving el = ((CraftLivingEntity)le).getHandle();
			for (AttributeEntry attribute : attributes) {
				el.getAttributeInstance(attribute.getAttribute()).setValue(attribute.getValue());
				if (attribute.getAttribute() == GenericAttributes.maxHealth)
					le.setHealth(attribute.getValue());
			}
			EntityEquipment eq = le.getEquipment();
			le.setRemoveWhenFarAway(false);
			// Even if they happen to be null, the entity should be forced not to have any equipment there
			eq.setItemInHand(weapon);
			eq.setHelmet(helmet);
			eq.setChestplate(chestplate);
			eq.setLeggings(leggings);
			eq.setBoots(boots);
			eq.setItemInHandDropChance(0);
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