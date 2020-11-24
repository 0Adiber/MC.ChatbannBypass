package at.adiber.bot;

import at.adiber.bot.listener.MessageListener;
import at.adiber.bot.listener.ReadyListener;
import at.adiber.config.BotConfig;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.security.auth.login.LoginException;

@Data
public final class Bot {
    public static int USERNAMECOLOR = 37887;

    private BotConfig config;
    private JDABuilder builder;
    private JDA api;

    public Bot(BotConfig config) {
        this.config = config;
        builder = JDABuilder.createDefault(config.getToken())
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
        guild.removeRoleFromMember(userId, guild.getMemberById(userId).getRoles().stream().filter(r -> r.getColorRaw() == USERNAMECOLOR).findFirst().get()).queue();
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

}
