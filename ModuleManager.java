package dev.exp.client.module;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
	private static final List<Module> MODULES = new ArrayList<>();
	private ModuleManager() {}

	public static void init() {
		MODULES.add(new Modules.KillAura());
		MODULES.add(new Modules.Sprint());
		MODULES.add(new Modules.Speed());
		MODULES.add(new Modules.Fly());
		MODULES.add(new Modules.Step());
		MODULES.add(new Modules.NoFall());
		MODULES.add(new Modules.AutoWalk());
		MODULES.add(new Modules.Fullbright());
		MODULES.add(new Modules.ESP());
		MODULES.add(new Modules.AutoFish());
	}

	public static List<Module> all() { return MODULES; }

	public static List<Module> byCategory(Category c) {
		List<Module> out = new ArrayList<>();
		for (Module m : MODULES) if (m.category == c) out.add(m);
		return out;
	}

	public static void tick() {
		for (Module m : MODULES) {
			if (!m.enabled) continue;
			try { m.onTick(); } catch (Throwable t) { t.printStackTrace(); m.setEnabled(false); }
		}
	}
}
