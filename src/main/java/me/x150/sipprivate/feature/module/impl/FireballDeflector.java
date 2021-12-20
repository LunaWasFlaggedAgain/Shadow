package me.x150.sipprivate.feature.module.impl;

import me.x150.sipprivate.SipoverPrivate;
import me.x150.sipprivate.feature.config.BooleanSetting;
import me.x150.sipprivate.feature.config.EnumSetting;
import me.x150.sipprivate.feature.module.Module;
import me.x150.sipprivate.feature.module.ModuleType;
import me.x150.sipprivate.helper.Rotations;
import me.x150.sipprivate.helper.render.Renderer;
import me.x150.sipprivate.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class FireballDeflector extends Module {
    public enum Mode {
        ReflectBack,
        DeflectSomewhere
    }
    EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.DeflectSomewhere)
            .name("Mode")
            .description("How to change the fireball's motion (ReflectBack = reflect back at shooter, DeflectSomewhere = idc get it away)")
            .get());
    BooleanSetting checkVel = this.config.create(new BooleanSetting.Builder(false)
            .name("Check velocity")
            .description("Checks if the fireball is actually approaching before hitting. Can get funky with a lot of them")
            .get());
    public FireballDeflector() {
        super("FireballDeflector", "Deflects fireballs in your hit range", ModuleType.MISC);
    }

    boolean isApproaching(Vec3d checkAgainst, Vec3d checkPos, Vec3d checkVel) {
        if (!this.checkVel.getValue()) return true;
        // return true if distance with next velocity to check pos is smaller than current distance -> the fireball is coming closer to us
        return checkAgainst.distanceTo(checkPos) > checkAgainst.distanceTo(checkPos.add(checkVel));
    }

    void hit(FireballEntity fe) {
        Entity owner = fe.getOwner();
        if (owner != null) {
            // we are the owner of this fireball = we shot it = dont hit it again
            if (owner.equals(SipoverPrivate.client.player)) return;
        if (mode.getValue() == Mode.ReflectBack) {
                Vec2f pitchYaw = Rotations.getPitchYawFromOtherEntity(fe.getPos().add(0, fe.getHeight()/2, 0),owner.getPos().add(0,owner.getHeight()/2,0));
                PlayerMoveC2SPacket p = new PlayerMoveC2SPacket.LookAndOnGround(pitchYaw.y, pitchYaw.x, SipoverPrivate.client.player.isOnGround());
                SipoverPrivate.client.getNetworkHandler().sendPacket(p);
        }
        }
        SipoverPrivate.client.interactionManager.attackEntity(SipoverPrivate.client.player, fe);
    }

    boolean inHitRange(Entity attacker, Entity target) {
        return attacker.getCameraPosVec(1f).distanceTo(target.getPos().add(0,target.getHeight()/2,0)) <= SipoverPrivate.client.interactionManager.getReachDistance();
    }

    @Override public void onFastTick() {
        for (Entity entity : SipoverPrivate.client.world.getEntities()) {
            if (entity instanceof FireballEntity fe) {
                if (inHitRange(SipoverPrivate.client.player, fe) && isApproaching(SipoverPrivate.client.player.getPos(), fe.getPos(), fe.getVelocity())) {
                    hit(fe);
                }
            }
        }
    }

    @Override public void tick() {

    }

    @Override public void enable() {

    }

    @Override public void disable() {

    }

    @Override public String getContext() {
        return null;
    }

    @Override public void onWorldRender(MatrixStack matrices) {
        if (isDebuggerEnabled()) {
            for (Entity entity : SipoverPrivate.client.world.getEntities()) {
                if (entity instanceof FireballEntity fe) {
                    if (fe.getOwner() != null) {
                        Entity owner = fe.getOwner();
                        Renderer.R3D.line(Utils.getInterpolatedEntityPosition(owner).add(0, owner.getHeight()/2, 0), Utils.getInterpolatedEntityPosition(fe).add(0, fe.getHeight()/2, 0), Color.MAGENTA,matrices);
                    }
                    if (inHitRange(SipoverPrivate.client.player, fe)) {
                        Renderer.R3D.line(Utils.getInterpolatedEntityPosition(SipoverPrivate.client.player).add(0, SipoverPrivate.client.player.getHeight()/2, 0), Utils.getInterpolatedEntityPosition(fe).add(0, fe.getHeight()/2, 0), Color.RED, matrices);
                    }
                }
            }
        }
    }

    @Override public void onHudRender() {

    }
}
