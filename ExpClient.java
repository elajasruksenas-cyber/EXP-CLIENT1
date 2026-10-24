package dev.exp.client;

import dev.exp.client.gui.ClickGui;
import dev.exp.client.gui.Hud;
import dev.exp.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ExpClient implements ClientModInitializer {
	private boolean wasDown;

	@Override
	public void onInitializeClient() {
		ModuleManager.init();

		HudElementRegistry.attachElementAfter(
			VanillaHudElements.MISC_OVERLAYS,
			Identifier.of("expclient", "hud"),
			(ctx, tick) -> Hud.render(ctx));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			// Menu key: Right Shift (polled directly, no KeyBinding needed)
			boolean down = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
			if (down && !wasDown) {
				if (mc.currentScreen == null && mc.player != null) mc.setScreen(new ClickGui());
				else if (mc.currentScreen instanceof ClickGui) mc.setScreen(null);
			}
			wasDown = down;

			if (mc.player != null && mc.world != null) ModuleManager.tick();
		});
	}
}
