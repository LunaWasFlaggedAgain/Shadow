/*
 * This file is part of the atomic client distribution.
 * Copyright (c) 2021-2021 0x150.
 */

package me.x150.coffee.mixin;

import me.x150.coffee.CoffeeClientMain;
import me.x150.coffee.helper.Rotations;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntityRenderer.class) public class LivingEntityRendererMixin {

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 2,
            at = @At(value = "STORE", ordinal = 0)) public float atomic_overwriteYaw(float oldValue, LivingEntity le) {
        if (Rotations.isEnabled() && le.equals(CoffeeClientMain.client.player)) {
            return Rotations.getClientYaw();
        }
        return oldValue;
    }

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 3,
            at = @At(value = "STORE", ordinal = 0)) public float atomic_overwriteHeadYaw(float oldValue, LivingEntity le) {
        if (le.equals(CoffeeClientMain.client.player) && Rotations.isEnabled()) {
            return Rotations.getClientYaw();
        }
        return oldValue;
    }

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 5,
            at = @At(value = "STORE", ordinal = 3)) public float atomic_overwritePitch(float oldValue, LivingEntity le) {
        if (le.equals(CoffeeClientMain.client.player) && Rotations.isEnabled()) {
            return Rotations.getClientPitch();
        }
        return oldValue;
    }

}
