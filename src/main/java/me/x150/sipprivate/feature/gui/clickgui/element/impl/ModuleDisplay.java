package me.x150.sipprivate.feature.gui.clickgui.element.impl;

import me.x150.sipprivate.feature.gui.clickgui.ClickGUI;
import me.x150.sipprivate.feature.gui.clickgui.element.Element;
import me.x150.sipprivate.feature.gui.clickgui.theme.Theme;
import me.x150.sipprivate.feature.module.Module;
import me.x150.sipprivate.helper.font.FontRenderers;
import me.x150.sipprivate.helper.render.Renderer;
import me.x150.sipprivate.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class ModuleDisplay extends Element {
    Module        module;
    ConfigDisplay cd;
    boolean       extended = false;
    double extendAnim = 0;
    public ModuleDisplay(double x, double y, Module module) {
        super(x, y, 100, 15);
        this.module = module;
        this.cd = new ConfigDisplay(x, y, module.config);
    }

    @Override public boolean clicked(double x, double y, int button) {
        if (inBounds(x, y)) {
            if (button == 0) {
                module.setEnabled(!module.isEnabled()); // left click
            } else if (button == 1) {
                extended = !extended;
            } else {
                return false;
            }
            return true;
        } else {
            return extended && cd.clicked(x, y, button);
        }
    }

    @Override public boolean dragged(double x, double y, double deltaX, double deltaY) {
        return extended && cd.dragged(x, y, deltaX, deltaY);
    }

    @Override public boolean released() {
        return extended && cd.released();
    }
    double easeInOutCubic(double x) {
        return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;

    }
    @Override public double getHeight() {
        return super.getHeight() + cd.getHeight()*easeInOutCubic(extendAnim);
    }

    @Override public boolean keyPressed(int keycode) {
        return extended && cd.keyPressed(keycode);
    }

    @Override public void render(MatrixStack matrices) {
        Theme theme = ClickGUI.theme;
        boolean hovered = inBounds(Utils.Mouse.getMouseX(), Utils.Mouse.getMouseY());
        Renderer.R2D.fill(matrices, hovered ? theme.getModule().darker() : theme.getModule(), x, y, x + width, y + height);
        FontRenderers.getNormal().drawCenteredString(matrices, module.getName(), x + width / 2d, y + height / 2d - FontRenderers.getNormal().getMarginHeight() / 2d, 0xFFFFFF);
        if (module.isEnabled()) {
            Renderer.R2D.fill(matrices, theme.getAccent(), x, y, x + 1, y + height);
        }
        cd.setX(this.x);
        cd.setY(this.y + height);
        Renderer.R2D.scissor(x,y,width,getHeight());
        cd.render(matrices);
        Renderer.R2D.unscissor();
    }

    @Override public void tickAnim() {
        double a = 0.04;
        if (!extended) a *= -1;
        extendAnim += a;
        extendAnim = MathHelper.clamp(extendAnim, 0, 1);
        cd.tickAnim();
    }
}
