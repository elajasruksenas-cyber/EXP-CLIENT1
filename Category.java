package dev.exp.client.module;

public enum Category {
	COMBAT("Combat"), MOVEMENT("Movement"), RENDER("Render"), MISC("Misc");
	public final String label;
	Category(String l) { label = l; }
}
