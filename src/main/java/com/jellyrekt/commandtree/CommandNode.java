package com.jellyrekt.commandtree;

import org.bukkit.command.CommandSender;

import java.util.*;

public class CommandNode {
    /**
     * Nodes containing the subcommand of this node's command
     */
    private Map<String, CommandNode> children = new HashMap<>();
    /**
     * Maps an alias to a child node's key
     */
    private Map<String, String> childAliases = new HashMap<>();
    /**
     * Executor to handle the command contained in this node.
     */
    private CommandExecutor commandExecutor = (sender, env) -> {
        return;
    };
    /**
     * Key used by parent to reference this node
     */
    private String key;
    /**
     * Permission needed to execute this command
     */
    private String permission = null;
    /**
     * Message sent when sender does not have permission to execute this command
     */
    private String permissionDeniedMessage;
    /**
     * Parent of this command node
     *
     * @param parent
     */
    private CommandNode parent;

    /**
     * Create a command node.
     *
     * @param parent Parent of this command node
     * @param key    Key used by parent to reference this node
     */
    protected CommandNode(CommandNode parent, String key) {
        this.parent = parent;
        this.key = key;
    }

    /**
     * Set the CommandExecutor for this node.
     * @param executor CommandExecutor
     * @return self
     */
    public CommandNode setExecutor(CommandExecutor executor) {
        commandExecutor = executor;
        return this;
    }

    /**
     * Set the permission needed to execute this command.
     *
     * @param permission Permission needed to execute this command
     * @return self
     */
    public CommandNode setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    /**
     * Set the message sent when sender does not have permission to execute this command
     *
     * @param message Message to send
     * @return self
     */
    public CommandNode setPermissionDeniedMessage(String message) {
        permissionDeniedMessage = message;
        return this;
    }

    /**
     * Add aliases for this command node.
     * Aliases are local to the level of the node;
     * for example, adding "b" as an alias to the command "alpha beta"
     * allows you to execute it with "alpha b".
     *
     * @param aliases Aliases for this command.
     * @return self
     */
    public CommandNode addAliases(String... aliases) {
        for (String a : aliases) {
            parent.childAliases.put(a, key);
        }
        return this;
    }

    /**
     * Register a subcommand under this command.
     *
     * @param subcommand Key (first token) of the subcommand
     */
    CommandNode add(String subcommand) {
        // Base case: Empty string
        if (subcommand.isEmpty()) {
            return this;
        }
        // Consume the first token to use as a key
        String[] split = subcommand.split(" ", 2);
        String key = split[0];
        subcommand = split.length > 1 ? split[1] : "";
        // Pass the rest of the work to the child node
        CommandNode child = getChild(key);
        // Create a child if it doesn't already exist
        // (It probably doesn't, but this way commands don't have to be defined in order)
        if (child == null) {
            // Add key as alias
            childAliases.put(key, key);
            // Create child
            children.put(key, new CommandNode(this, key));
            child = getChild(key);
        }
        // Recursive call
        return child.add(subcommand);
    }

    /**
     * Execute the given command
     *
     * @param sender
     * @param command
     * @param env
     */
    protected void execute(CommandSender sender, String command, Map<String, String[]> env) {
        // Base case: we've arrived at the final node
        if (command.isBlank()) {
            // Check for permission
            if (permission != null && !sender.hasPermission(permission)) {
                sender.sendMessage(permissionDeniedMessage);
                return;
            }
            commandExecutor.execute(sender, env);
            return;
        }
        // Get the key/alias and subcommand
        String[] split = command.split(" ", 2);
        String key = split[0];
        String subcommand = split.length > 1 ? split[1] : "";
        // Easy-to-use ref to the next node to be called
        CommandNode child = getChild(key);
        // Add any arguments to the environment
        List<String> args = new ArrayList<>();
        Scanner scanner = new Scanner(subcommand);
        String token = "";
        while (scanner.hasNext()) {
            token = scanner.next();
            // Stop parsing args if a symbol has been reached
            if (child.childAliases.containsKey(token)) {
                break;
            }
            args.add(token);
        }
        env.put(childAliases.get(key), args.toArray(new String[0]));
        // If the end of the string hasn't been reached
        if (!child.childAliases.containsKey(token)) {
            token = "";
        }
        // Turn the remainder of the string into the subcommand
        StringBuilder builder = new StringBuilder(token + " ");
        while (scanner.hasNext()) {
            builder.append(scanner.next()).append(" ");
        }
        scanner.close();
        subcommand = builder.toString().trim();
        // Call the subcommand on the child node
        child.execute(sender, subcommand, env);
    }

    /**
     * Gets a child node by key or alias.
     *
     * @param key key or alias
     * @return CommandNode child node
     */
    private CommandNode getChild(String key) {
        return children.get(childAliases.get(key));
    }

    /**
     * Checks if a String matches the param pattern
     *
     * @param s String to check
     * @return True if the string matches the param pattern
     */
    private static boolean isParam(String s) {
        return s.charAt(0) == ':';
    }

    /**
     * Converts a param string to the key used for the environment
     *
     * @param s Param string (formatted according to rules)
     * @return The string used as a key in the environment
     */
    private static String extractParamKey(String s) {
        return s.substring(1);
    }

    protected Map<String, CommandNode> getChildren() {
        return children;
    }

    /**
     * Determine whether the alias maps to a child of this ndoe.
     * @param alias Alias to check for
     * @return True if the given alias maps to a child of this node
     */
    boolean hasAlias(String alias) {
        return childAliases.containsKey(alias);
    }

    /**
     * Helper to build string representation of the full tree.
     *
     * @param key   This node's key
     * @param depth This node's depth in the command tree
     * @return String representation of this subtree
     */
    protected StringBuilder toStringRec(String key, int depth) {
        StringBuilder builder = new StringBuilder("\n");
        builder.append("| ".repeat(depth));
        builder.append(key);
        for (String k : children.keySet()) {
            builder.append(getChild(k).toStringRec(k, depth + 1));
        }
        return builder;
    }
}
