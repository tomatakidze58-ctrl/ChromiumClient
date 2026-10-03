package dev.chromium.client.module;

import dev.chromium.client.ChromiumClient;
import dev.chromium.client.util.Hud;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.ChunkBuilderMode;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticlesMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class PerfModules {
    private PerfModules() {}

    // Distance culling of entities (applied in EntityRenderDispatcherMixin)
    public static class EntityCulling extends Module {
        public final Setting distance = num("Max Distance", 48, 16, 128, 4);
        public final Setting players = bool("Never Cull Players", true);
        public EntityCulling() { super("Entity Culling", Category.PERFORMANCE, "Skips rendering far entities", false, 0, 0); on(); }
        public boolean cull(Entity e) {
            var p = MinecraftClient.getInstance().player;
            if (p == null || e == p) return false;
            if (players.b && e instanceof PlayerEntity) return false;
            double d = distance.n;
            return p.squaredDistanceTo(e) > d * d;
        }
    }

    // Distance culling of chests/signs/banners... (applied in BlockEntityRenderDispatcherMixin)
    public static class BlockEntityCulling extends Module {
        public final Setting distance = num("Max Distance", 32, 8, 96, 4);
        public BlockEntityCulling() { super("Block Entity Culling", Category.PERFORMANCE, "Skips far chests, signs, banners", false, 0, 0); on(); }
        public boolean cull(BlockEntity be) {
            var p = MinecraftClient.getInstance().player;
            if (p == null) return false;
            double d = distance.n;
            return p.squaredDistanceTo(be.getPos().getX() + .5, be.getPos().getY() + .5, be.getPos().getZ() + .5) > d * d;
        }
    }

    public static class ParticleOptimization extends Module {
        final Setting level = mode("Level", 0, "Decreased", "Minimal");
        ParticlesMode old;
        public ParticleOptimization() { super("Particle Optimization", Category.PERFORMANCE, "Reduces particle count", false, 0, 0); }
        @Override public void onEnable() { old = MinecraftClient.getInstance().options.getParticles().getValue(); apply(); }
        @Override public void onTick(MinecraftClient mc) { apply(); }
        private void apply() {
            MinecraftClient.getInstance().options.getParticles().setValue(level.mode == 0 ? ParticlesMode.DECREASED : ParticlesMode.MINIMAL);
        }
        @Override public void onDisable() { if (old != null) MinecraftClient.getInstance().options.getParticles().setValue(old); }
    }

    public static class FpsLimiter extends Module {
        public final Setting cap = num("Max FPS", 240, 30, 260, 10);
        int old = -1;
        public FpsLimiter() { super("FPS Limiter", Category.PERFORMANCE, "Caps the frame rate", false, 0, 0); }
        @Override public void onEnable() { old = MinecraftClient.getInstance().options.getMaxFps().getValue(); }
        @Override public void onTick(MinecraftClient mc) {
            if (mc.options.getMaxFps().getValue() != cap.i()) mc.options.getMaxFps().setValue(cap.i());
        }
        @Override public void onDisable() { if (old > 0) MinecraftClient.getInstance().options.getMaxFps().setValue(old); }
    }

    public static class DynamicFps extends Module {
        final Setting bgFps = num("Unfocused FPS", 15, 1, 60, 1);
        int original = -1; boolean throttled;
        public DynamicFps() { super("Dynamic FPS", Category.PERFORMANCE, "Lowers FPS while the window is unfocused", false, 0, 0); on(); }
        @Override public void onTick(MinecraftClient mc) {
            boolean focused = mc.isWindowFocused();
            if (!focused && !throttled) {
                original = mc.options.getMaxFps().getValue(); throttled = true;
            }
            if (!focused) mc.options.getMaxFps().setValue(bgFps.i());
            else if (throttled) {
                throttled = false;
                FpsLimiter l = ChromiumClient.mod(FpsLimiter.class);
                mc.options.getMaxFps().setValue(l != null && l.enabled ? l.cap.i() : original);
            }
        }
        @Override public void onDisable() {
            if (throttled && original > 0) MinecraftClient.getInstance().options.getMaxFps().setValue(original);
            throttled = false;
        }
    }

    public static class FastRendering extends Module {
        GraphicsMode gm; Boolean ao, shadows; CloudRenderMode clouds;
        final Setting keepAo = bool("Keep Smooth Lighting", false);
        public FastRendering() { super("Fast Rendering", Category.PERFORMANCE, "Fast graphics, no shadows/clouds", false, 0, 0); on(); }
        @Override public void onEnable() {
            var o = MinecraftClient.getInstance().options;
            gm = o.getGraphicsMode().getValue(); ao = o.getAo().getValue();
            shadows = o.getEntityShadows().getValue(); clouds = o.getCloudRenderMode().getValue();
            o.getGraphicsMode().setValue(GraphicsMode.FAST);
            if (!keepAo.b) o.getAo().setValue(false);
            o.getEntityShadows().setValue(false);
            o.getCloudRenderMode().setValue(CloudRenderMode.OFF);
        }
        @Override public void onDisable() {
            var o = MinecraftClient.getInstance().options;
            if (gm != null) o.getGraphicsMode().setValue(gm);
            if (ao != null) o.getAo().setValue(ao);
            if (shadows != null) o.getEntityShadows().setValue(shadows);
            if (clouds != null) o.getCloudRenderMode().setValue(clouds);
        }
    }

    public static class FastChunkLoading extends Module {
        ChunkBuilderMode old;
        public FastChunkLoading() { super("Fast Chunk Loading", Category.PERFORMANCE, "Threaded chunk builder, no blocking", false, 0, 0); on(); }
        @Override public void onEnable() {
            var o = MinecraftClient.getInstance().options;
            old = o.getChunkBuilderMode().getValue();
            o.getChunkBuilderMode().setValue(ChunkBuilderMode.NONE);
        }
        @Override public void onDisable() { if (old != null) MinecraftClient.getInstance().options.getChunkBuilderMode().setValue(old); }
    }

    // Texture animation throttling (applied in SpriteAtlasTextureMixin)
    public static class AnimationOptimization extends Module {
        public final Setting rate = mode("Texture Updates", 1, "Frozen", "Half Rate", "Quarter Rate");
        int n;
        public AnimationOptimization() { super("Animation Optimization", Category.PERFORMANCE, "Throttles animated textures", false, 0, 0); }
        public boolean skip() {
            n++;
            return switch (rate.mode) { case 0 -> true; case 1 -> (n & 1) == 1; default -> (n & 3) != 0; };
        }
    }

    public static class WeatherOptimization extends Module {
        final Setting cap = num("Max Rain Intensity %", 40, 0, 100, 5);
        public WeatherOptimization() { super("Weather Optimization", Category.PERFORMANCE, "Caps rain/thunder rendering load", false, 0, 0); }
        @Override public void onTick(MinecraftClient mc) {
            if (mc.world == null) return;
            float c = cap.f() / 100f;
            if (mc.world.getRainGradient(1f) > c) mc.world.setRainGradient(c);
            if (mc.world.getThunderGradient(1f) > c) mc.world.setThunderGradient(c);
        }
    }

    public static class PerformanceProfiler extends Module {
        final Setting graph = bool("Frame Graph", true);
        final Deque<Long> frames = new ArrayDeque<>();
        long last;
        public PerformanceProfiler() { super("Performance Profiler", Category.PERFORMANCE, "Frame time, 1% lows, entities, memory", true, 200, 6); }
        @Override public void onHud(DrawContext ctx, MinecraftClient mc) {
            long now = System.nanoTime();
            if (last != 0) { frames.add(now - last); if (frames.size() > 300) frames.pollFirst(); }
            last = now;
            if (frames.isEmpty() || mc.world == null) return;
            List<Long> sorted = new ArrayList<>(frames); sorted.sort(null);
            double avg = frames.stream().mapToLong(Long::longValue).average().orElse(1) / 1e6;
            double worst1 = sorted.subList((int) (sorted.size() * 0.99), sorted.size()).stream().mapToLong(Long::longValue).average().orElse(0) / 1e6;
            int ents = 0; for (Entity e : mc.world.getEntities()) ents++;
            Runtime r = Runtime.getRuntime();
            List<String[]> rows = new ArrayList<>();
            rows.add(new String[]{"Frame", String.format("%.2f ms (%.0f fps)", avg, 1000 / avg)});
            rows.add(new String[]{"1% low", String.format("%.0f fps", 1000 / Math.max(0.01, worst1))});
            rows.add(new String[]{"Entities", String.valueOf(ents)});
            rows.add(new String[]{"Heap", ((r.totalMemory() - r.freeMemory()) >> 20) + "MB"});
            Hud.rows(ctx, mc, this, rows);
            if (graph.b) {
                int gx = x, gy = y + h + 2, i = 0;
                ctx.fill(gx, gy, gx + 100, gy + 24, 0x80000000);
                List<Long> list = new ArrayList<>(frames);
                for (int k = Math.max(0, list.size() - 100); k < list.size(); k++, i++) {
                    double ms = list.get(k) / 1e6; int bh = (int) Math.min(24, ms * 1.2);
                    ctx.fill(gx + i, gy + 24 - bh, gx + i + 1, gy + 24, Hud.gradient((float) (1 - Math.min(1, ms / 33))));
                }
            }
        }
    }
}
