package dev.exp.client.gui;

import dev.exp.client.module.Module;
import dev.exp.client.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.Comparator;
import java.util.List;

public final class Hud {
	private Hud() {}

	public static void render(DrawContext ctx) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.options.hudHidden) return;

		// Watermark with per-letter gradient
		String wm = "Exp Client";
		int x = 5;
		for (int i = 0; i < wm.length(); i++) {
			String ch = String.valueOf(wm.charAt(i));
			Theme.text(ctx, ch, x, 5, Theme.accent(i));
			x += Theme.width(ch);
		}
		Theme.text(ctx, mc.getCurrentFps() + " fps", 5, 16, Theme.DIM);

		// Array list (right side, longest first)
		List<Module> on = ModuleManager.all().stream()
			.filter(m -> m.enabled)
			.sorted(Comparator.comparingInt((Module m) -> -Theme.width(m.name)))
			.toList();

		int sw = ctx.getScaledWindowWidth();
		int y = 3, i = 0;
		for (Module m : on) {
			int w = Theme.width(m.name);
			int col = Theme.accent(i);
			ctx.fill(sw - w - 8, y, sw, y + 11, 0x95000000);
			ctx.fill(sw - 2, y, sw, y + 11, col);
			Theme.text(ctx, m.name, sw - w - 5, y + 2, col);
			y += 11; i++;
		}
	}
}
