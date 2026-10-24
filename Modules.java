package dev.exp.client.module;

import dev.exp.client.util.MoveUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

public final class Modules {
	private Modules() {}

	// ───────────────────────── COMBAT ─────────────────────────
	public static class KillAura extends Module {
		private final Setting range = add(new Setting("Range", 4.0, 2.0, 6.0, 0.1));
		private final Setting mobs = add(new Setting("Mobs", false));

		public KillAura() { super("KillAura", "Attacks the nearest target in range", Category.COMBAT); }

		@Override public void onTick() {
			ClientPlayerEntity p = mc.player;
			if (p == null || mc.world == null || mc.interactionManager == null) return;
			if (p.getAttackCooldownProgress(0.5f) < 1f) return;

			Entity best = null; double bestD = range.d();
			for (Entity e : mc.world.getEntities()) {
				if (e == p || !(e instanceof LivingEntity le) || !le.isAlive() || e instanceof ArmorStandEntity) continue;
				if (!(e instanceof PlayerEntity) && !mobs.on()) continue;
				double d = p.distanceTo(e);
				if (d <= bestD) { bestD = d; best = e; }
			}
			if (best != null) {
				mc.interactionManager.attackEntity(p, best);
				p.swingHand(Hand.MAIN_HAND);
			}
		}
	}

	// ───────────────────────── MOVEMENT ─────────────────────────
	public static class Sprint extends Module {
		public Sprint() { super("Sprint", "Always sprint while moving forward", Category.MOVEMENT); }
		@Override public void onTick() {
			ClientPlayerEntity p = mc.player;
			if (mc.options.forwardKey.isPressed() && !p.isSneaking() && !p.horizontalCollision
				&& p.getHungerManager().getFoodLevel() > 6) p.setSprinting(true);
		}
	}

	public static class Speed extends Module {
		private final Setting speed = add(new Setting("Speed", 0.35, 0.1, 1.5, 0.01));
		public Speed() { super("Speed", "Boosts horizontal movement", Category.MOVEMENT); }
		@Override public void onTick() {
			ClientPlayerEntity p = mc.player;
			double[] d = MoveUtil.dir(p);
			if (d == null) return;
			p.setVelocity(d[0] * speed.d(), p.getVelocity().y, d[1] * speed.d());
		}
	}

	public static class Fly extends Module {
		private final Setting speed = add(new Setting("Speed", 1.0, 0.1, 5.0, 0.1));
		public Fly() { super("Fly", "Creative-style flight (Space up / Shift down)", Category.MOVEMENT); }
		@Override public void onTick() {
			ClientPlayerEntity p = mc.player;
			double s = speed.d();
			double vy = mc.options.jumpKey.isPressed() ? s : mc.options.sneakKey.isPressed() ? -s : 0;
			double[] d = MoveUtil.dir(p);
			p.setVelocity(d == null ? 0 : d[0] * s, vy, d == null ? 0 : d[1] * s);
		}
	}

	public static class Step extends Module {
		private final Setting height = add(new Setting("Height", 1.5, 0.6, 2.5, 0.1));
		public Step() { super("Step", "Walk up blocks without jumping", Category.MOVEMENT); }
		@Override public void onTick() { apply(height.d()); }
		@Override protected void onDisable() { apply(0.6); }
		private void apply(double h) {
			if (mc.player == null) return;
			var attr = mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
			if (attr != null) attr.setBaseValue(h);
		}
	}

	public static class NoFall extends Module {
		public NoFall() { super("NoFall", "Cancels fall damage", Category.MOVEMENT); }
		@Override public void onTick() {
			ClientPlayerEntity p = mc.player;
			if (p.fallDistance > 2.5 && mc.getNetworkHandler() != null)
				mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, p.horizontalCollision));
		}
	}

	public static class AutoWalk extends Module {
		public AutoWalk() { super("AutoWalk", "Holds the forward key for you", Category.MOVEMENT); }
		@Override public void onTick() { mc.options.forwardKey.setPressed(true); }
		@Override protected void onDisable() { mc.options.forwardKey.setPressed(false); }
	}

	// ───────────────────────── RENDER ─────────────────────────
	public static class Fullbright extends Module {
		public Fullbright() { super("Fullbright", "Permanent night vision (client-side)", Category.RENDER); }
		@Override public void onTick() {
			ClientPlayerEntity p = mc.player;
			StatusEffectInstance cur = p.getStatusEffect(StatusEffects.NIGHT_VISION);
			if (cur == null || cur.getDuration() < 400)
				p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 2000, 0, false, false, false));
		}
		@Override protected void onDisable() {
			if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
		}
	}

	/** Uses the vanilla glow outline, so it needs no custom render code. */
	public static class ESP extends Module {
		private final Setting mobs = add(new Setting("Mobs", false));
		public ESP() { super("ESP", "Outlines players through walls", Category.RENDER); }
		@Override public void onTick() {
			for (Entity e : mc.world.getEntities()) {
				if (e == mc.player) continue;
				if (e instanceof PlayerEntity || (mobs.on() && e instanceof LivingEntity && !(e instanceof ArmorStandEntity)))
					e.setGlowing(true);
				else if (e.isGlowing() && !mobs.on() && e instanceof LivingEntity) e.setGlowing(false);
			}
		}
		@Override protected void onDisable() {
			if (mc.world == null) return;
			for (Entity e : mc.world.getEntities()) if (e instanceof LivingEntity) e.setGlowing(false);
		}
	}

	// ───────────────────────── MISC ─────────────────────────
	public static class AutoFish extends Module {
		private final Setting bite = add(new Setting("Bite Speed", 0.15, 0.05, 0.5, 0.01));
		private final Setting recastDelay = add(new Setting("Recast Delay", 15, 5, 60, 1));
		private final Setting autoCast = add(new Setting("Auto Recast", true));
		private int cooldown;
		private boolean needCast;

		public AutoFish() { super("AutoFish", "Reels in on bite, then recasts", Category.MISC); }

		@Override protected void onEnable() { cooldown = 0; needCast = false; }

		@Override public void onTick() {
			ClientPlayerEntity p = mc.player;
			if (mc.interactionManager == null) return;
			if (cooldown > 0) { cooldown--; return; }
			if (!p.getMainHandStack().isOf(Items.FISHING_ROD)) return;

			FishingBobberEntity hook = p.fishHook;
			if (hook == null) {
				if (needCast && autoCast.on()) { use(p); needCast = false; cooldown = 20; }
				return;
			}
			// A bite yanks the bobber downward; watch its vertical velocity.
			if (hook.age > 20 && hook.isTouchingWater() && hook.getVelocity().y < -bite.d()) {
				use(p);
				needCast = true;
				cooldown = recastDelay.i();
			}
		}

		private void use(ClientPlayerEntity p) {
			mc.interactionManager.interactItem(p, Hand.MAIN_HAND);
			p.swingHand(Hand.MAIN_HAND);
		}
	}
}
