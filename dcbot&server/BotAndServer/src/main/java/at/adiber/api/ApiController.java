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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import at.adiber.api.beans.MessageType;
import org.springframework.web.bind.annotation.RequestBody;

public class ApiController {

    private static ObjectMapper mapper;

    @PostMapping("message/{type}")
    Result message(@PathVariable String type, @RequestBody JsonNode payload) {
        try {
            MessageType mt = MessageType.valueOf(type);
            Message msg = mapper.treeToValue(payload, Message.class);

            switch (mt) {
                case MC:
                        Chat.clan.push(msg);
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
        } catch (JsonProcessingException e) {
            throw new NullPointerException("Cannot read payload as Message Object");
        }
    }

    @GetMapping("message/{type}")
    Message message(@PathVariable String type) {
        try {
            MessageType mt = MessageType.valueOf(type);

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
        }
    }

    @PostMapping("verify")
    Result verify(@RequestBody JsonNode payload) {
        try {
            Verification ver = mapper.treeToValue(payload, Verification.class);

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
        } catch (JsonProcessingException e) {
            throw new NullPointerException("Cannot read payload as Verification Object");
        }
    }
}
