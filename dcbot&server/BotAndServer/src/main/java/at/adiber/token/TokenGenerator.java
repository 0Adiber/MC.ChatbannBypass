package at.adiber.token;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TokenGenerator {

    //Token; discord UserId
    public static Map<String, Long> tokens = new HashMap<>();

    public static String nextString(Long id) {
        String token = "";
        for (int i = 0; i < 6; ++i)
            token += symbols[random.nextInt(symbols.length)];
        tokens.put(token, id);
        return token;
    }

    private static final char[] symbols = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ"+"ABCDEFGHIJKLMNOPQRSTUVWXYZ".toLowerCase()+"0123456789").toCharArray();

    private static final Random random = new Random();

}
