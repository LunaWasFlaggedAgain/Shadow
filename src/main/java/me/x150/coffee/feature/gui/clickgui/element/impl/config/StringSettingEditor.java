package me.x150.coffee.feature.gui.clickgui.element.impl.config;

import me.x150.coffee.feature.config.StringSetting;
import me.x150.coffee.feature.gui.widget.RoundTextFieldWidget;
import me.x150.coffee.helper.font.FontRenderers;
import net.minecraft.client.util.math.MatrixStack;

public class StringSettingEditor extends ConfigBase<StringSetting> {
    final RoundTextFieldWidget input;

    public StringSettingEditor(double x, double y, double width, StringSetting configValue) {
        super(x, y, width, 0, configValue);
        double h = FontRenderers.getRenderer().getFontHeight() + 2;
        input = new RoundTextFieldWidget(x, y, width, h, configValue.getName());
        input.changeListener = () -> configValue.setValue(input.get());
        input.setText(configValue.getValue());
        this.height = h + FontRenderers.getRenderer().getMarginHeight() + 1;
    }

    @Override public boolean clicked(double x, double y, int button) {
        return input.mouseClicked(x, y, button);
    }

    @Override public boolean dragged(double x, double y, double deltaX, double deltaY, int button) {
        return false;
    }

    @Override public boolean released() {
        return false;
    }

    @Override public boolean keyPressed(int keycode, int modifiers) {
        return input.keyPressed(keycode, 0, modifiers);
    }

    @Override public void render(MatrixStack matrices, double mouseX, double mouseY, double scrollBeingUsed) {
        FontRenderers.getRenderer().drawString(matrices, configValue.getName(), x, y, 0xFFFFFF);
        input.setX(x);
        input.setY(y + FontRenderers.getRenderer().getFontHeight());
        input.render(matrices, (int) mouseX, (int) mouseY, 0);
    }

    @Override public void tickAnim() {

    }

    @Override public boolean charTyped(char c, int mods) {
        return input.charTyped(c, mods);
    }
}
