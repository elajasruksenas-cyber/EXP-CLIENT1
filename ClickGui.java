package dev.exp.client.gui;

import dev.exp.client.module.Category;
import dev.exp.client.module.Module;
import dev.exp.client.module.ModuleManager;
import dev.exp.client.module.Setting;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ClickGui extends Screen {
	private static final int W = 112, HEAD = 18, ROW = 15, SROW = 13;
	/** Panel positions survive closing/reopening the menu. */
	private static final Map<Category, int[]> POS = new EnumMap<>(Category.class);

	private record Panel(Category cat, int[] pos, List<Module> mods) {}

	private final List<Panel> panels = new ArrayList<>();
	private Panel dragging;
	private int dragDX, dragDY;
	private Setting sliding;
	private int slideX;

	public ClickGui() {
		super(Text.literal("Exp Client"));
		for (Category c : Category.values()) {
			int[] pos = POS.computeIfAbsent(c, k -> new int[]{24 + k.ordinal() * (W + 10), 24});
			panels.add(new Panel(c, pos, ModuleManager.byCategory(c)));
		}
	}

	@Override public boolean shouldPause() { return false; }

	private static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	private static int panelHeight(Panel p) {
		int h = HEAD;
		for (Module m : p.mods()) { h += ROW; if (m.open) h += m.settings.size() * SROW; }
		return h + 2;
	}

	// ───────────────────────── rendering ─────────────────────────
	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		ctx.fill(0, 0, width, height, 0x78000000);
		Module hovered = null;
		int idx = 0;

		for (Panel p : panels) {
			int x = p.pos()[0], y = p.pos()[1], h = panelHeight(p);
			int accent = Theme.accent(idx * 3);

			Theme.rrect(ctx, x - 1, y - 1, W + 2, h + 2, Theme.alpha(accent, 0x55)); // glow border
			Theme.rrect(ctx, x, y, W, h, Theme.BG);
			ctx.fill(x, y, x + W, y + HEAD, Theme.HEAD);
			ctx.fill(x, y + HEAD - 1, x + W, y + HEAD, accent);
			Theme.text(ctx, p.cat().label, x + (W - Theme.width(p.cat().label)) / 2, y + 5, Theme.TEXT);

			int cy = y + HEAD;
			for (Module m : p.mods()) {
				m.anim += ((m.enabled ? 1f : 0f) - m.anim) * 0.22f;
				boolean hot = inside(mx, my, x, cy, W, ROW);
				if (hot) hovered = m;

				int bg = Theme.lerp(hot ? Theme.ROW_HOT : Theme.ROW, Theme.alpha(accent, 0x58), m.anim);
				ctx.fill(x + 2, cy, x + W - 2, cy + ROW, bg);
				ctx.fill(x + 2, cy, x + 2 + Math.round(2 * m.anim), cy + ROW, accent);
				Theme.text(ctx, m.name, x + 8, cy + 4, Theme.lerp(Theme.DIM, Theme.TEXT, Math.max(m.anim, hot ? 0.7f : 0f)));
				if (!m.settings.isEmpty()) Theme.text(ctx, m.open ? "-" : "+", x + W - 11, cy + 4, Theme.DIM);
				cy += ROW;

				if (m.open) {
					for (Setting s : m.settings) {
						ctx.fill(x + 2, cy, x + W - 2, cy + SROW, Theme.SET_BG);
						if (!s.bool) {
							float t = (float) ((s.value - s.min) / (s.max - s.min));
							ctx.fill(x + 2, cy, x + 2 + Math.round((W - 4) * t), cy + SROW, Theme.alpha(accent, 0x50));
						}
						Theme.text(ctx, s.name, x + 8, cy + 3, Theme.DIM);
						String v = s.display();
						Theme.text(ctx, v, x + W - 8 - Theme.width(v), cy + 3, s.bool && s.on() ? accent : Theme.TEXT);
						cy += SROW;
					}
				}
			}
			idx++;
		}

		if (hovered != null) {
			String d = hovered.description;
			int tw = Theme.width(d);
			int tx = Math.min(mx + 10, width - tw - 8), ty = my + 10;
			Theme.rrect(ctx, tx - 3, ty - 3, tw + 6, 14, 0xF0000000);
			Theme.text(ctx, d, tx, ty, Theme.TEXT);
		}

		String hint = "LMB toggle  |  RMB settings  |  drag headers  |  RShift close";
		Theme.text(ctx, hint, (width - Theme.width(hint)) / 2, height - 14, Theme.DIM);
	}

	// ───────────────────────── input ─────────────────────────
	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		double mx = click.x(), my = click.y();
		int btn = click.button();

		for (Panel p : panels) {
			int x = p.pos()[0], y = p.pos()[1];
			if (inside(mx, my, x, y, W, HEAD)) {
				if (btn == 0) { dragging = p; dragDX = (int) mx - x; dragDY = (int) my - y; }
				return true;
			}
			int cy = y + HEAD;
			for (Module m : p.mods()) {
				if (inside(mx, my, x, cy, W, ROW)) {
					if (btn == 0) m.toggle();
					else if (btn == 1 && !m.settings.isEmpty()) m.open = !m.open;
					return true;
				}
				cy += ROW;
				if (m.open) {
					for (Setting s : m.settings) {
						if (inside(mx, my, x, cy, W, SROW)) {
							if (s.bool) { if (btn == 0) s.set(s.on() ? 0 : 1); }
							else if (btn == 0) { sliding = s; slideX = x; slide(mx); }
							return true;
						}
						cy += SROW;
					}
				}
			}
		}
		return super.mouseClicked(click, doubled);
	}

	private void slide(double mx) {
		double t = (mx - (slideX + 2)) / (W - 4);
		t = Math.max(0, Math.min(1, t));
		sliding.set(sliding.min + t * (sliding.max - sliding.min));
	}

	@Override
	public boolean mouseDragged(Click click, double dx, double dy) {
		if (dragging != null) {
			dragging.pos()[0] = (int) click.x() - dragDX;
			dragging.pos()[1] = (int) click.y() - dragDY;
			return true;
		}
		if (sliding != null) { slide(click.x()); return true; }
		return super.mouseDragged(click, dx, dy);
	}

	@Override
	public boolean mouseReleased(Click click) {
		dragging = null;
		sliding = null;
		return super.mouseReleased(click);
	}
}
