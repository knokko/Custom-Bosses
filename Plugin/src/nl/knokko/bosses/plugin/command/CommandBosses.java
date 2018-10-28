package nl.knokko.bosses.plugin.command;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_8_R3.GenericAttributes;
import nl.knokko.bosses.plugin.BossesPlugin;
import nl.knokko.bosses.plugin.boss.AttributeEntry;
import nl.knokko.bosses.plugin.boss.CustomBoss;

public class CommandBosses implements CommandExecutor {
	
	private void sendUseage(CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "Use /bosses spawn/list/hardcode/reload");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length > 0) {
			if (args[0].equals("spawn")) {
				if (sender.hasPermission("custombosses.spawn")) {
					if (args.length >= 2) {
						String name = args[1];
						for (int index = 2; index < args.length; index++)
							name += " " + args[index];
						Collection<CustomBoss> bosses = BossesPlugin.getInstance().getBosses();
						CustomBoss boss = null;
						for (CustomBoss cb : bosses) {
							if (cb.getName().equals(name)) {
								boss = cb;
								break;
							}
						}
						if (boss != null) {
							boss.spawn();
							sender.sendMessage("The boss has been spawned at its spawn location.");
						} else {
							sender.sendMessage(ChatColor.RED + "There is no boss with name " + name);
							sender.sendMessage("Use /custombosses list to get a list of bosses");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Use /custombosses spawn <boss name>");
					}
				} else {
					sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to use this command.");
				}
			} else if(args[0].equals("example")) {
				if (sender.isOp()) {
					Collection<AttributeEntry> attributes = new ArrayList<AttributeEntry>(4);
					attributes.add(new AttributeEntry(GenericAttributes.MOVEMENT_SPEED, 0.3));
					attributes.add(new AttributeEntry(GenericAttributes.ATTACK_DAMAGE, 7));
					attributes.add(new AttributeEntry(GenericAttributes.maxHealth, 80));
					attributes.add(new AttributeEntry(GenericAttributes.FOLLOW_RANGE, 70));
					attributes.add(new AttributeEntry(GenericAttributes.c, 1));
					Collection<ItemStack> drops = new ArrayList<ItemStack>(2);
					drops.add(new ItemStack(Material.ROTTEN_FLESH, 7));
					ItemStack sword = new ItemStack(Material.IRON_SWORD);
					sword.addUnsafeEnchantment(Enchantment.DAMAGE_UNDEAD, 10);
					sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
					drops.add(sword);
					ItemStack weapon = new ItemStack(Material.IRON_AXE);
					weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 7);
					weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
					BossesPlugin.getInstance().getBosses().add(new CustomBoss("Example boss", EntityType.ZOMBIE, 
							"world", 0, 70, 0, 300000, attributes, drops, 10, weapon, new ItemStack(Material.DIAMOND_HELMET), 
							null, new ItemStack(Material.CHAINMAIL_LEGGINGS), null));
					sender.sendMessage("An example boss has been added");
					BossesPlugin.getInstance().save();
				} else {
					sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to use this command.");
				}
			} else if(args[0].equals("list")) {
				if (sender.hasPermission("custombosses.list")) {
					Collection<CustomBoss> bosses = BossesPlugin.getInstance().getBosses();
					if (bosses.isEmpty())
						sender.sendMessage("There are no bosses");
					for (CustomBoss boss : bosses)
						sender.sendMessage((boss.getCurrentID() == null ? ChatColor.GRAY : ChatColor.BLUE) + boss.getName());
				} else {
					sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to use this command.");
				}
			} else if(args[0].equals("reload")) {
				if (sender.hasPermission("custombosses.reload")) {
					BossesPlugin.getInstance().reload();
					sender.sendMessage(ChatColor.GREEN + "The bosses have been reloaded.");
				} else {
					sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to use this command.");
				}
			} else {
				sendUseage(sender);
			}
		} else {
			sendUseage(sender);
		}
		return true;
	}
}