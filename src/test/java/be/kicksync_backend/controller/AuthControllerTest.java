package be.kicksync_backend.controller;

import be.kicksync_backend.dto.LoginRequest;
import be.kicksync_backend.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testPublicEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/test/public", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("This is a public endpoint - no authentication required", response.getBody());
    }

    @Test
    void testRegisterUser() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/register", registerRequest, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testProtectedEndpointWithoutAuth() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/test/protected", String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}