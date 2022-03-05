/*
 * This file is part of the atomic client distribution.
 * Copyright (c) 2021-2021 0x150.
 */

package me.x150.coffee.feature.command.impl;

import me.x150.coffee.feature.command.Command;
import me.x150.coffee.feature.command.CommandRegistry;

import java.awt.*;

public class Help extends Command {

    public Help() {
        super("Help", "Shows all commands", "help", "h", "?", "cmds", "commands", "manual", "man");
    }

    @Override
    public void onExecute(String[] args) {
        message("All commands and their description");
        for (Command command : CommandRegistry.getCommands()) {
            message(command.getName() + ": " + command.getDescription());
            message0("  " + String.join(", ", command.getAliases()), Color.GRAY);
        }
    }
}
