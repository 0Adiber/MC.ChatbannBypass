package at.adiber.bot.listener;

import at.adiber.main.Main;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import at.adiber.token.TokenGenerator;

import javax.annotation.Nonnull;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        super.onMessageReceived(event);

        if(event.getAuthor().isBot()) return;

        String msg = event.getMessage().getContentRaw().trim();

        //check whether message is for this bot
        if(!msg.startsWith(Main.bot.getConfig().getPrefix())) return;

        msg = msg.substring(Main.bot.getConfig().getPrefix().length()).trim();

        String[] args = msg.split(" ");

        if(event.getChannel().getType() == ChannelType.PRIVATE) {
            if(args[0].equalsIgnoreCase("verify")) {
                //TODO: check whether discord user already verified (API)

                String token = TokenGenerator.nextString(event.getAuthor().getIdLong());
                event.getChannel().sendMessage(Main.bot.tokenEmbed(token)).queue();
            }
        }

        if(event.getChannel().getId().equalsIgnoreCase(Main.bot.getConfig().getSyncChannel())) {
            //TODO
        }

    }
}
