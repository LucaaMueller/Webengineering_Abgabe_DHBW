package org.example.web_eng2;

import io.vertx.core.json.jackson.DatabindCodec;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

public class VertxJacksonConfig {

    public static void configureJackson() {
        // Registriere das JavaTimeModule für den Standard-Mapper
        DatabindCodec.mapper().registerModule(new JavaTimeModule());
        DatabindCodec.mapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Registriere das JavaTimeModule für den "pretty"-Mapper
        DatabindCodec.prettyMapper().registerModule(new JavaTimeModule());
        DatabindCodec.prettyMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}