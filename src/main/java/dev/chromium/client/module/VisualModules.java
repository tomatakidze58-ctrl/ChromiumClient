package dev.chromium.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public final class VisualModules {
    private VisualModules() {}

    // Night vision effect applied client-side only (no gamma hacks)
    public static class Fullbright extends Module {
        public Fullbright() { super("Fullbright", Category.VISUAL, "Maximum brightness", false, 0, 0); }
        @Override public void onTick(MinecraftClient mc) {
            var p = mc.player; if (p == null) return;
            StatusEffectInstance cur = p.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (cur == null || (!cur.shouldShowIcon() && cur.getDuration() < 240))
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false, false));
        }
        @Override public void onDisable() {
            var p = MinecraftClient.getInstance().player; if (p == null) return;
            StatusEffectInstance cur = p.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (cur != null && !cur.shouldShowIcon()) p.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    // Hold the Zoom key (default C). FOV is divided in GameRendererMixin.
    public static class Zoom extends Module {
        public final Setting level = num("Zoom Level", 4.0, 1.5, 12.0, 0.5);
        final Setting smooth = bool("Smooth", true), lowSens = bool("Lower Sensitivity", true);
        public static float factor = 1f;
        double oldSens = -1;
        public Zoom() { super("Zoom", Category.VISUAL, "Hold C to zoom", false, 0, 0); on(); }
        @Override public void onTick(MinecraftClient mc) {
            boolean zoom = mc.currentScreen == null && dev.chromium.client.ChromiumClient.zoomKey.isPressed();
            float target = zoom ? level.f() : 1f;
            factor = smooth.b ? factor + (target - factor) * 0.35f : target;
            if (Math.abs(factor - target) < 0.01f) factor = target;
            if (lowSens.b) {
                if (zoom && oldSens < 0) { oldSens = mc.options.getMouseSensitivity().getValue(); mc.options.getMouseSensitivity().setValue(oldSens / Math.sqrt(level.n)); }
                else if (!zoom && oldSens >= 0) { mc.options.getMouseSensitivity().setValue(oldSens); oldSens = -1; }
            }
        }
        @Override public void onDisable() {
            factor = 1f;
            if (oldSens >= 0) { MinecraftClient.getInstance().options.getMouseSensitivity().setValue(oldSens); oldSens = -1; }
        }
    }

    public static class WeatherChanger extends Module {
        final Setting weather = mode("Weather", 1, "Server", "Clear", "Rain", "Thunder");
        public WeatherChanger() { super("Weather Changer", Category.VISUAL, "Client-side weather", false, 0, 0); }
        @Override public void onTick(MinecraftClient mc) {
            if (mc.world == null || weather.mode == 0) return;
            float rain = weather.mode >= 2 ? 1f : 0f, thunder = weather.mode == 3 ? 1f : 0f;
            mc.world.setRainGradient(rain); mc.world.setThunderGradient(thunder);
        }
    }

    // The next three are applied by InGameHudMixin
    public static class NoVignette extends Module {
        public NoVignette() { super("No Vignette", Category.VISUAL, "Hides the screen-edge vignette", false, 0, 0); on(); }
    }
    public static class NoPumpkinOverlay extends Module {
        public NoPumpkinOverlay() { super("No Pumpkin Overlay", Category.VISUAL, "Hides the pumpkin blur", false, 0, 0); on(); }
    }
    public static class NoPortalOverlay extends Module {
        public NoPortalOverlay() { super("No Portal Overlay", Category.VISUAL, "Hides the nether portal swirl", false, 0, 0); on(); }
    }

    // Applied by WorldRendererMixin
    public static class CustomBlockOutline extends Module {
        public final Setting color = color("Color", 0xFF4DA3FF), alpha = num("Opacity", 255, 40, 255, 5), rainbow = bool("Rainbow", false);
        public CustomBlockOutline() { super("Custom Block Outline", Category.VISUAL, "Colored block selection outline", false, 0, 0); }
        public int argb() {
            int rgb = color.color & 0xFFFFFF;
            if (rainbow.b) rgb = net.minecraft.util.math.MathHelper.hsvToRgb((System.currentTimeMillis() % 3000L) / 3000f, 0.8f, 1f);
            return (alpha.i() << 24) | rgb;
        }
    }

    // Applied by WorldMixin (sky angle: 0 noon, .25 sunset, .5 midnight, .75 sunrise)
    public static class CustomSky extends Module {
        public final Setting preset = mode("Preset", 1, "Noon", "Sunset", "Midnight", "Sunrise", "Cycle (Fast)");
        public CustomSky() { super("Custom Sky", Category.VISUAL, "Forces a sky/time-of-day look (client only)", false, 0, 0); }
        public float angle() {
            return switch (preset.mode) {
                case 0 -> 0f; case 1 -> 0.25f; case 2 -> 0.5f; case 3 -> 0.75f;
                default -> (System.currentTimeMillis() % 60000L) / 60000f;
            };
        }
    }
}
