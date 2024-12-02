package  org.example.web_eng2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service

public class TokenService {

    private final RestTemplate restTemplate;

    public TokenService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAccessToken() {
        String tokenUrl = "http://localhost:8080/auth/realms/biletado/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("angular", "client-secret"); // Client-ID und Secret aus deinem Setup
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=client_credentials";

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        // Hier den Access Token aus der Antwort extrahieren
        // Zum Beispiel, wenn die Antwort ein JSON-Objekt ist
        // {"access_token": "xyz", ...}
        return response.getBody(); // Du musst den Access Token extrahieren.
    }
}
