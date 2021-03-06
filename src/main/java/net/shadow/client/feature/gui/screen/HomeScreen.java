/*
 * Copyright (c) Shadow client, Saturn5VFive and contributors 2022. All rights reserved.
 */

package net.shadow.client.feature.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.shadow.client.ShadowMain;
import net.shadow.client.feature.gui.widget.RoundButton;
import net.shadow.client.helper.GameTexture;
import net.shadow.client.helper.Texture;
import net.shadow.client.helper.font.FontRenderers;
import net.shadow.client.helper.font.adapter.FontAdapter;
import net.shadow.client.helper.render.MSAAFramebuffer;
import net.shadow.client.helper.render.PlayerHeadResolver;
import net.shadow.client.helper.render.Renderer;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL40C;

import java.awt.Color;
import java.io.File;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class HomeScreen extends ClientScreen {
    static final double padding = 6;
    static final Texture background = GameTexture.TEXTURE_BACKGROUND.getWhere();
    static final HttpClient downloader = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    static boolean isDev = false;
    static String version = "unknown";
    static String changelog = "";
    private static HomeScreen instance;
    final FontAdapter propFr = FontRenderers.getCustomSize(22);
    final Texture currentAccountTexture = new Texture("dynamic/tex_currentaccount_home");
    boolean loaded = false;
    long initTime = System.currentTimeMillis();
    boolean fadeOut = false;
    boolean currentAccountTextureLoaded = false;
    UUID previousChecked = null;

    private HomeScreen() {
        super(MSAAFramebuffer.MAX_SAMPLES);
    }

    public static HomeScreen instance() {
        if (instance == null) {
            instance = new HomeScreen();
        }
        //        instance = new HomeScreen();
        return instance;
    }

    void load() {
        loaded = true;
        try {
            File execF = new File(HomeScreen.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            isDev = execF.isDirectory();
            HomeScreen.version = IOUtils.toString(Objects.requireNonNull(HomeScreen.class.getClassLoader().getResourceAsStream("version.txt")), StandardCharsets.UTF_8);
            HomeScreen.changelog = IOUtils.toString(Objects.requireNonNull(HomeScreen.class.getClassLoader().getResourceAsStream("changelogLatest.txt")), StandardCharsets.UTF_8);
            //            System.out.println("updating acc");
            updateCurrentAccount(() -> {

            });
            complete();
        } catch (Exception e) {
            e.printStackTrace();
            complete();
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        this.width = width;
        this.height = height;
        clearChildren();
        initWidgets();
    }

    void initWidgets() {
        List<Map.Entry<String, Runnable>> buttonsMap = new ArrayList<>();
        buttonsMap.add(new AbstractMap.SimpleEntry<>("Singleplayer", () -> ShadowMain.client.setScreen(new SelectWorldScreen(this))));
        buttonsMap.add(new AbstractMap.SimpleEntry<>("Multiplayer", () -> ShadowMain.client.setScreen(new MultiplayerScreen(this))));
        buttonsMap.add(new AbstractMap.SimpleEntry<>("Realms", () -> ShadowMain.client.setScreen(new RealmsMainScreen(this))));
        buttonsMap.add(new AbstractMap.SimpleEntry<>("Alts", () -> {
            ShadowMain.client.setScreen(AltManagerScreen.instance());
            //            Notification.create(RandomStringUtils.randomPrint(20), RandomUtils.nextLong(4000, 7000), Notification.Type.INFO);
        }));
        buttonsMap.add(new AbstractMap.SimpleEntry<>("Settings", () -> ShadowMain.client.setScreen(new OptionsScreen(this, ShadowMain.client.options))));
        buttonsMap.add(new AbstractMap.SimpleEntry<>("Quit", ShadowMain.client::scheduleStop));
        //        buttonsMap.add(new AbstractMap.SimpleEntry<>("reinit", this::init));
        double rowWidth = 150;
        double rowHeight = 30;
        double padding = 5;
        int entriesPerRow = 2;
        double rootX = width / 2d;
        double rootY = height / 2d;
        List<List<Map.Entry<String, Runnable>>> part = Lists.partition(buttonsMap, entriesPerRow);
        for (int ii = 0; ii < part.size(); ii++) {
            double currentY = ii * (rowHeight + padding);
            List<Map.Entry<String, Runnable>> entries = part.get(ii);
            double oneButtonWidth = (rowWidth - padding) / entries.size();
            for (int i = 0; i < entries.size(); i++) {
                Map.Entry<String, Runnable> e = entries.get(i);
                double currentX = i * oneButtonWidth + i * padding;
                RoundButton btn = new RoundButton(RoundButton.STANDARD, rootX - rowWidth / 2d + currentX, rootY + currentY, oneButtonWidth, rowHeight, e.getKey(), e.getValue());
                addDrawableChild(btn);
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        initTime = System.currentTimeMillis();
        initWidgets();
        if (loaded) {
            updateCurrentAccount(() -> {
            }); // already loaded this instance, refresh on the fly
        } else load();
    }

    void complete() {
        fadeOut = true;
    }

    void updateCurrentAccount(Runnable callback) {
        UUID uid = ShadowMain.client.getSession().getProfile().getId();
        if (previousChecked != null && previousChecked.equals(uid)) {
            callback.run();
            return;
        }
        previousChecked = uid;
        PlayerHeadResolver.resolve(uid, this.currentAccountTexture);
        currentAccountTextureLoaded = true;
        callback.run();
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {

        Renderer.R2D.renderQuad(stack, new Color(20, 20, 20), 0, 0, width, height);

        //RenderSystem.setShaderTexture(0, background);
        //Renderer.R2D.renderTexture(stack, 0, 0, width, height, 0, 0, width, height, width, height);
        //RenderSystem.defaultBlendFunc();

        propFr.drawString(stack, "Changelog", 6, 6, 0xFFFFFF, false);
        double yoff = 6 + propFr.getMarginHeight();
        for (String s : changelog.split("\n")) {
            FontRenderers.getRenderer().drawString(stack, s, 6, (float) yoff, 0xAAAAAA, false);
            yoff += FontRenderers.getRenderer().getMarginHeight();
        }

        double originalWidth = 2888;
        double originalHeight = 1000;
        double newWidth = 150;
        double per = newWidth / originalWidth;
        double newHeight = originalHeight * per;
        RenderSystem.setShaderTexture(0, GameTexture.TEXTURE_LOGO.getWhere());
        Renderer.R2D.renderTexture(stack, width / 2d - newWidth / 2d, height / 2d - newHeight - padding, newWidth, newHeight, 0, 0, newWidth, newHeight, newWidth, newHeight);
        super.renderInternal(stack, mouseX, mouseY, delta); // render bottom row widgets

        double texDim = 20;
        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Renderer.R2D.renderRoundedQuadInternal(stack.peek().getPositionMatrix(), 0, 0, 0, 1, width - padding - texDim, padding, width - padding, padding + texDim, 3, 10);

        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderTexture(0, currentAccountTextureLoaded ? currentAccountTexture : DefaultSkinHelper.getTexture());
        if (currentAccountTextureLoaded) {
            Renderer.R2D.renderTexture(stack, width - padding - texDim, padding, texDim, texDim, 0, 0, 64, 64, 64, 64);
        } else {
            Renderer.R2D.renderTexture(stack, width - padding - texDim, padding, texDim, texDim, 8, 8, 8, 8, 64, 64);
        }
        FontAdapter fa = FontRenderers.getRenderer();
        RenderSystem.defaultBlendFunc();
        String uname = ShadowMain.client.getSession().getUsername();
        double unameWidth = fa.getStringWidth(uname);
        fa.drawString(stack, uname, width - padding - texDim - padding - unameWidth, padding + texDim / 2d - fa.getFontHeight() / 2d, 0xFFFFFF);
    }

}
