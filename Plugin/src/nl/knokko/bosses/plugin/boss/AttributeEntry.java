package nl.knokko.bosses.plugin.boss;

import org.bukkit.attribute.Attribute;

public class AttributeEntry {
	
	private final Attribute attribute;
	private final double value;
	
	public AttributeEntry(Attribute attribute, double value) {
		this.attribute = attribute;
		this.value = value;
	}
	
	public Attribute getAttribute() {
		return attribute;
	}
	
	public double getValue() {
		return value;
	}
}