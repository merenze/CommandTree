package com.jellyrekt.commandtree;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class CommandTree extends CommandTreeNode {
    private JavaPlugin plugin;
    private CommandListener commandListener = new CommandListener(this);

    public CommandTree(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register a command.
     *
     * @param command     Full command string
     * @param executor    CommandExecutor to handle the command
     */
    @Override
    public void add(String command, CommandExecutor executor) {
        super.add(stripExtraSpaces(command), executor);
    }

    /**
     * Execute a command.
     *
     * @param sender
     * @param command
     */
    public void execute(CommandSender sender, String command) {
        super.execute(sender, command, new HashMap<>());
    }

    /**
     * Register the commands in this tree to the plugin.
     * Root commands still need to be registered in plugin.yml.
     */
    public void register() {
        // Register the event listener
        plugin
                .getServer()
                .getPluginManager()
                .registerEvents(commandListener, plugin);
    }

    /**
     * Trim the string and remove all repeated spaces.
     * @param s String to fix.
     * @return The passed string, with leading, trailing, and repeated spaces removed.
     */
    static String stripExtraSpaces(String s) {
        return s.trim().replaceAll(" +", " ");
    }

    /**
     * @inheritDoc
     */
    public String toString() {
        StringBuilder builder = new StringBuilder("\n");
        for (String key : getChildren().keySet()) {
            builder.append(getChildren().get(key).toStringRec(key, 0));
        }
        return builder.append("\n").toString();
    }
}