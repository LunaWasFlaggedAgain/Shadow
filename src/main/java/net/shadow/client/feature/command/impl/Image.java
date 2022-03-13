/*
 * Copyright (c) Shadow client, 0x150, Saturn5VFive 2022. All rights reserved.
 */

package net.shadow.client.feature.command.impl;

import net.shadow.client.ShadowMain;
import net.shadow.client.feature.command.Command;
import net.shadow.client.helper.event.EventType;
import net.shadow.client.helper.event.Events;
import net.shadow.client.helper.event.events.PacketEvent;
import net.shadow.client.helper.util.Utils;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.hit.BlockHitResult;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Image extends Command {
    final String block = "█";
    final String unblock = "⠀";
    BufferedImage imageToBuild;
    boolean real;

    public Image() {
        super("Image", "apply images to various text mediums", "image", "img");
        Events.registerEventHandler(EventType.PACKET_RECEIVE, event -> {
            if(!real) return;
            PacketEvent pe = (PacketEvent) event;
            if (pe.getPacket() instanceof GameMessageS2CPacket p) {
                if (p.getMessage().getString().contains("Command set:")) {
                    event.setCancelled(true);
                }
            }
        });
    }

    @Override
    public String[] getSuggestions(String fullCommand, String[] args) {
        if(args.length == 1){
            return new String[]{"chat", "book", "lore"};
        }
        if(args.length == 2){
            return new String[]{"(url)"};
        }
        if(args.length == 3){
            return new String[]{"(size)"};
        }
        return super.getSuggestions(fullCommand, args);
    }

    @Override
    public void onExecute(String[] args) {
        if (args.length < 3) {
            message("Please Use >image <mode> <url> <size>, or >image help");
            return;
        }
        switch (args[0]) {
            case "help" -> {
                message("Modes:");
                message(">image chat");
                message(">image book");
                message(">image lore");
            }
            case "chat" -> new Thread(() -> {
                try {
                    real = true;
                    loadImage(args[1], Integer.parseInt(args[2]));
                    int max = imageToBuild.getHeight();
                    for (int index = 0; index < max; index++) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("[");
                        for (int i = 0; i < imageToBuild.getWidth(); i++) {
                            int r = imageToBuild.getRGB(i, index);
                            int rP = r & 0xFFFFFF | 0xF000000;
                            builder.append("{\"text\":\"").append(block).append("\",\"color\":\"#").append(Integer.toString(rP, 16).substring(1)).append("\"},");
                        }
                        String mc = builder.substring(0, builder.length() - 1) + "]";
                        Utils.sleep(50);
                        ShadowMain.client.player.networkHandler.sendPacket(new UpdateCommandBlockC2SPacket(((BlockHitResult) ShadowMain.client.crosshairTarget).getBlockPos(), "REST", CommandBlockBlockEntity.Type.REDSTONE, false, false, false));
                        Utils.sleep(50);
                        ShadowMain.client.player.networkHandler.sendPacket(new UpdateCommandBlockC2SPacket(((BlockHitResult) ShadowMain.client.crosshairTarget).getBlockPos(), "/execute run tellraw @a " + mc, CommandBlockBlockEntity.Type.REDSTONE, false, false, true));
                    }
                } catch (Exception e) {
                    message("ChatPrinter");
                }
                Utils.sleep(2000);
                real = false;
            }).start();
            case "chat2" -> new Thread(() -> {
                try {
                    real = true;
                    loadImage(args[1], Integer.parseInt(args[2]));
                    int max = imageToBuild.getHeight();
                    for (int index = 0; index < max; index++) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("[");
                        for (int i = 0; i < imageToBuild.getWidth(); i++) {
                            int r = imageToBuild.getRGB(i, index);
                            int rP = r & 0xFFFFFF | 0xF000000;
                            builder.append("{\"text\":\"").append(block).append("\",\"color\":\"#").append(Integer.toString(rP, 16).substring(1)).append("\"},");
                        }
                        String mc = builder.substring(0, builder.length() - 1) + "]";
                        Utils.sleep(50);
                        ShadowMain.client.player.networkHandler.sendPacket(new UpdateCommandBlockC2SPacket(((BlockHitResult) ShadowMain.client.crosshairTarget).getBlockPos(), "REST", CommandBlockBlockEntity.Type.REDSTONE, false, false, false));
                        Utils.sleep(50);
                        ShadowMain.client.player.networkHandler.sendPacket(new UpdateCommandBlockC2SPacket(((BlockHitResult) ShadowMain.client.crosshairTarget).getBlockPos(), "/tellraw @a " + mc, CommandBlockBlockEntity.Type.REDSTONE, false, false, true));
                    }
                } catch (Exception e) {
                    message("ChatPrinter");
                }
                Utils.sleep(2000);
                real = false;
            }).start();
            case "lore" -> {
                ItemStack item = ShadowMain.client.player.getMainHandStack();
                StringBuilder page = new StringBuilder();
                loadImage(args[1], Integer.parseInt(args[2]));
                int max = imageToBuild.getHeight();
                for (int index = 0; index < max; index++) {
                    StringBuilder lamo = new StringBuilder();
                    for (int i = 0; i < imageToBuild.getWidth(); i++) {
                        int r = imageToBuild.getRGB(i, index);
                        int hex = r & 0xFFFFFF | 0xF000000;
                        lamo.append("{\"text\":\"").append(block).append("\",\"color\":\"#").append(Integer.toString(hex, 16).substring(1)).append("\",\"italic\":false},");
                    }
                    String lamopage = lamo.substring(0, lamo.length() - 1);
                    page.append("'[" + lamopage + "]'" + ",");
                }
                String loader = page.substring(0, page.length() - 1);
                try {
                    item.getOrCreateNbt().copyFrom(StringNbtReader.parse("{display:{Lore:[" + loader + "]}}"));
                } catch (Exception ignored) {
                }
                ShadowMain.client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + ShadowMain.client.player.getInventory().selectedSlot, item));
                message("ImageBook");
            }
            case "book" -> {
                ItemStack book = new ItemStack(Items.WRITTEN_BOOK, 1);
                StringBuilder pager = new StringBuilder();
                loadImage(args[1]);
                int ma2x = imageToBuild.getHeight();
                for (int index = 0; index < ma2x; index++) {
                    for (int i = 0; i < imageToBuild.getWidth(); i++) {
                        int r = imageToBuild.getRGB(i, index);
                        int hex = r & 0xFFFFFF | 0xF000000;
                        pager.append("{\"text\":\"").append(block).append("\",\"color\":\"#").append(Integer.toString(hex, 16).substring(1)).append("\"},");
                    }
                    pager.append("{\"text\":\"\\\\n\"},");
                }
                String loaderstr = pager.substring(0, pager.length() - 1);
                try {
                    book.getOrCreateNbt().copyFrom(StringNbtReader.parse("{title:\"\",author:\"ImageBook\",pages:['[" + loaderstr + "]']}"));
                } catch (Exception ignored) {
                }
                ShadowMain.client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + ShadowMain.client.player.getInventory().selectedSlot, book));
                message("ImageBook");
            }
        }
    }

    public void loadImage(String imageurl, int size) {
        try {
            URL u = new URL(imageurl);
            HttpURLConnection huc = (HttpURLConnection) u.openConnection();
            huc.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0");
            huc.connect();
            InputStream is = huc.getInputStream();
            BufferedImage loadedImage = ImageIO.read(is);
            double scale = (double) loadedImage.getWidth() / (double) size;
            imageToBuild = resize(loadedImage, (int) (loadedImage.getWidth() / scale), (int) (loadedImage.getHeight() / scale));
            message("Loaded Image into memory");
            huc.disconnect();
        } catch (Exception ignored) {
            message("Failed to Loaded Image into memory");
        }
    }

    public void loadImage(String imageurl) {
        try {
            URL u = new URL(imageurl);
            HttpURLConnection huc = (HttpURLConnection) u.openConnection();
            huc.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0");
            huc.connect();
            InputStream is = huc.getInputStream();
            BufferedImage loadedImage = ImageIO.read(is);
            double scalew = (double) loadedImage.getWidth() / 12;
            double scaleh = (double) loadedImage.getHeight() / 15;
            imageToBuild = resize(loadedImage, (int) (loadedImage.getWidth() / scalew), (int) (loadedImage.getHeight() / scaleh));
            huc.disconnect();
        } catch (Exception ignored) {
            message("ImageLoader");
        }
    }

    private BufferedImage resize(BufferedImage img, int newW, int newH) {
        java.awt.Image tmp = img.getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }
}