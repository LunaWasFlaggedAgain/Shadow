/*
 * This file is part of the atomic client distribution.
 * Copyright (c) 2021-2021 0x150.
 */

package me.x150.coffee.feature.module.impl.world;

import me.x150.coffee.feature.module.Module;
import me.x150.coffee.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

public class NoBreakDelay extends Module {

    public NoBreakDelay() {
        super("NoBreakDelay", "Removes the break delay", ModuleType.WORLD);
    }

    @Override
    public void tick() {

    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }
}

