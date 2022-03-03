package me.x150.coffee.feature.gui.clickgui.element.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.x150.coffee.feature.gui.clickgui.ClickGUI;
import me.x150.coffee.feature.gui.clickgui.element.Element;
import me.x150.coffee.feature.gui.clickgui.theme.Theme;
import me.x150.coffee.feature.module.Module;
import me.x150.coffee.feature.module.ModuleRegistry;
import me.x150.coffee.feature.module.ModuleType;
import me.x150.coffee.helper.font.FontRenderers;
import me.x150.coffee.helper.font.adapter.impl.ClientFontRenderer;
import me.x150.coffee.helper.render.ClipStack;
import me.x150.coffee.helper.render.Rectangle;
import me.x150.coffee.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryDisplay extends Element {
    static final ClientFontRenderer  cfr = FontRenderers.getCustomSize(20);
    final        List<ModuleDisplay> md  = new ArrayList<>();
    final        ModuleType          mt;
    boolean selected = false;

    public CategoryDisplay(double x, double y, ModuleType mt) {
        super(x, y, 100, 500);
        this.mt = mt;
        for (Module module : ModuleRegistry.getModules()) {
            if (module.getModuleType() == mt) {
                ModuleDisplay md1 = new ModuleDisplay(0, 0, module);
                md.add(md1);
            }
        }
    }

    @Override public boolean clicked(double x, double y, int button) {
        if (x >= this.x && x < this.x + this.width && y >= this.y && y < this.y + headerHeight()) {
            if (button == 0) {
                selected = true;
                return true;
            }
        } else {
            for (ModuleDisplay moduleDisplay : getModules()) {
                if (moduleDisplay.clicked(x, y, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override public boolean dragged(double x, double y, double deltaX, double deltaY, int button) {
        if (selected) {
            this.x += deltaX;
            this.y += deltaY;
            return true;
        } else {
            for (ModuleDisplay moduleDisplay : getModules()) {
                if (moduleDisplay.dragged(x, y, deltaX, deltaY, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override public boolean released() {
        selected = false;
        for (ModuleDisplay moduleDisplay : getModules()) {
            moduleDisplay.released();
        }
        return false;
    }

    @Override public boolean keyPressed(int keycode, int modifiers) {
        for (ModuleDisplay moduleDisplay : getModules()) {
            if (moduleDisplay.keyPressed(keycode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    List<ModuleDisplay> getModules() {
        return md.stream().filter(moduleDisplay -> moduleDisplay.module.getName().toLowerCase().startsWith(ClickGUI.instance().searchTerm.toLowerCase())).collect(Collectors.toList());
    }

    float headerHeight() {
        float padding = 3;
        return padding + cfr.getFontHeight() + padding;
    }

    @Override public void render(MatrixStack matrices, double mouseX, double mouseY, double scrollBeingUsed) {
        Theme theme = ClickGUI.theme;
        //        Renderer.R2D.fill(matrices, theme.getHeader(), x, y, x + width, y + headerHeight());
        double r = 5;
        double hheight = headerHeight();
        double texPad = 4;
        double texDim = hheight - texPad * 2;

        double modHeight = getModules().stream().map(ModuleDisplay::getHeight).reduce(Double::sum).orElse(0d);
        modHeight = Math.min(modHeight, 100);
        this.height = headerHeight() + modHeight; // pre calc height
        if (modHeight != 0) {
            height += r;
        }
        ClipStack.globalInstance.addWindow(matrices, new Rectangle(x, y, x + width, y + this.height));
        Renderer.R2D.renderRoundedQuad(matrices, theme.getHeader(), x, y, x + width, y + this.height, r, 20);
        RenderSystem.setShaderTexture(0, mt.getTex());
        Renderer.R2D.renderTexture(matrices, x + texPad, y + texPad, texDim, texDim, 0, 0, texDim, texDim, texDim, texDim);
        //        cfr.drawCenteredString(matrices,mt.getName(),x+texPad+texDim+texPad,y+headerHeight()/2d-cfr.getFontHeight()/2d,0xFFFFFF);
        cfr.drawCenteredString(matrices, mt.getName(), x + width / 2d, y + headerHeight() / 2d - cfr.getFontHeight() / 2d, 0xFFFFFF);
        double y = headerHeight();
        for (ModuleDisplay moduleDisplay : getModules()) {
            moduleDisplay.setX(this.x);
            moduleDisplay.setY(this.y + y);
            moduleDisplay.render(matrices, mouseX, mouseY, scrollBeingUsed);
            y += moduleDisplay.getHeight();
        }
        ClipStack.globalInstance.popWindow(matrices);
        //        FontRenderers.getRenderer().drawCenteredString(matrices, getModules().size() + " modules", this.x + this.width / 2d, this.y + this.height - 1 - FontRenderers.getRenderer().getMarginHeight(), 0xFFFFFF);
    }

    @Override public void tickAnim() {
        for (ModuleDisplay moduleDisplay : getModules()) {
            moduleDisplay.tickAnim();
        }
    }

    @Override public boolean charTyped(char c, int mods) {
        for (ModuleDisplay moduleDisplay : getModules()) {
            if (moduleDisplay.charTyped(c, mods)) {
                return true;
            }
        }
        return false;
    }
}
