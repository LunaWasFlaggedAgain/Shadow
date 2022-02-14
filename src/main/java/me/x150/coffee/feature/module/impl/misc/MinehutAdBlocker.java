package me.x150.coffee.feature.module.impl.misc;

import me.x150.coffee.feature.module.Module;
import me.x150.coffee.feature.module.ModuleType;
import me.x150.coffee.helper.event.EventType;
import me.x150.coffee.helper.event.Events;
import me.x150.coffee.helper.event.events.PacketEvent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public class MinehutAdBlocker extends Module {
    long blocked = 0;

    public MinehutAdBlocker() {
        super("MinehutAdBlocker", "Blocks the /ad command on minehut from sending to you", ModuleType.MISC);
        Events.registerEventHandler(EventType.PACKET_RECEIVE, event -> {
            if (!this.isEnabled()) return;
            PacketEvent pe = (PacketEvent) event;
            if (pe.getPacket() instanceof GameMessageS2CPacket msg) {
                if (msg.getMessage().getString().contains("[AD]")) {
                    event.setCancelled(true);
                    blocked++;
                }
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

    }

    @Override
    public String getContext() {
        return blocked + "";
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }
}