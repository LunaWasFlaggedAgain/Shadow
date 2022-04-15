/*
 * Copyright (c) Shadow client, 0x150, Saturn5VFive 2022. All rights reserved.
 */

package net.shadow.client.feature.command.impl;

import net.shadow.client.feature.command.Command;
import net.shadow.client.feature.command.coloring.ArgumentType;
import net.shadow.client.helper.util.Utils;

public class Drop extends Command {

    public Drop() {
        super("Drop", "Drops all items in your inventory", "drop", "d", "throw");
    }

    @Override
    public ArgumentType getArgumentType(String[] args, String lookingAtArg, int lookingAtArgIndex) {
        return null;
    }

    @Override
    public void onExecute(String[] args) {
        for (int i = 0; i < 36; i++) {
            Utils.Inventory.drop(i);
        }
    }
}
