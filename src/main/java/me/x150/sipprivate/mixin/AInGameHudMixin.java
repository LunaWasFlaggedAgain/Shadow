package me.x150.sipprivate.mixin;

import me.x150.sipprivate.feature.gui.notifications.NotificationRenderer;
import me.x150.sipprivate.feature.module.Module;
import me.x150.sipprivate.feature.module.ModuleRegistry;
import me.x150.sipprivate.util.Utils;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class) public abstract class AInGameHudMixin extends DrawableHelper {
    @Inject(method = "render", at = @At("RETURN")) public void atomic_postRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        for (Module module : ModuleRegistry.getModules()) {
            if (module.isEnabled()) {
                module.onHudRender();
            }
        }
        NotificationRenderer.render();
        Utils.TickManager.render();
    }
}