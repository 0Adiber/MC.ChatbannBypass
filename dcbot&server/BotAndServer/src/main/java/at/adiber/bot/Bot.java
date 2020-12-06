package at.adiber.bot;

import at.adiber.api.chat.Chat;
import at.adiber.api.chat.Message;
import at.adiber.bot.listener.MessageListener;
import at.adiber.bot.listener.ReadyListener;
import at.adiber.config.BotConfig;
import at.adiber.util.Minecraft;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Data
public final class Bot {
    public static int USERNAMECOLOR = 37887;

    private BotConfig config;
    private JDABuilder builder;
    private JDA api;

    public Bot(BotConfig config) {
        this.config = config;
        builder = JDABuilder.createDefault(config.getToken())
                            .setChunkingFilter(ChunkingFilter.ALL)
                            .setMemberCachePolicy(MemberCachePolicy.ALL)
                            .enableIntents(GatewayIntent.GUILD_MEMBERS)
                            .addEventListeners(new ReadyListener(), new MessageListener())
                            .setAutoReconnect(true);

        builder.setActivity(Activity.watching("https://adiber.rocks"));

        try {
            api = builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        api.shutdownNow();
    }

    public MessageEmbed tokenEmbed(String token) {
        return new EmbedBuilder().setColor(config.getColor())
                .setDescription("**TOKEN**")
                .addField("Token: ", token, true).build();
    }

    public void complete(Long userId, String username) {
        Guild guild = api.getGuildById(config.getGuildId());
        guild.createRole().setColor(USERNAMECOLOR).setName(username).queue(role -> guild.addRoleToMember(userId, role).queue());
        guild.addRoleToMember(userId, guild.getRolesByName("verified", true).get(0)).queue();
        guild.getMemberById(userId).getUser().openPrivateChannel().queue(channel -> channel.sendMessage(verificationCompleted(username)).queue());
    }

    public MessageEmbed invalidate(Long userId) {
        Guild guild = api.getGuildById(config.getGuildId());
        guild.removeRoleFromMember(userId, guild.getRolesByName("verified", true).get(0)).queue();
        Role role = guild.getMemberById(userId).getRoles().stream().filter(r -> r.getColorRaw() == USERNAMECOLOR).findFirst().get();
        role.delete().queue();
        return new EmbedBuilder().setColor(config.getColor())
                .setDescription("**FERTIG**")
                .addField("", "Deine Verifizierung wurde aufgehoben", true).build();
    }

    private MessageEmbed verificationCompleted(String username) {
        return new EmbedBuilder().setColor(config.getColor())
                .setDescription("**FERTIG**")
                .addField("", "Deine Verifizierung ist nun abgeschlossen: " + username, true).build();
    }

    public boolean isUsernameVerified(String username) {
        return api.getGuildById(config.getGuildId()).getRolesByName(username, false).size() > 0;
    }

    public boolean isUserVerified(Long userId) {
        return api.getGuildById(config.getGuildId()).getMemberById(userId).getRoles().stream().anyMatch(r -> r.getColorRaw() == USERNAMECOLOR);
    }

    public MessageEmbed alreadyVerified() {
        return new EmbedBuilder().setColor(config.getColor())
                .setDescription("*FEHLER*")
                .addField("Ursache", "Du bist bereits verifiziert!", true).build();
    }

    public MessageEmbed notVerified() {
        return new EmbedBuilder().setColor(config.getColor())
                .setDescription("*FEHLER*")
                .addField("Ursache", "Du bist noch nicht verifiziert!", true).build();
    }

    public void incoming() {
        incoming(Chat.clan.pop());
    }

    public void incoming(Message msg) {
        try {
            URL url = new URL(Minecraft.AVATAR + msg.getSender());

            Icon icon = Icon.from(url.openStream());

            Guild guild = api.getGuildById(config.getGuildId());
            TextChannel chan = guild.getTextChannelById(config.getSyncChannel());

            chan.retrieveWebhooks().queue(webhooks -> {
                if(webhooks.size() > 0) {
                    Webhook hook = webhooks.get(0);
                    hook.getManager().setName(msg.getSender()).setAvatar(icon).queue(aVoid -> {
                        try(WebhookClient client = WebhookClient.withUrl(hook.getUrl())) {
                            client.send(msg.getMsg());
                        }
                    });
                } else {
                    chan.createWebhook(msg.getSender()).setName(msg.getSender()).setAvatar(icon).queue(h -> {

                        try (WebhookClient client = WebhookClient.withUrl(h.getUrl())) {
                            client.send(msg.getMsg());
                        }
                    });
                }
            });

            /*
            api.getGuildById(config.getGuildId()).getTextChannelById(config.getSyncChannel()).createWebhook(msg.getSender()).setName(msg.getSender()).setAvatar(icon).queue(h -> {

                try (WebhookClient client = WebhookClient.withUrl(h.getUrl())) {
                    client.send(msg.getMsg()).thenRunAsync(() -> h.delete().queue());
                }
            });*/

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
