/*
 * This file is part of the atomic client distribution.
 * Copyright (c) 2021-2021 0x150.
 */

package me.x150.coffee.feature.module.impl.movement;

import me.x150.coffee.feature.config.EnumSetting;
import me.x150.coffee.feature.module.Module;
import me.x150.coffee.feature.module.ModuleType;
import me.x150.coffee.helper.event.EventType;
import me.x150.coffee.helper.event.Events;
import me.x150.coffee.helper.event.events.PacketEvent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;

import java.util.ArrayList;
import java.util.List;

public class Blink extends Module {

    final List<Packet<?>> queue = new ArrayList<>();
    final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.Delay)
            .name("Mode")
            .description("Whether to delay or remove the packets being sent")
            .get());

    public Blink() {
        super("Blink", "Delay or cancel outgoing packets", ModuleType.MOVEMENT);
        Events.registerEventHandler(EventType.PACKET_SEND, event1 -> {
            if (!this.isEnabled()) {
                return;
            }
            if (client.player == null || client.world == null) {
                setEnabled(false);
                return;
            }
            PacketEvent event = (PacketEvent) event1;
            if (event.getPacket() instanceof KeepAliveC2SPacket) {
                return;
            }
            event.setCancelled(true);
            if (mode.getValue() == Mode.Delay) {
                queue.add(event.getPacket());
            }
        });
    }

    @Override
    public void tick() {

    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {
        if (client.player == null || client.getNetworkHandler() == null) {
            queue.clear();
            return;
        }
        for (Packet<?> packet : queue.toArray(new Packet<?>[0])) {
            client.getNetworkHandler().sendPacket(packet);
        }
        queue.clear();
    }

    @Override
    public String getContext() {
        return queue.size() + "";
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }

    //    final MultiValue      mode  = (MultiValue) this.config.create("Mode", "delay", "delay", "drop").description("Whether or not to delay or drop the packets");
    public enum Mode {
        Delay, Drop
    }
}