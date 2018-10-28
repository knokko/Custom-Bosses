package nl.knokko.bosses.plugin.boss;

import net.minecraft.server.v1_8_R3.IAttribute;

public class AttributeEntry {
	
	private final IAttribute attribute;
	private final double value;
	
	public AttributeEntry(IAttribute attribute, double value) {
		this.attribute = attribute;
		this.value = value;
	}
	
	public IAttribute getAttribute() {
		return attribute;
	}
	
	public double getValue() {
		return value;
	}
}