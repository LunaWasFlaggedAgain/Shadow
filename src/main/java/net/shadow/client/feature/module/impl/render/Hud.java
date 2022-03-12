package net.shadow.client.feature.module.impl.render;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.shadow.client.ShadowMain;
import net.shadow.client.feature.config.BooleanSetting;
import net.shadow.client.feature.gui.clickgui.theme.ThemeManager;
import net.shadow.client.feature.gui.hud.HudRenderer;
import net.shadow.client.feature.gui.notifications.Notification;
import net.shadow.client.feature.gui.notifications.NotificationRenderer;
import net.shadow.client.feature.module.Module;
import net.shadow.client.feature.module.ModuleRegistry;
import net.shadow.client.feature.module.ModuleType;
import net.shadow.client.helper.Texture;
import net.shadow.client.helper.Timer;
import net.shadow.client.helper.event.EventType;
import net.shadow.client.helper.event.Events;
import net.shadow.client.helper.event.events.PacketEvent;
import net.shadow.client.helper.font.FontRenderers;
import net.shadow.client.helper.font.adapter.impl.ClientFontRenderer;
import net.shadow.client.helper.render.Renderer;
import net.shadow.client.helper.util.Transitions;
import net.shadow.client.helper.util.Utils;
import net.shadow.client.mixin.IDebugHudAccessor;
import net.shadow.client.mixin.IInGameHudAccessor;
import net.shadow.client.mixin.IMinecraftClientAccessor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;

public class Hud extends Module {
    public static double currentTps = 0;
    public static double real = 0;
    static ClientFontRenderer titleFr;
    final Identifier LOGO = new Texture("logo.png");
    public final BooleanSetting speed = this.config.create(new BooleanSetting.Builder(true).name("Speed").description("Show your current velocity").get());
    final DateFormat minSec = new SimpleDateFormat("mm:ss");
    final BooleanSetting fps = this.config.create(new BooleanSetting.Builder(true).name("FPS").description("Whether to show FPS").get());
    final BooleanSetting tps = this.config.create(new BooleanSetting.Builder(true).name("TPS").description("Whether to show TPS").get());
    final BooleanSetting coords = this.config.create(new BooleanSetting.Builder(true).name("Coordinates").description("Whether to show current coordinates").get());
    final BooleanSetting ping = this.config.create(new BooleanSetting.Builder(true).name("Ping").description("Whether to show current ping").get());
    final BooleanSetting modules = this.config.create(new BooleanSetting.Builder(true).name("Array list").description("Whether to show currently enabled modules").get());
    final List<ModuleEntry> moduleList = new ArrayList<>();
    final Timer tpsUpdateTimer = new Timer();
    final List<Double> last5SecondTpsAverage = new ArrayList<>();
    long lastTimePacketReceived;
    double rNoConnectionPosY = -10d;
    Notification serverNotResponding = null;

    public Hud() {
        super("Hud", "Shows information about the player on screen", ModuleType.RENDER);
        lastTimePacketReceived = System.currentTimeMillis();

        Events.registerEventHandler(EventType.PACKET_RECEIVE, event1 -> {
            PacketEvent event = (PacketEvent) event1;
            if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
                lastTimePacketReceived = System.currentTimeMillis();
            }
        });
    }

    double calcTps(double n) {
        return (20.0 / Math.max((n - 1000.0) / (500.0), 1.0));
    }

    @Override
    public void tick() {
        long averageTime = 5000;
        long waitTime = 100;
        long maxLength = averageTime / waitTime;
        if (tpsUpdateTimer.hasExpired(waitTime)) {
            tpsUpdateTimer.reset();
            double newTps = calcTps(System.currentTimeMillis() - lastTimePacketReceived);
            last5SecondTpsAverage.add(newTps);
            while (last5SecondTpsAverage.size() > maxLength) {
                last5SecondTpsAverage.remove(0);
            }
            currentTps = Utils.Math.roundToDecimal(last5SecondTpsAverage.stream().reduce(Double::sum).orElse(0d) / last5SecondTpsAverage.size(), 2);

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
    public void onHudRenderNoMSAA() {
        if (ShadowMain.client.getNetworkHandler() == null) {
            return;
        }
        if (ShadowMain.client.player == null) {
            return;
        }
        MatrixStack ms = Renderer.R3D.getEmptyMatrixStack();
        double heightOffsetLeft = 0, heightOffsetRight = 0;
        if (ShadowMain.client.options.debugEnabled) {
            double heightAccordingToMc = 9;
            List<String> lt = ((IDebugHudAccessor) ((IInGameHudAccessor) ShadowMain.client.inGameHud).getDebugHud()).callGetLeftText();
            List<String> rt = ((IDebugHudAccessor) ((IInGameHudAccessor) ShadowMain.client.inGameHud).getDebugHud()).callGetRightText();
            heightOffsetLeft = 2 + heightAccordingToMc * (lt.size() + 3);
            heightOffsetRight = 2 + heightAccordingToMc * rt.size() + 5;
        }
        if (!shouldNoConnectionDropDown()) {
            if (serverNotResponding != null) {
                serverNotResponding.duration = 0;
            }
        } else {
            if (serverNotResponding == null) {
                serverNotResponding = Notification.create(-1, "", true, Notification.Type.INFO, "Server not responding! " + minSec.format(System.currentTimeMillis() - lastTimePacketReceived));
            }
            serverNotResponding.contents = new String[]{"Server not responding! " + minSec.format(System.currentTimeMillis() - lastTimePacketReceived)};
        }
        if (!NotificationRenderer.topBarNotifications.contains(serverNotResponding)) {
            serverNotResponding = null;
        }
        makeSureIsInitialized();

        if (modules.getValue()) {
            ms.push();
            ms.translate(0, heightOffsetRight, 0);
            drawModuleList(ms);
            ms.pop();
        }
        ms.push();
        ms.translate(0, heightOffsetLeft, 0);
        ms.pop();

        HudRenderer.getInstance().render();
    }

    @Override
    public void onHudRender() {
        MatrixStack ms = Renderer.R3D.getEmptyMatrixStack();
        drawTopLeft(ms);
    }

    void drawTopLeft(MatrixStack ms) {
//        DrawableHelper.drawTexture(ms, 3, 3, 0, 0, j, i, j, i);
        double rootX = 0;
        double rootY = 6;
        List<String> values = new ArrayList<>();
        if (this.fps.getValue()) {
            values.add(((IMinecraftClientAccessor) ShadowMain.client).getCurrentFps() + " fps");
        }

        if (this.tps.getValue()) {
            String tStr = currentTps + "";
            String[] dotS = tStr.split("\\.");
            String tpsString = dotS[0];
            if (!dotS[1].equalsIgnoreCase("0")) {
                tpsString += "." + dotS[1];
            }
            values.add(tpsString + " tps");
        }
        if (this.ping.getValue()) {
            PlayerListEntry ple = Objects.requireNonNull(ShadowMain.client.getNetworkHandler()).getPlayerListEntry(Objects.requireNonNull(ShadowMain.client.player).getUuid());
            values.add((ple == null || ple.getLatency() == 0 ? "?" : ple.getLatency() + "") + " ms");
        }
        if (this.coords.getValue()) {
            BlockPos bp = Objects.requireNonNull(ShadowMain.client.player).getBlockPos();
            values.add(bp.getX() + " " + bp.getY() + " " + bp.getZ());
        }
        String drawStr = String.join(" | ", values);
        double width = FontRenderers.getRenderer().getStringWidth(drawStr) + 5;
        if (values.isEmpty()) {
            return;
        }
        double height = FontRenderers.getRenderer().getMarginHeight()+2;
        int i = (int) Math.round(48/1.3d);
        int j = (int) Math.round(194/1.3d);
        Renderer.R2D.renderRoundedQuad(ms, Renderer.Util.modify(ThemeManager.getMainTheme().getModule(), -1, -1, -1, 200), rootX - 5, -5, rootX + width + 5, rootY + height + i + 3 + 5, 7, 14);
        Renderer.R2D.renderRoundedQuad(ms, ThemeManager.getMainTheme().getInactive(), rootX + 1, rootY + i + 3, rootX + width, rootY + height + i + 3, 5, 11);
        RenderSystem.defaultBlendFunc();
        real = height + i + 3 + 5;
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, LOGO);
        Renderer.R2D.renderTexture(ms,((rootX + width + 5) / 2) - (j / 2) + 15,5,j,i,0,0,j,i,j,i);
        //Renderer.R2D.renderQuad(ms, ThemeManager.getMainTheme().getActive(), rootX , rootY + i + 3, rootX + 1, rootY + height + i + 3);
        FontRenderers.getRenderer().drawString(ms, drawStr, rootX + 2, rootY + height / 2d - FontRenderers.getRenderer().getMarginHeight() / 2d + i + 3, 0xAAAAAA);
    }

    void drawModuleList(MatrixStack ms) {
        double width = ShadowMain.client.getWindow().getScaledWidth();
        double y = 0;
        for (ModuleEntry moduleEntry : moduleList.stream().sorted(Comparator.comparingDouble(value -> -value.getRenderWidth())).toList()) {
            double prog = moduleEntry.getAnimProg() * 2;
            if (prog == 0) {
                continue;
            }
            double expandProg = MathHelper.clamp(prog, 0, 1); // 0-1 as 0-1 from 0-2
            double slideProg = MathHelper.clamp(prog - 1, 0, 1); // 1-2 as 0-1 from 0-2
            double hei = (FontRenderers.getRenderer().getMarginHeight() + 2);
            double wid = moduleEntry.getRenderWidth() + 3;
            Renderer.R2D.renderQuad(ms, ThemeManager.getMainTheme().getActive(), width - (wid + 1), y, width, y + hei * expandProg);
            ms.push();
            ms.translate((1 - slideProg) * wid, 0, 0);
            Renderer.R2D.renderQuad(ms, ThemeManager.getMainTheme().getModule(), width - wid, y, width, y + hei * expandProg);
            double nameW = FontRenderers.getRenderer().getStringWidth(moduleEntry.module.getName());
            FontRenderers.getRenderer().drawString(ms, moduleEntry.module.getName(), width - wid + 1, y + 1, 0xFFFFFF);
            if (moduleEntry.module.getContext() != null && !moduleEntry.module.getContext().isEmpty()) {
                FontRenderers.getRenderer().drawString(ms, " " + moduleEntry.module.getContext(), width - wid + 1 + nameW, y + 1, 0xAAAAAA);
            }
            ms.pop();
            y += hei * expandProg;
        }

    }

    void makeSureIsInitialized() {
        if (moduleList.isEmpty()) {
            for (Module module : ModuleRegistry.getModules()) {
                ModuleEntry me = new ModuleEntry();
                me.module = module;
                moduleList.add(me);
            }
            moduleList.sort(Comparator.comparingDouble(value -> -FontRenderers.getRenderer().getStringWidth(value.module.getName())));
        }
    }

    @Override
    public void onFastTick() {
        rNoConnectionPosY = Transitions.transition(rNoConnectionPosY, shouldNoConnectionDropDown() ? 10 : -10, 10);
        HudRenderer.getInstance().fastTick();
        makeSureIsInitialized();
        for (ModuleEntry moduleEntry : new ArrayList<>(moduleList)) {
            moduleEntry.animate();
        }
    }

    boolean shouldNoConnectionDropDown() {
        return System.currentTimeMillis() - lastTimePacketReceived > 2000;
    }

    static class ModuleEntry {
        Module module;
        double animationProgress = 0;
        double renderWidth = getWidth();

        void animate() {
            double a = 0.02;
            if (module == null || !module.isEnabled()) {
                a *= -1;
            }
            animationProgress += a;
            animationProgress = MathHelper.clamp(animationProgress, 0, 1);
            renderWidth = Transitions.transition(renderWidth, getWidth(), 7, 0);
        }

        double getAnimProg() {
            return easeInOutCirc(animationProgress);
        }

        String getDrawString() {
            if (module == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(module.getName());
            if (module.getContext() != null && !module.getContext().isEmpty()) {
                sb.append(" ").append(module.getContext());
            }
            return sb.toString();
        }

        double getWidth() {
            return FontRenderers.getRenderer().getStringWidth(getDrawString());
        }

        double getRenderWidth() {
            return renderWidth;
        }

        double easeInOutCirc(double x) {
            return x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? Math.pow(2, 20 * x - 10) / 2 : (2 - Math.pow(2, -20 * x + 10)) / 2;

        }
    }

}