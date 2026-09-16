package org.dpdns.wayne227304.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.bukkit.plugin.java.JavaPlugin;
import org.dpdns.wayne227304.managers.PluginManager;

import java.util.Locale;

public final class DiscordManager extends ListenerAdapter {
    private static final DiscordManager INSTANCE = new DiscordManager();
    private static final String MODAL_ID = "discordwhitelist:modal";
    private static final String JAVA_INPUT = "java_name";
    private static final String BEDROCK_INPUT = "bedrock_name";

    private JDA jda;
    private JavaPlugin plugin;

    private DiscordManager() {
    }

    public static DiscordManager getInstance() {
        return INSTANCE;
    }

    public void start(JavaPlugin plugin) {
        this.plugin = plugin;
        if (!plugin.getConfig().getBoolean("discord.enabled", false)) {
            plugin.getLogger().info("Discord integration is disabled in config.yml.");
            return;
        }

        String token = plugin.getConfig().getString("discord.token", "").trim();
        if (token.isEmpty() || token.equals("PUT_YOUR_BOT_TOKEN_HERE")) {
            plugin.getLogger().warning("Discord is enabled but discord.token is not configured.");
            return;
        }

        try {
            jda = JDABuilder.createDefault(token)
                    .addEventListeners(this)
                    .build();
            jda.awaitReady();
            registerCommand();
            plugin.getLogger().info("Discord bot connected as " + jda.getSelfUser().getAsTag() + ".");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            plugin.getLogger().severe("Discord bot startup was interrupted.");
        } catch (Exception exception) {
            plugin.getLogger().severe("Could not start Discord bot: " + exception.getMessage());
        }
    }

    private void registerCommand() {
        String commandName = plugin.getConfig().getString("discord.command-name", "whitelist")
                .trim().toLowerCase(Locale.ROOT);
        String guildId = plugin.getConfig().getString("discord.guild-id", "").trim();
        if (!guildId.isEmpty()) {
            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                plugin.getLogger().warning("Discord guild not found: " + guildId);
                return;
            }
            guild.updateCommands().addCommands(Commands.slash(commandName, "新增 Java 版或基岩版玩家至白名單")).queue();
            return;
        }

        jda.updateCommands().addCommands(Commands.slash(commandName, "新增 Java 版或基岩版玩家至白名單")).queue();
        plugin.getLogger().info("Registered global Discord command; it may take up to one hour to appear.");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = plugin.getConfig().getString("discord.command-name", "whitelist")
                .trim().toLowerCase(Locale.ROOT);
        if (!event.getName().equals(commandName)) {
            return;
        }

        String requiredPermission = plugin.getConfig().getString("discord.command-permission", "").trim();
        if (!requiredPermission.isEmpty() && event.getMember() != null
                && !event.getMember().hasPermission(net.dv8tion.jda.api.Permission.valueOf(requiredPermission))) {
            event.reply("你沒有使用此指令的權限。此指令需要 Discord 權限：" + requiredPermission)
                    .setEphemeral(true).queue();
            return;
        }

        TextInput javaInput = TextInput.create(JAVA_INPUT, "Java 版", TextInputStyle.SHORT)
                .setPlaceholder("Java 玩家名稱（可留白）")
                .setRequired(false)
                .setMaxLength(16)
                .build();
        TextInput bedrockInput = TextInput.create(BEDROCK_INPUT, "基岩版", TextInputStyle.SHORT)
                .setPlaceholder("基岩版玩家名稱（可留白，不需輸入 .）")
                .setRequired(false)
                .setMaxLength(32)
                .build();
        Modal modal = Modal.create(MODAL_ID, "Minecraft 白名單")
                .addComponents(ActionRow.of(javaInput), ActionRow.of(bedrockInput))
                .build();
        event.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals(MODAL_ID)) {
            return;
        }

        String javaName = valueOf(event, JAVA_INPUT);
        String bedrockName = valueOf(event, BEDROCK_INPUT);
        event.deferReply(true).queue();
        plugin.getServer().getScheduler().runTask(plugin, () -> process(event, javaName, bedrockName));
    }

    private String valueOf(ModalInteractionEvent event, String id) {
        return event.getValue(id) == null ? "" : event.getValue(id).getAsString().trim();
    }

    private void process(ModalInteractionEvent event, String javaName, String bedrockName) {
        if (javaName.isEmpty() && bedrockName.isEmpty()) {
            event.getHook().editOriginal("Java 版或基岩版至少需填寫一個。").queue();
            return;
        }

        StringBuilder response = new StringBuilder();
        if (!bedrockName.isEmpty()) {
            if (!bedrockName.startsWith(".")) {
                bedrockName = "." + bedrockName;
            }
            response.append("基岩板玩家不須要在前面加上 . 程式會自動加入\n");
        }
        addPlayer(response, javaName, false);
        addPlayer(response, bedrockName, true);
        event.getHook().editOriginal(response.toString()).queue();
    }

    private void addPlayer(StringBuilder response, String playerName, boolean bedrock) {
        if (playerName.isEmpty()) {
            return;
        }
        boolean added = bedrock
            ? PluginManager.getInstance().addBedrockToWhitelist(playerName)
            : PluginManager.getInstance().addToWhitelist(playerName);
        if (added) {
            response.append(playerName).append(" 已新增至白名單並立即生效。\n");
        } else {
            response.append("已在白名單，不須重新填加：").append(playerName).append('\n');
        }
    }

    public void stop() {
        if (jda != null) {
            jda.shutdown();
            jda = null;
        }
        plugin = null;
    }
}