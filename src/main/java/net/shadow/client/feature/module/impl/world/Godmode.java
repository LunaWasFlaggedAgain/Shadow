/*
 * Copyright (c) Shadow client, 0x150, Saturn5VFive 2022. All rights reserved.
 */

package net.shadow.client.feature.module.impl.world;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.shadow.client.feature.module.ModuleType;
import net.shadow.client.helper.event.EventListener;
import net.shadow.client.helper.event.EventType;
import net.shadow.client.helper.event.events.PacketEvent;
import net.shadow.client.feature.config.EnumSetting;
import net.shadow.client.feature.module.Module;
import net.minecraft.client.util.math.MatrixStack;

public class Godmode extends Module {

    int ticks;
    final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.Vanilla).name("Mode").description("The mode to get god in").get());

    public Godmode() {
        super("Godmode", "God mods", ModuleType.WORLD);
    }

    @EventListener(type=EventType.PACKET_SEND)
    void giveAShit(PacketEvent event){
        if(event.getPacket() instanceof ClientStatusC2SPacket packet){
            if(packet.getMode() == ClientStatusC2SPacket.Mode.PERFORM_RESPAWN){
                event.setCancelled(true);
                client.setScreen(null);
                client.currentScreen = null;
                client.player.setHealth(20F);
            }
        }
    }

    @Override
    public void tick() {
        if(mode.getValue() == Mode.Matrix){
            ticks++;
            if (ticks % 10 == 0) {
                for (int i = 0; i < 2; i++) {
                    client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(client.player.getX(), client.player.getY() + 5, client.player.getZ(), true));
                }
            }
        }
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

    public enum Mode {
        Vanilla,
        Matrix
    }
}