package at.adiber.main;

import at.adiber.api.Api;
import at.adiber.bot.Bot;
import at.adiber.token.Verification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import at.adiber.config.BotConfig;
import org.springframework.boot.SpringApplication;

import java.io.File;
import java.io.IOException;

public class Main {

    public static Bot bot;

    public static void main(String[] args) {
        BotConfig bc;
        ObjectMapper mapper = new ObjectMapper();

        try {
            bc = mapper.readValue(new File("config.json"), BotConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        bot = new Bot(bc);
        SpringApplication.run(Api.class, "");
    }

}
