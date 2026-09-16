package org.dpdns.wayne227304.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.plugin.java.JavaPlugin;
import org.dpdns.wayne227304.managers.PluginManager;

import java.util.Collections;
import java.util.List;

public class WhitelistCommand implements CommandExecutor, TabCompleter {
    private static final String JAVA_NAME = "javaName";
    private static final String BEDROCK_NAME = "bedrockName";

    private final JavaPlugin plugin;
    private final ConversationFactory conversationFactory;

    public WhitelistCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.conversationFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withLocalEcho(true)
                .withEscapeSequence("取消")
                .withFirstPrompt(new JavaNamePrompt());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("discordwhitelist.use")) {
            sender.sendMessage(message("messages.no-permission"));
            return true;
        }

        if (args.length > 2) {
            sender.sendMessage(message("messages.usage"));
            return true;
        }

        if (args.length > 0) {
            String javaName = args[0];
            String bedrockName = args.length == 2 ? args[1] : "";
            process(sender, javaName, bedrockName);
            return true;
        }

        if (!(sender instanceof org.bukkit.conversations.Conversable conversable)) {
            sender.sendMessage(message("messages.console-usage"));
            return true;
        }

        Conversation conversation = conversationFactory.buildConversation(conversable);
        conversation.begin();
        return true;
    }

    private void process(CommandSender sender, String javaName, String bedrockName) {
        javaName = clean(javaName);
        bedrockName = cleanBedrockName(bedrockName);

        if (javaName.isEmpty() && bedrockName.isEmpty()) {
            sender.sendMessage(message("messages.at-least-one"));
            return;
        }

        if (!bedrockName.isEmpty()) {
            sender.sendMessage(message("messages.bedrock-prefix"));
        }

        addPlayer(sender, javaName, false);
        addPlayer(sender, bedrockName, true);
    }

    private void addPlayer(CommandSender sender, String playerName, boolean bedrock) {
        if (playerName.isEmpty()) {
            return;
        }

        boolean added = bedrock
            ? PluginManager.getInstance().addBedrockToWhitelist(playerName)
            : PluginManager.getInstance().addToWhitelist(playerName);
        if (added) {
            sender.sendMessage(format("messages.added", playerName));
        } else {
            sender.sendMessage(format("messages.already-whitelisted", playerName));
        }
    }

    private String cleanBedrockName(String name) {
        name = clean(name);
        return name.isEmpty() || name.startsWith(".") ? name : "." + name;
    }

    private String clean(String name) {
        return name == null ? "" : name.trim();
    }

    private String message(String path) {
        return format(path, "");
    }

    private String format(String path, String playerName) {
        String value = plugin.getConfig().getString(path, "");
        return ChatColor.translateAlternateColorCodes('&', value.replace("{player}", playerName));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    private final class JavaNamePrompt extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            return message("messages.ask-java");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            context.setSessionData(JAVA_NAME, input);
            return new BedrockNamePrompt();
        }
    }

    private final class BedrockNamePrompt extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            return message("messages.ask-bedrock");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            context.setSessionData(BEDROCK_NAME, input);
            return new ResultPrompt();
        }
    }

    private final class ResultPrompt extends MessagePrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            CommandSender sender = (CommandSender) context.getForWhom();
            process(sender, (String) context.getSessionData(JAVA_NAME),
                    (String) context.getSessionData(BEDROCK_NAME));
            return "";
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return END_OF_CONVERSATION;
        }
    }
}