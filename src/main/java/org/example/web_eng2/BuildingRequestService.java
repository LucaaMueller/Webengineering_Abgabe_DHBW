/*package org.example.web_eng2;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BuildingRequestService {

    private final RestTemplate restTemplate;


    public BuildingRequestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String sendBuildingRequest() {

        String url = "http://localhost:8080/api/v3/assets/buildings";


        String jwtToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJPQ1ItaVFXb0lqQUw4X0R6dU1Pc1d4NG9VS0ZuN3VMaEozVDdOMkh5NDVrIn0.eyJleHAiOjE3MzMwMTIwMTUsImlhdCI6MTczMzAxMTk1NSwiYXV0aF90aW1lIjoxNzMzMDExOTU1LCJqdGkiOiIxYjBkZDRhNy1lZWI4LTQyMTMtODY1MS1iM2RjMjI3ZjJkODAiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwOTAvYXV0aC9yZWFsbXMvYmlsZXRhZG8iLCJhdWQiOiJhbmd1bGFyIiwic3ViIjoiOWFkOTAxYmEtZjQ0MC00YzIxLTgyNjctYzRjMTk5M2I4NmQ4IiwidHlwIjoiSUQiLCJhenAiOiJhbmd1bGFyIiwibm9uY2UiOiIxOTc0ZTcyNi03YTY2LTRkNDItOWU3ZC0wYWQxZjQzNTYyMGQiLCJzaWQiOiJlYmZiNGE2ZC01OThlLTQwMTQtYWZjNy1lOTU3N2M4MzM2ZmUiLCJhdF9oYXNoIjoiWk5JWVdWNEpFN05qOEtBM1o3djJCdyIsImFjciI6IjEiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6ImJpbGV0YWRvIn0.rZIsQ4yhimnR3lzsRz5mpcGNK6EvjskubGGe6TB4yS71jZU_KnQ7Iy5SWPNbIDJMuOqLqlAXiJf7pzLEU6zvJjGeVAJR1X_qCMSEX5mPdsR2ypzlVkgIdUE7MR569TAd4A_DDAyNzqxxseokMamBxog8AAwbGCRfxqIbBBd7StGQwUPKLlFW5W29rR1KRY6NYvMPL64KAuLMNcXwC74Qq6B_I4te5AtsgoFEpxKtKUkg0G0zzsXVpOmTWTC8P6QP9aLhBjv9AQ7qr0NoXAjyLs9t56EHQlOBLPQYS_4bPlgL3j8Zc38cimmOdjLg7G57F7i3mEsdSY4XM_GsCfXg1Q"; // Ersetze durch den vollständigen Token


        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken); // Alternativ zu "Authorization: Bearer <Token>"
        headers.setContentType(MediaType.APPLICATION_JSON); // Typ als MediaType


        //HttpHeaders headers = new HttpHeaders();
        //headers.set("Authorization", "Bearer " + jwtToken);
        //  headers.set("Content-Type", "application/json");


        String requestBody = """
                {
                    "name": "okcidento",
                    "streetname": "provluda strato",
                    "housenumber": "14",
                    "country_code": "eo",
                    "postalcode": "12345",
                    "city": "trial urbo"
                }
                """;


        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        //ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("Response: " + response.getBody());
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
        //return response.getBody();x
    }
}
*/