package dev.exp.client.module;

public class Setting {
	public final String name;
	public final double min, max, step;
	public final boolean bool;
	public double value;

	public Setting(String name, boolean v) {
		this.name = name; this.bool = true; this.min = 0; this.max = 1; this.step = 1; this.value = v ? 1 : 0;
	}

	public Setting(String name, double v, double min, double max, double step) {
		this.name = name; this.bool = false; this.value = v; this.min = min; this.max = max; this.step = step;
	}

	public boolean on() { return value > 0.5; }
	public double d() { return value; }
	public int i() { return (int) Math.round(value); }

	public void set(double v) {
		double c = Math.max(min, Math.min(max, v));
		if (!bool && step > 0) c = min + Math.round((c - min) / step) * step;
		value = Math.max(min, Math.min(max, c));
	}

	public String display() {
		if (bool) return on() ? "On" : "Off";
		return step >= 1 ? String.valueOf(i()) : String.format("%.2f", value);
	}
}
