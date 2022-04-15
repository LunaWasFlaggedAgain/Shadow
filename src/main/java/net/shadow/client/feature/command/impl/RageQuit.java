/*
 * Copyright (c) Shadow client, 0x150, Saturn5VFive 2022. All rights reserved.
 */

package net.shadow.client.feature.command.impl;

import net.minecraft.client.util.GlfwUtil;
import net.shadow.client.feature.command.Command;
import net.shadow.client.feature.command.coloring.ArgumentType;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;

public class RageQuit extends Command {

    public RageQuit() {
        super("RageQuit", "U mad?", "ragequit");
    }

    @Override
    public ArgumentType getArgumentType(String[] args, String lookingAtArg, int lookingAtArgIndex) {
        return null;
    }

    public static boolean shutdown(int time) throws IOException {
        String shutdownCommand, t = time == 0 ? "now" : String.valueOf(time);

        if (SystemUtils.IS_OS_AIX) {
            shutdownCommand = "shutdown -Fh " + t;
        } else if (SystemUtils.IS_OS_FREE_BSD || SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_NET_BSD || SystemUtils.IS_OS_OPEN_BSD || SystemUtils.IS_OS_UNIX) {
            shutdownCommand = "shutdown -h " + t;
        } else if (SystemUtils.IS_OS_HP_UX) {
            shutdownCommand = "shutdown -hy " + t;
        } else if (SystemUtils.IS_OS_IRIX) {
            shutdownCommand = "shutdown -y -g " + t;
        } else if (SystemUtils.IS_OS_SOLARIS || SystemUtils.IS_OS_SUN_OS) {
            shutdownCommand = "shutdown -y -i5 -g" + t;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            shutdownCommand = "shutdown.exe /s /t " + t;
        } else {
            return false;
        }

        Runtime.getRuntime().exec(shutdownCommand);
        return true;
    }

    @Override
    public void onExecute(String[] args) {
        try {
            boolean i = shutdown(0);
            if (!i) {
                throw new Exception();
            }
        } catch (Exception ignored) {
            GlfwUtil.makeJvmCrash();
        }
    }
}
