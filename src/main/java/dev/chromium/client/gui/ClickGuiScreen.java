package dev.chromium.client.gui;

import dev.chromium.client.ChromiumClient;
import dev.chromium.client.module.Module;
import dev.chromium.client.module.Setting;
import dev.chromium.client.util.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/** Custom-drawn ClickGUI. Left click = toggle/adjust, right click = expand settings, drag headers, scroll panels. */
public class ClickGuiScreen extends Screen {
    private static final int ROW = 14, HEAD = 18;

    private static class Panel {
        final String title; final List<Module> mods; final List<Setting> direct;
        int x, y, w = 110; float scroll, maxScroll;
        Panel(String t, List<Module> m, List<Setting> d) { title = t; mods = m; direct = d; }
    }

    private static List<Panel> panels;
    private static final Set<Module> open = new HashSet<>();
    private boolean pl, pr, pk;
    private Panel dragP; private int dox, doy;
    private Setting dragS; private int dsx, dsw;

    public ClickGuiScreen() { super(Text.literal("ChromiumClient")); }

    @Override protected void init() {
        if (panels == null) {
            panels = new ArrayList<>();
            for (Module.Category c : Module.Category.values()) panels.add(new Panel(c.label, ChromiumClient.modules.in(c), null));
            panels.add(new Panel("Client", null, Theme.settings));
            int pw = Math.max(88, Math.min(112, (width - 20) / panels.size() - 6));
            int total = panels.size() * (pw + 6) - 6, sx = Math.max(6, (width - total) / 2);
            for (int i = 0; i < panels.size(); i++) { Panel p = panels.get(i); p.w = pw; p.x = sx + i * (pw + 6); p.y = 44; }
        }
        long h = client.getWindow().getHandle();
        pk = GLFW.glfwGetKey(h, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        pl = GLFW.glfwGetMouseButton(h, 0) == GLFW.GLFW_PRESS; pr = GLFW.glfwGetMouseButton(h, 1) == GLFW.GLFW_PRESS;
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void removed() { Config.save(); }

    private static int settingHeight(Setting s) {
        return switch (s.type) { case NUM -> 17; case COLOR -> 22; default -> 13; };
    }

    private int contentHeight(Panel p) {
        int h = 0;
        if (p.direct != null) for (Setting s : p.direct) h += settingHeight(s);
        else for (Module m : p.mods) {
            h += ROW;
            if (open.contains(m)) for (Setting s : m.settings) h += settingHeight(s);
        }
        return h;
    }

    private static boolean in(int mx, int my, int x, int y, int w, int h, int clipT, int clipB) {
        return mx >= x && mx < x + w && my >= y && my < y + h && my >= clipT && my < clipB;
    }

    @Override public void render(DrawContext ctx, int mx, int my, float delta) {
        long hnd = client.getWindow().getHandle();
        boolean l = GLFW.glfwGetMouseButton(hnd, 0) == GLFW.GLFW_PRESS, r = GLFW.glfwGetMouseButton(hnd, 1) == GLFW.GLFW_PRESS;
        boolean k = GLFW.glfwGetKey(hnd, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        boolean lc = l && !pl, rc = r && !pr;
        if (k && !pk) { client.setScreen(null); return; }

        ctx.fill(0, 0, width, height, 0xAA05070B);
        Logo.draw(ctx, textRenderer, 8, 6, 32);
        ctx.drawText(textRenderer, "ChromiumClient", 46, 11, Theme.accent(), true);
        ctx.drawText(textRenderer, "Left click: toggle  |  Right click: settings  |  Drag headers  |  Scroll panels", 46, 24, 0xFF8890A0, false);

        // HUD editor button
        String bt = "Edit HUD Layout";
        int bw = textRenderer.getWidth(bt) + 14, bx = width - bw - 8;
        boolean bh = in(mx, my, bx, 8, bw, 16, 0, height);
        ctx.fill(bx, 8, bx + bw, 24, bh ? Theme.withAlpha(Theme.accent(), 0xFF) : 0xFF1B2030);
        ctx.drawText(textRenderer, bt, bx + 7, 12, bh ? 0xFF000000 : 0xFFFFFFFF, false);
        if (bh && lc) { client.setScreen(new HudEditorScreen()); return; }

        for (Panel p : panels) drawPanel(ctx, p, mx, my, lc, rc);

        if (dragP != null) { if (l) { dragP.x = mx - dox; dragP.y = Math.max(0, my - doy); } else dragP = null; }
        if (dragS != null) {
            if (l) dragS.setNum(dragS.min + Math.max(0, Math.min(1, (mx - dsx) / (double) dsw)) * (dragS.max - dragS.min));
            else dragS = null;
        }
        pl = l; pr = r; pk = k;
    }

    private void drawPanel(DrawContext ctx, Panel p, int mx, int my, boolean lc, boolean rc) {
        int acc = Theme.accent();
        int bodyTop = p.y + HEAD, content = contentHeight(p);
        int maxH = Math.max(40, height - bodyTop - 8), vis = Math.min(content, maxH);
        p.maxScroll = Math.max(0, content - vis);
        p.scroll = Math.max(0, Math.min(p.maxScroll, p.scroll));

        ctx.fill(p.x - 1, p.y - 1, p.x + p.w + 1, bodyTop + vis + 1, 0xFF000000);
        ctx.fill(p.x, p.y, p.x + p.w, p.y + HEAD, 0xFF161B28);
        ctx.fill(p.x, p.y + HEAD - 2, p.x + p.w, p.y + HEAD, acc);
        ctx.drawText(textRenderer, p.title, p.x + 6, p.y + 5, 0xFFFFFFFF, true);
        if (in(mx, my, p.x, p.y, p.w, HEAD, 0, height) && dragP == null && dragS == null && lc) { dragP = p; dox = mx - p.x; doy = my - p.y; }

        ctx.fill(p.x, bodyTop, p.x + p.w, bodyTop + vis, 0xF0101420);
        ctx.enableScissor(p.x, bodyTop, p.x + p.w, bodyTop + vis);
        int cy = bodyTop - (int) p.scroll, clipT = bodyTop, clipB = bodyTop + vis;

        if (p.direct != null) {
            for (Setting s : p.direct) cy = drawSetting(ctx, s, p.x + 4, cy, p.w - 8, mx, my, lc, clipT, clipB);
        } else {
            for (Module m : p.mods) {
                boolean hov = in(mx, my, p.x, cy, p.w, ROW, clipT, clipB);
                int bg = m.enabled ? Theme.withAlpha(acc, 0x55) : (hov ? 0xFF1C2233 : 0x00000000);
                if (bg != 0) ctx.fill(p.x, cy, p.x + p.w, cy + ROW, bg);
                if (m.enabled) ctx.fill(p.x, cy, p.x + 2, cy + ROW, acc);
                ctx.drawText(textRenderer, m.name, p.x + 6, cy + 3, m.enabled ? 0xFFFFFFFF : 0xFFA0A8B8, false);
                if (!m.settings.isEmpty()) ctx.drawText(textRenderer, open.contains(m) ? "-" : "+", p.x + p.w - 10, cy + 3, 0xFF707890, false);
                if (hov && lc) m.toggle();
                if (hov && rc && !m.settings.isEmpty()) { if (!open.remove(m)) open.add(m); }
                cy += ROW;
                if (open.contains(m)) {
                    ctx.fill(p.x, cy, p.x + p.w, cy + settings(m), 0x40000000);
                    for (Setting s : m.settings) cy = drawSetting(ctx, s, p.x + 6, cy, p.w - 12, mx, my, lc, clipT, clipB);
                }
            }
        }
        ctx.disableScissor();
        // hover description
        for (Module m : p.mods == null ? List.<Module>of() : p.mods) { /* descriptions drawn below */ }
    }

    private int settings(Module m) { int h = 0; for (Setting s : m.settings) h += settingHeight(s); return h; }

    private int drawSetting(DrawContext ctx, Setting s, int x, int y, int w, int mx, int my, boolean lc, int clipT, int clipB) {
        int hgt = settingHeight(s), acc = Theme.accent();
        boolean hov = in(mx, my, x, y, w, hgt, clipT, clipB);
        switch (s.type) {
            case BOOL -> {
                ctx.drawText(textRenderer, s.name, x, y + 2, 0xFFC0C8D8, false);
                int bx = x + w - 18; ctx.fill(bx, y + 2, bx + 16, y + 10, s.b ? acc : 0xFF303648);
                ctx.fill(s.b ? bx + 9 : bx + 1, y + 3, s.b ? bx + 15 : bx + 7, y + 9, 0xFFFFFFFF);
                if (hov && lc) s.b = !s.b;
            }
            case MODE -> {
                ctx.drawText(textRenderer, s.name + ":", x, y + 2, 0xFFC0C8D8, false);
                String m = s.modeName();
                ctx.drawText(textRenderer, m, x + w - textRenderer.getWidth(m), y + 2, acc, false);
                if (hov && lc) s.cycle();
            }
            case NUM -> {
                String v = s.step >= 1 ? String.valueOf(s.i()) : String.format("%.2f", s.n);
                ctx.drawText(textRenderer, s.name, x, y + 1, 0xFFC0C8D8, false);
                ctx.drawText(textRenderer, v, x + w - textRenderer.getWidth(v), y + 1, 0xFFFFFFFF, false);
                ctx.fill(x, y + 11, x + w, y + 14, 0xFF303648);
                int fw = (int) (w * (s.n - s.min) / (s.max - s.min));
                ctx.fill(x, y + 11, x + fw, y + 14, acc);
                ctx.fill(x + fw - 1, y + 9, x + fw + 1, y + 16, 0xFFFFFFFF);
                if (hov && lc && dragS == null) { dragS = s; dsx = x; dsw = w; }
            }
            case COLOR -> {
                ctx.drawText(textRenderer, s.name, x, y + 1, 0xFFC0C8D8, false);
                int sw = Math.min(11, (w - 2) / Setting.PALETTE.length);
                for (int i = 0; i < Setting.PALETTE.length; i++) {
                    int sx = x + i * (sw + 1), sy = y + 11;
                    int c = Setting.PALETTE[i];
                    ctx.fill(sx - (s.color == c ? 1 : 0), sy - (s.color == c ? 1 : 0), sx + sw + (s.color == c ? 1 : 0), sy + 9 + (s.color == c ? 1 : 0), s.color == c ? 0xFFFFFFFF : 0xFF000000);
                    ctx.fill(sx, sy, sx + sw, sy + 8, c);
                    if (lc && in(mx, my, sx, sy, sw, 8, clipT, clipB)) s.color = c;
                }
            }
        }
        return y + hgt;
    }

    @Override public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        for (Panel p : panels)
            if (mx >= p.x && mx < p.x + p.w && my >= p.y) { p.scroll -= (float) (vAmount * 14); return true; }
        return false;
    }
}
