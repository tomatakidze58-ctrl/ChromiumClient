package dev.chromium.client.module;

import dev.chromium.client.ChromiumClient;
import dev.chromium.client.util.Hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticlesMode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class PerfModules {

    private PerfModules() {}

    public static class ParticleOptimization extends Module {

        final Setting level =
                mode("Level", 0, "Decreased", "Minimal");

        ParticlesMode old;

        public ParticleOptimization() {
            super("Particle Optimization", Category.PERFORMANCE,
                    "Reduces particle count", false, 0, 0);
        }

        @Override
        public void onEnable() {
            old = MinecraftClient.getInstance()
                    .options.getParticles().getValue();
            apply();
        }

        @Override
        public void onTick(MinecraftClient mc) {
            apply();
        }

        private void apply() {
            MinecraftClient.getInstance()
                    .options.getParticles()
                    .setValue(level.mode == 0
                            ? ParticlesMode.DECREASED
                            : ParticlesMode.MINIMAL);
        }

        @Override
        public void onDisable() {
            if (old != null) {
                MinecraftClient.getInstance()
                        .options.getParticles()
                        .setValue(old);
            }
        }
    }

    public static class FpsLimiter extends Module {

        public final Setting cap =
                num("Max FPS", 240, 30, 260, 10);

        int old = -1;

        public FpsLimiter() {
            super("FPS Limiter", Category.PERFORMANCE,
                    "Caps the frame rate", false, 0, 0);
        }

        @Override
        public void onEnable() {
            old = MinecraftClient.getInstance()
                    .options.getMaxFps().getValue();
        }

        @Override
        public void onTick(MinecraftClient mc) {
            if (mc.options.getMaxFps().getValue() != cap.i()) {
                mc.options.getMaxFps().setValue(cap.i());
            }
        }

        @Override
        public void onDisable() {
            if (old > 0) {
                MinecraftClient.getInstance()
                        .options.getMaxFps().setValue(old);
            }
        }
    }

    public static class DynamicFps extends Module {

        final Setting bgFps =
                num("Unfocused FPS", 15, 1, 60, 1);

        int original = -1;
        boolean throttled;

        public DynamicFps() {
            super("Dynamic FPS", Category.PERFORMANCE,
                    "Lowers FPS while unfocused", false, 0, 0);
            on();
        }

        @Override
        public void onTick(MinecraftClient mc) {

            boolean focused = mc.isWindowFocused();

            if (!focused && !throttled) {
                original = mc.options.getMaxFps().getValue();
                throttled = true;
            }

            if (!focused) {
                mc.options.getMaxFps().setValue(bgFps.i());
            } else if (throttled) {

                throttled = false;

                FpsLimiter limiter =
                        ChromiumClient.mod(FpsLimiter.class);

                mc.options.getMaxFps().setValue(
                        limiter != null && limiter.enabled
                                ? limiter.cap.i()
                                : original
                );
            }
        }

        @Override
        public void onDisable() {
            if (throttled && original > 0) {
                MinecraftClient.getInstance()
                        .options.getMaxFps().setValue(original);
            }

            throttled = false;
        }
    }

    public static class WeatherOptimization extends Module {

        final Setting cap =
                num("Max Rain Intensity %", 40, 0, 100, 5);

        public WeatherOptimization() {
            super("Weather Optimization", Category.PERFORMANCE,
                    "Reduces weather rendering load", false, 0, 0);
        }

        @Override
        public void onTick(MinecraftClient mc) {

            if (mc.world == null) {
                return;
            }

            float c = cap.f() / 100f;

            if (mc.world.getRainGradient(1f) > c) {
                mc.world.setRainGradient(c);
            }

            if (mc.world.getThunderGradient(1f) > c) {
                mc.world.setThunderGradient(c);
            }
        }
    }
}
