package dev.chromium.client.util;

import dev.chromium.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import java.util.List;

public final class Hud {
    private Hud() {}

    /** Draws background (if enabled) and records the module's size for the HUD editor. */
    public static void bg(DrawContext ctx, Module m, int w, int h) {
        if (Theme.hudBg.b) ctx.fill(m.x, m.y, m.x + w, m.y + h, Theme.bgAlpha.i() << 24);
        m.w = w; m.h = h;
    }

    public static void one(DrawContext ctx, MinecraftClient mc, Module m, String label, String value) {
        rows(ctx, mc, m, List.<String[]>of(new String[]{label, value}));
    }

    public static void rows(DrawContext ctx, MinecraftClient mc, Module m, List<String[]> rows) {
        TextRenderer tr = mc.textRenderer;
        int pad = 3, lh = 10, w = 0;
        for (String[] r : rows) w = Math.max(w, tr.getWidth(r[0] == null ? "" : r[0] + ": ") + tr.getWidth(r[1]));
        bg(ctx, m, w + pad * 2, rows.size() * lh + pad * 2 - 1);
        int y = m.y + pad;
        for (String[] r : rows) {
            int x = m.x + pad;
            if (r[0] != null) {
                String l = r[0] + ": ";
                ctx.drawText(tr, l, x, y, Theme.accent(), Theme.shadow.b);
                x += tr.getWidth(l);
            }
            ctx.drawText(tr, r[1], x, y, 0xFFFFFFFF, Theme.shadow.b);
            y += lh;
        }
    }

    public static int gradient(float pct) { // 1 = green, 0 = red
        return 0xFF000000 | net.minecraft.util.math.MathHelper.hsvToRgb(Math.max(0, Math.min(1, pct)) * 0.33f, 0.9f, 1f);
    }
}
