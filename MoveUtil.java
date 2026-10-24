package dev.exp.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class MoveUtil {
	private MoveUtil() {}

	/** Unit horizontal direction from WASD + yaw, or null when no movement key is held. */
	public static double[] dir(ClientPlayerEntity p) {
		var o = MinecraftClient.getInstance().options;
		int f = (o.forwardKey.isPressed() ? 1 : 0) - (o.backKey.isPressed() ? 1 : 0);
		int s = (o.leftKey.isPressed() ? 1 : 0) - (o.rightKey.isPressed() ? 1 : 0);
		if (f == 0 && s == 0) return null;
		double yaw = Math.toRadians(p.getYaw());
		double sin = Math.sin(yaw), cos = Math.cos(yaw);
		double dx = -sin * f + cos * s;
		double dz = cos * f + sin * s;
		double len = Math.sqrt(dx * dx + dz * dz);
		return new double[]{dx / len, dz / len};
	}
}
