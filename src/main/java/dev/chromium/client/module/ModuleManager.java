package dev.chromium.client.module;

import dev.chromium.client.module.InfoModules.*;
import dev.chromium.client.module.PerfModules.*;
import dev.chromium.client.module.PvpModules.*;
import dev.chromium.client.module.UtilityModules.*;
import dev.chromium.client.module.VisualModules.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public final List<Module> all = new ArrayList<>();
    private boolean started;

    public ModuleManager() {
        // PvP
        all.add(new Keystrokes());      all.add(new CpsCounter());      all.add(new ArmorHud());
        all.add(new PotionEffects());   all.add(new DurabilityHud());   all.add(new TotemCounter());
        all.add(new ItemCounter());     all.add(new AttackIndicator()); all.add(new AttackCooldown());
        all.add(new MaceCooldown());    all.add(new CrosshairCustomizer()); all.add(new HitColor());
        all.add(new HitSound());        all.add(new LowFire());         all.add(new LowShield());
        all.add(new AutoSprint());
        // Info
        all.add(new FpsCounter());      all.add(new PingDisplay());     all.add(new Coordinates());
        all.add(new Direction());       all.add(new Speedometer());     all.add(new SessionTime());
        all.add(new MemoryUsage());     all.add(new Clock());           all.add(new BiomeDisplay());
        all.add(new PlayerCounter());   all.add(new ServerAddress());   all.add(new CustomHudEditor());
        // Performance
all.add(new ParticleOptimization());
all.add(new DynamicFps());
all.add(new WeatherOptimization());
all.add(new FpsLimiter());;
        // Visual
        all.add(new Fullbright());      all.add(new Zoom());            all.add(new WeatherChanger());
        all.add(new NoVignette());      all.add(new NoPumpkinOverlay()); all.add(new NoPortalOverlay());
        all.add(new CustomBlockOutline()); all.add(new CustomSky());
        // Utility
        all.add(new AutoRespawn());     all.add(new ScreenshotManager());
        all.add(new ChatTimestamps());  all.add(new ChatNotifications());
    }

    public <T extends Module> T get(Class<T> c) {
        for (Module m : all) if (c.isInstance(m)) return c.cast(m);
        return null;
    }

    public List<Module> in(Module.Category c) {
        List<Module> l = new ArrayList<>();
        for (Module m : all) if (m.category == c) l.add(m);
        return l;
    }

    /** Runs onEnable for modules enabled by defaults/config (needs a live client). */
    public void tick(MinecraftClient mc) {
        if (!started) {
            started = true;
            for (Module m : all) if (m.enabled) { try { m.onEnable(); } catch (Throwable t) { t.printStackTrace(); } }
        }
        for (Module m : all) if (m.enabled) {
            try { m.onTick(mc); } catch (Throwable t) { t.printStackTrace(); }
        }
    }

    public void renderHud(DrawContext ctx, MinecraftClient mc) {
        for (Module m : all) if (m.enabled && m.hasHud) {
            try { m.onHud(ctx, mc); } catch (Throwable t) { t.printStackTrace(); }
        }
    }
}
