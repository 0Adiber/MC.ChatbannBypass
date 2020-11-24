package at.adiber.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BotConfig {
    private String token;
    private String syncChannel;
    private String prefix;
    private int color;
    private Long guildId;
}
