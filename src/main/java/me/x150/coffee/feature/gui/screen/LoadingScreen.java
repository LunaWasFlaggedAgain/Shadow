package me.x150.coffee.feature.gui.screen;

import com.google.common.util.concurrent.AtomicDouble;
import lombok.Data;
import me.x150.coffee.CoffeeClientMain;
import me.x150.coffee.feature.gui.FastTickable;
import me.x150.coffee.helper.font.FontRenderers;
import me.x150.coffee.helper.font.adapter.impl.ClientFontRenderer;
import me.x150.coffee.helper.render.MSAAFramebuffer;
import me.x150.coffee.helper.render.Renderer;
import me.x150.coffee.helper.util.Transitions;
import me.x150.coffee.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoadingScreen extends ClientScreen implements FastTickable {
    static final int           atOnce   = 3;
    static       LoadingScreen INSTANCE = null;
    static       Color         GREEN    = new Color(100, 255, 20);
    static       Color         RED      = new Color(255, 50, 20);
    AtomicBoolean loaded     = new AtomicBoolean(false);
    AtomicBoolean loadInProg = new AtomicBoolean(false);
    //    double progress = 0;
    volatile AtomicDouble progress = new AtomicDouble();
    double             smoothProgress = 0;
    double             opacity        = 1;
    ClientFontRenderer title          = FontRenderers.getCustomSize(40);
    Map<CoffeeClientMain.ResourceEntry, ProgressData> progressMap = new ConcurrentHashMap<>();

    private LoadingScreen() {
        super(MSAAFramebuffer.MAX_SAMPLES);
    }

    public static LoadingScreen instance() {
        if (INSTANCE == null) {
            INSTANCE = new LoadingScreen();
        }
        return INSTANCE;
    }

    @Override protected void init() {
        assert client != null;
        HomeScreen.instance().init(client, width, height);
        if (loaded.get() && opacity == 0.001) {
            client.setScreen(HomeScreen.instance());
        }
        super.init();
    }

    @Override public void onFastTick() {

        //System.out.println(progressMap.values().stream().map(AtomicDouble::get).reduce(Double::sum)+"-"+CoffeeClientMain.resources.size());
        progress.set(progressMap.values().stream().map(progressData -> progressData.getProgress().get()).reduce(Double::sum).orElse(0d) / CoffeeClientMain.resources.size());

        smoothProgress = Transitions.transition(smoothProgress, progress.get(), 10, 0.0001);
        //        smoothProgress = progress.get();
        if (CoffeeClientMain.client.getOverlay() == null) {
            if (!loadInProg.get()) {
                load();
            }
        }
        if (loaded.get()) {
            opacity -= 0.01;
            opacity = MathHelper.clamp(opacity, 0.001, 1);
        }
        HomeScreen.instance().onFastTick();
    }

    void load() {
        loadInProg.set(true);

        ExecutorService es = Executors.newFixedThreadPool(atOnce);


        for (CoffeeClientMain.ResourceEntry resource : CoffeeClientMain.resources) {
            progressMap.put(resource, new ProgressData());
            es.execute(() -> {
                CoffeeClientMain.log(Level.INFO, "Downloading " + resource.url());
                progressMap.get(resource).getWorkingOnIt().set(true);
                try {

                    URL url = new URL(resource.url());
                    HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
                    long completeFileSize = httpConnection.getContentLength();

                    BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    byte[] data = new byte[16];
                    long downloadedFileSize = 0;
                    int x;
                    progressMap.get(resource).getProgress().set(0.1);
                    while ((x = in.read(data, 0, 16)) >= 0) {
                        downloadedFileSize += x;

                        double currentProgress = ((double) downloadedFileSize) / ((double) completeFileSize);
                        //                        progress.set(MathHelper.lerp(currentProgress, progressBefore, completedProgress));
                        progressMap.get(resource).getProgress().set(currentProgress * 0.8 + 0.1);

                        bout.write(data, 0, x);
                    }
                    bout.close();
                    in.close();
                    byte[] imageBuffer = bout.toByteArray();
                    BufferedImage bi = ImageIO.read(new ByteArrayInputStream(imageBuffer));
                    Utils.registerBufferedImageTexture(resource.tex(), bi);
                    CoffeeClientMain.log(Level.INFO, "Downloaded " + resource.url());
                } catch (Exception e) {
                    CoffeeClientMain.log(Level.ERROR, "Failed to download " + resource.url() + ": " + e.getMessage());
                } finally {
                    progressMap.get(resource).getProgress().set(1);
                    progressMap.get(resource).getWorkingOnIt().set(false);
                }
            });
        }
        new Thread(() -> {
            es.shutdown();
            try {
                //noinspection ResultOfMethodCallIgnored
                es.awaitTermination(99999, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                loaded.set(true);
            }

        }, "Loader").start();
    }

    @Override public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {

        if (loaded.get()) {
            HomeScreen.instance().renderInternal(stack, mouseX, mouseY, delta);
            if (opacity == 0.001) {
                assert this.client != null;
                this.client.setScreen(HomeScreen.instance());
                return;
            }
        }
        Renderer.R2D.renderQuad(stack, new Color(0, 0, 0, (float) opacity), 0, 0, width, height);
        String coffee = "Loading Coffee..";
        double pad = 5;
        double textWidth = title.getStringWidth(coffee) + 1;
        double textHeight = title.getMarginHeight();
        double centerY1 = height / 2d;
        double centerX = width / 2d;
        title.drawString(stack, coffee, centerX - textWidth / 2f, centerY1 - textHeight / 2d, new Color(1f, 1f, 1f, (float) opacity).getRGB());
        double maxWidth = 200;
        double rWidth = smoothProgress * maxWidth;
        double barHeight = 3;
        rWidth = Math.max(rWidth, barHeight);

        Color MID_END = Renderer.Util.lerp(GREEN, RED, smoothProgress);
        String perStr = Utils.Math.roundToDecimal(smoothProgress * 100, 1) + "%";
        Renderer.R2D.renderRoundedQuad(stack, new Color(40, 40, 40, (int) (opacity * 255)), centerX - maxWidth / 2d, centerY1 + textHeight / 2d + pad, centerX + maxWidth / 2d, centerY1 + textHeight / 2d + pad + barHeight, barHeight / 2d, 10);
        Renderer.R2D.renderRoundedQuad(stack, Renderer.Util.modify(MID_END, -1, -1, -1, (int) (opacity * 255)), centerX - maxWidth / 2d, centerY1 + textHeight / 2d + pad, centerX - maxWidth / 2d + rWidth, centerY1 + textHeight / 2d + pad + barHeight, barHeight / 2d, 10);
        double currentY = centerY1 + textHeight / 2d + pad + barHeight + 5;
        //double xOffset = 0;
        for (ProgressData value : progressMap.values()) {
            if (value.getWorkingOnIt().get()) {
                double prg = value.getProgress().get() * maxWidth;
                prg = Math.max(prg, barHeight);
                Renderer.R2D.renderRoundedQuad(stack, new Color(40, 40, 40, (int) (opacity * 255)), centerX - maxWidth / 2d, currentY, centerX + maxWidth / 2d, currentY + barHeight, barHeight / 2d, 10);
                Renderer.R2D.renderRoundedQuad(stack, Renderer.Util.modify(MID_END, -1, -1, -1, (int) (opacity * 255)), centerX - maxWidth / 2d, currentY, centerX - maxWidth / 2d + prg, currentY + barHeight, barHeight / 2d, 10);
                currentY += barHeight + 2;
            }
        }
        //Renderer.R2D.renderQuad(stack,Color.BLACK,width/2d-pslen/2d,centerY1 + textHeight / 2d + pad,width/2d+pslen/2d,centerY1 + textHeight / 2d + pad + barHeight);
        FontRenderers.getRenderer().drawCenteredString(stack, perStr, width / 2d, currentY + 3, 1f, 1f, 1f, (float) opacity);
        super.renderInternal(stack, mouseX, mouseY, delta);
    }

    @Data static class ProgressData {
        AtomicDouble  progress    = new AtomicDouble(0);
        AtomicBoolean workingOnIt = new AtomicBoolean(false);
    }
}
