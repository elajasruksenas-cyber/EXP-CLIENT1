package dev.exp.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public final class Theme {
	private Theme() {}

	public static final int BG       = 0xEB0E0F14;
	public static final int HEAD     = 0xF0171922;
	public static final int ROW      = 0xD2131520;
	public static final int ROW_HOT  = 0xD21C1F2B;
	public static final int SET_BG   = 0xD20E101A;
	public static final int TEXT     = 0xFFEAECF2;
	public static final int DIM      = 0xFF8C92A3;

	/** Slowly cycling accent colour; offset shifts the hue so lists get a gradient. */
	public static int accent(int offset) {
		float h = ((System.currentTimeMillis() % 6000L) / 6000f + offset * 0.035f) % 1f;
		return 0xFF000000 | MathHelper.hsvToRgb(h, 0.55f, 1f);
	}

	public static int alpha(int color, int a) { return (color & 0x00FFFFFF) | (a << 24); }

	public static int lerp(int a, int b, float t) {
		t = Math.max(0, Math.min(1, t));
		int aa = (int) (((a >>> 24) & 255) + (((b >>> 24) & 255) - ((a >>> 24) & 255)) * t);
		int ar = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
		int ag = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
		int ab = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
		return (aa << 24) | (ar << 16) | (ag << 8) | ab;
	}

	/** Cheap rounded rectangle (1px corner cut). */
	public static void rrect(DrawContext c, int x, int y, int w, int h, int col) {
		c.fill(x + 1, y, x + w - 1, y + h, col);
		c.fill(x, y + 1, x + w, y + h - 1, col);
	}

	public static void text(DrawContext c, String s, int x, int y, int col) {
		c.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, s, x, y, col);
	}

	public static int width(String s) { return MinecraftClient.getInstance().textRenderer.getWidth(s); }
}
