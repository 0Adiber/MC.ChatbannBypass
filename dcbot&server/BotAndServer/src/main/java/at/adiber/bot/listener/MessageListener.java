package at.adiber.bot.listener;

import at.adiber.api.chat.Chat;
import at.adiber.api.chat.Message;
import at.adiber.bot.Bot;
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

        if(event.getChannel().getType() == ChannelType.PRIVATE) {
            //check whether message is for this bot
            if(!msg.startsWith(Main.bot.getConfig().getPrefix())) return;
            msg = msg.substring(Main.bot.getConfig().getPrefix().length()).trim();
            String[] args = msg.split(" ");

            if(args[0].equalsIgnoreCase("verify")) {
                //check whether user is already verified
                if(Main.bot.isUserVerified(event.getAuthor().getIdLong())) {
                    event.getChannel().sendMessage(Main.bot.alreadyVerified()).queue();
                    return;
                }

                //generate token & send
                String token = TokenGenerator.nextString(event.getAuthor().getIdLong());
                event.getChannel().sendMessage(Main.bot.tokenEmbed(token)).queue();
            } else if(args[0].equalsIgnoreCase("invalidate")) {
                if(Main.bot.isUserVerified(event.getAuthor().getIdLong())) {
                    event.getChannel().sendMessage(Main.bot.invalidate(event.getAuthor().getIdLong())).queue();
                } else {
                    event.getChannel().sendMessage(Main.bot.notVerified()).queue();
                }
            }
        }

        if(event.getChannel().getId().equalsIgnoreCase(Main.bot.getConfig().getSyncChannel())) {
           if(!event.getMember().getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase("verified")))
               return;
           event.getMessage().delete().queue();
           String username = event.getMember().getRoles().stream().filter(r -> r.getColorRaw() == Bot.USERNAMECOLOR).findFirst().get().getName();
           Message send = new Message(username, msg);
           Chat.discord.push(send);
           Main.bot.incoming(send);
        }

    }
}
