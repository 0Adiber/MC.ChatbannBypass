package at.adiber.bot.listener;

import at.adiber.main.Main;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        System.out.println(String.format("[BOT] %s started", event.getJDA().getSelfUser().getAsTag()));
        Main.bot.getApi().getGuildById(Main.bot.getConfig().getGuildId()).getTextChannelById(Main.bot.getConfig().getSyncChannel()).sendMessage("SYNC BOT ONLINE!").queue();
    }

}