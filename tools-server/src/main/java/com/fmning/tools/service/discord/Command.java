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

    public int length() {
        return commands.length;
    }

    public boolean equals(int ind, String name, String alias) {
        if (commands.length > ind) {
            String command = commands[ind];
            if (command.equalsIgnoreCase(name)) return true;
            return alias == null || command.equalsIgnoreCase(alias);
        }

        return false;
    }

}
