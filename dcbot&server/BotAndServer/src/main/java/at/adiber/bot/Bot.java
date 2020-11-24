package at.adiber.bot;

import at.adiber.bot.listener.ReadyListener;
import at.adiber.config.BotConfig;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.security.auth.login.LoginException;

@Data
public final class Bot {
    private BotConfig config;
    private JDABuilder builder;
    private JDA api;

    public Bot(BotConfig config) {
        this.config = config;
        builder = JDABuilder.createDefault(config.getToken())
                            .addEventListeners(new ReadyListener())
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
                .addField("Your token: ", token, false).build();
    }

    public void complete(Long userId, String username) {
        api.getGuildById(config.getGuildId()).createRole().setColor()
    }

}
