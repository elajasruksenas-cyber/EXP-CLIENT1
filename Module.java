package dev.exp.client.module;

import net.minecraft.client.MinecraftClient;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
	protected static final MinecraftClient mc = MinecraftClient.getInstance();

	public final String name, description;
	public final Category category;
	public final List<Setting> settings = new ArrayList<>();
	public boolean enabled, open;
	public float anim; // GUI animation state

	protected Module(String name, String description, Category category) {
		this.name = name; this.description = description; this.category = category;
	}

	protected Setting add(Setting s) { settings.add(s); return s; }

	public void toggle() { setEnabled(!enabled); }

	public void setEnabled(boolean e) {
		if (e == enabled) return;
		enabled = e;
		if (e) onEnable(); else onDisable();
	}

	protected void onEnable() {}
	protected void onDisable() {}
	public void onTick() {}
}
