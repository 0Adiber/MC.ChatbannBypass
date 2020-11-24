package at.adiber.api;

import at.adiber.api.chat.Chat;
import at.adiber.api.chat.Message;
import at.adiber.api.status.Result;
import at.adiber.main.Main;
import at.adiber.token.TokenGenerator;
import at.adiber.token.Verification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.internal.parser.Token;
import org.springframework.web.bind.annotation.*;
import at.adiber.api.beans.MessageType;

import java.util.EmptyStackException;

@RestController
public class ApiController {

    private static ObjectMapper mapper = new ObjectMapper();

    @PostMapping("message/{type}")
    Result message(@PathVariable String type, @RequestBody Message msg) {
        try {
            MessageType mt = MessageType.valueOf(type.toUpperCase());
            //Message msg = mapper.treeToValue(payload, Message.class);

            switch (mt) {
                case MC:
                        Chat.clan.push(msg);
                        Main.bot.incoming();
                    break;
                case DC:
                        Chat.discord.push(msg);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("No implementation for MessageType (%s)", type));
            }

            return new Result("message", "success");

        } catch(IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("No such MessageType (%s)", type));
        //} catch (JsonProcessingException e) {
        //    throw new NullPointerException("Cannot read payload as Message Object");
        }
    }

    @GetMapping("message/{type}")
    Message message(@PathVariable String type) {
        try {
            MessageType mt = MessageType.valueOf(type.toUpperCase());

            switch (mt) {
                case MC:
                    return Chat.clan.pop();
                case DC:
                    return Chat.discord.pop();
                default:
                    throw new IllegalArgumentException(String.format("No implementation for MessageType (%s)", type));
            }

        } catch(IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("No such MessageType (%s)", type));
        } catch(EmptyStackException e) {
            return null;
        }
    }

    @PostMapping("verify")
    Result verify(@RequestBody Verification ver) {
            if(Main.bot.isUsernameVerified(ver.getUsername())) {
                throw new IllegalArgumentException(String.format("Username '%s' is already verified!", ver.getUsername()));
            }

            if(TokenGenerator.tokens.containsKey(ver.getToken())) {
                Long userid = TokenGenerator.tokens.get(ver.getToken());
                TokenGenerator.tokens.remove(ver.getToken());
                Main.bot.complete(userid, ver.getUsername());
                return new Result("verify", "success");
            }
            throw new IllegalArgumentException(String.format("Token '%s' does not exist!", ver.getToken()));
    }
}
