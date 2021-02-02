package com.fmning.tools.service.discord;

public class Command {
    private String[] commands;
    private int ind;

    public Command (String command) {
        commands = command.split("\\s+");
        ind = 0;
    }

    public String get(int ind) {
        if (commands.length > ind) {
            return commands[ind];
        }
        return null;
    }

    public String next() {
        if (commands.length > this.ind) {
            return commands[this.ind ++];
        }
        return null;
    }

}
