package at.adiber.util;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Minecraft {

    private static final String UURL = "https://mc-heads.net/minecraft/profile/";
    private static ObjectMapper mapper = new ObjectMapper();

    public static String getUUID(String username) throws IOException {
        URL url = new URL(UURL + username);

        //URLConnection conn = url.openConnection();

        //BufferedReader buffer = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        Profile p = mapper.readValue(url, Profile.class);
        return p.getName();
    }

}
