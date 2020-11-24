package at.adiber.api;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;

@SpringBootApplication
public class Api implements ApplicationListener<WebServerInitializedEvent> {
    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        System.out.println(String.format("[API] started on Port %d", event.getWebServer().getPort()));
    }
}
