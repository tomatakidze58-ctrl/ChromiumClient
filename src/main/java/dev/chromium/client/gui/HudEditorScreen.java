package dev.chromium.client.gui;

import dev.chromium.client.ChromiumClient;
import dev.chromium.client.module.InfoModules.CustomHudEditor;
import dev.chromium.client.module.Module;
import dev.chromium.client.util.Config;
import dev.chromium.client.util.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Drag HUD modules around. Left drag = move, right click = reset position. ESC = save & close. */
public class HudEditorScreen extends Screen {
    private Module drag; private int ox, oy;
    private boolean pl, pr, pk;

    public HudEditorScreen() { super(Text.literal("HUD Editor")); }

    @Override protected void init() {
        long h = client.getWindow().getHandle();
        pl = GLFW.glfwGetMouseButton(h, 0) == GLFW.GLFW_PRESS; pr = GLFW.glfwGetMouseButton(h, 1) == GLFW.GLFW_PRESS;
        pk = GLFW.glfwGetKey(h, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }
    @Override public boolean shouldPause() { return false; }
    @Override public void removed() { Config.save(); }

    private Module under(int mx, int my) {
        var list = ChromiumClient.modules.all;
        for (int i = list.size() - 1; i >= 0; i--) {
            Module m = list.get(i);
            if (m.enabled && m.editable && mx >= m.x - 2 && mx <= m.x + m.w + 2 && my >= m.y - 2 && my <= m.y + m.h + 2) return m;
        }
        return null;
    }

    @Override public void render(DrawContext ctx, int mx, int my, float delta) {
        long hnd = client.getWindow().getHandle();
        boolean l = GLFW.glfwGetMouseButton(hnd, 0) == GLFW.GLFW_PRESS, r = GLFW.glfwGetMouseButton(hnd, 1) == GLFW.GLFW_PRESS;
        boolean k = GLFW.glfwGetKey(hnd, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        boolean lc = l && !pl, rc = r && !pr;
        if (k && !pk) { client.setScreen(null); return; }

        ctx.fill(0, 0, width, height, 0x66000000);
        if (CustomHudEditor.SNAP.b) {
            int g = Math.max(4, CustomHudEditor.GRID.i() * 4);
            for (int x = 0; x < width; x += g) ctx.fill(x, 0, x + 1, height, 0x14FFFFFF);
            for (int y = 0; y < height; y += g) ctx.fill(0, y, width, y + 1, 0x14FFFFFF);
        }
        if (CustomHudEditor.GUIDES.b) {
            ctx.fill(width / 2, 0, width / 2 + 1, height, 0x55FF5555);
            ctx.fill(0, height / 2, width, height / 2 + 1, 0x55FF5555);
        }
        ctx.drawCenteredTextWithShadow(textRenderer, "HUD Editor", width / 2, 8, Theme.accent());
        ctx.drawCenteredTextWithShadow(textRenderer, "Drag to move  |  Right-click to reset  |  ESC to save", width / 2, 20, 0xFFAAAAAA);

        Module hover = under(mx, my);
        if (lc && hover != null) { drag = hover; ox = mx - hover.x; oy = my - hover.y; }
        if (rc && hover != null) { hover.x = hover.defX; hover.y = hover.defY; }
        if (drag != null) {
            if (l) {
                drag.x = Math.max(0, Math.min(width - drag.w, CustomHudEditor.snap(mx - ox)));
                drag.y = Math.max(0, Math.min(height - drag.h, CustomHudEditor.snap(my - oy)));
            } else { drag = null; Config.save(); }
        }
        for (Module m : ChromiumClient.modules.all) {
            if (!m.enabled || !m.editable) continue;
            int c = m == drag ? Theme.accent() : (m == hover ? 0xFFFFFFFF : 0x99FFFFFF);
            ctx.fill(m.x - 2, m.y - 2, m.x + m.w + 2, m.y - 1, c); ctx.fill(m.x - 2, m.y + m.h + 1, m.x + m.w + 2, m.y + m.h + 2, c);
            ctx.fill(m.x - 2, m.y - 2, m.x - 1, m.y + m.h + 2, c); ctx.fill(m.x + m.w + 1, m.y - 2, m.x + m.w + 2, m.y + m.h + 2, c);
            if (m == hover || m == drag) ctx.drawText(textRenderer, m.name, m.x, m.y - 12, Theme.accent(), true);
        }
        pl = l; pr = r; pk = k;
    }
}
