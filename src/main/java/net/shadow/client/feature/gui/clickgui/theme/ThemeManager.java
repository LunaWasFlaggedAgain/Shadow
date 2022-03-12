package net.shadow.client.feature.gui.clickgui.theme;

import net.shadow.client.feature.gui.clickgui.theme.impl.Custom;
import net.shadow.client.feature.gui.clickgui.theme.impl.Coffee;
import net.shadow.client.feature.gui.clickgui.theme.impl.Shadow;
import net.shadow.client.feature.module.ModuleRegistry;

public class ThemeManager {
    static net.shadow.client.feature.module.impl.render.Theme t = ModuleRegistry.getByClass(net.shadow.client.feature.module.impl.render.Theme.class);
    static Theme custom = new Custom();
    static Theme shadow = new Shadow();
    static Theme bestThemeEver = new Coffee();

    public static Theme getMainTheme() {
        return switch (t.modeSetting.getValue()) {
            case Coffee -> bestThemeEver;
            case Custom -> custom;
            case Shadow -> shadow;
        };
    }
}