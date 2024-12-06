package org.example.web_eng2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.example.web_eng2.VertxJacksonConfig;
import io.vertx.core.Vertx;
import io.vertx.core.AbstractVerticle;



@SpringBootApplication
public class WebEng2Application {

    static {
        VertxJacksonConfig.configureJackson();
    }
    public static void main(String[] args) {
        SpringApplication.run(WebEng2Application.class, args);
    }

}
