package org.example.web_eng2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/v3/assets/buildings/**").permitAll() // GET-Anfragen erlauben
                        .requestMatchers(HttpMethod.POST, "/api/v3/assets/buildings/**").hasAuthority("ROLE_manage-account") // Nur Benutzer mit "manage-account" können POST ausführen
                        .anyRequest().authenticated() // Alle anderen Endpunkte erfordern Authentifizierung
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())) // JWT-Authentifizierung
                )
                .csrf().disable(); // CSRF deaktivieren, wenn nicht notwendig (z. B. bei REST APIs)

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Verwendet die Zertifikate vom Identity Provider (z. B. Keycloak)
        return NimbusJwtDecoder.withJwkSetUri("http://localhost:9090/auth/realms/biletado/protocol/openid-connect/certs")
                .build();
    }

    @Bean
    public CorsFilter corsFilter() {
        // Konfiguration für Cross-Origin Resource Sharing (CORS)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:9090")); // Erlaubt das Frontend
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            Map<String, Object> claims = jwt.getClaims();

            // Logging zur Prüfung der Claims
            System.out.println("JWT Claims: " + claims);

            // Extrahiert Rollen aus `resource_access -> account -> roles`
            if (claims.containsKey("resource_access")) {
                Map<String, Object> resourceAccess = (Map<String, Object>) claims.get("resource_access");
                if (resourceAccess.containsKey("account")) {
                    Map<String, Object> accountAccess = (Map<String, Object>) resourceAccess.get("account");
                    if (accountAccess.containsKey("roles")) {
                        List<String> roles = (List<String>) accountAccess.get("roles");
                        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
                    }
                }
            }

            return authorities;
        });
        return converter;
    }
}
