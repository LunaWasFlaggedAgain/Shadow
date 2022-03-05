/*
 * This file is part of the atomic client distribution.
 * Copyright (c) 2021-2021 0x150.
 */

package me.x150.coffee.feature.module.impl.movement;

import me.x150.coffee.feature.module.Module;
import me.x150.coffee.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

public class IgnoreWorldBorder extends Module {

    public IgnoreWorldBorder() {
        super("IgnoreWorldBorder", "Lets you move through the worldborder", ModuleType.MOVEMENT);
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

