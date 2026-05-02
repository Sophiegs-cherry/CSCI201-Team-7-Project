package com.moodtunes.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtTokenProvider unit tests — security-critical paths NOT covered elsewhere:
 *   - Token round-trip (generate → username extracted)
 *   - Validation accepts well-formed tokens
 *   - Validation rejects tampered signatures
 *   - Validation rejects expired tokens
 *   - Validation rejects garbage / null input
 *   - Tokens for different users are distinct
 */
class JwtTokenProviderTest {

    private static final String SECRET = "this-is-a-test-secret-that-is-at-least-32-bytes-long-for-hmac";
    private static final long EXPIRATION_MS = 60_000L;  // 1 min

    private JwtTokenProvider provider;

    @BeforeEach
    void setup() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", EXPIRATION_MS);
    }

    @Test
    void generateAndExtract_roundTripsUsername() {
        String token = provider.generateToken("alice");
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("alice", provider.getUsernameFromToken(token));
    }

    @Test
    void validateToken_acceptsWellFormedToken() {
        String token = provider.generateToken("alice");
        assertTrue(provider.validateToken(token));
    }

    @Test
    void validateToken_rejectsTamperedSignature() {
        String token = provider.generateToken("alice");
        // Flip the last character of the signature
        String tampered = token.substring(0, token.length() - 1)
                + (token.charAt(token.length() - 1) == 'A' ? 'B' : 'A');

        assertFalse(provider.validateToken(tampered),
                "Tampered token must fail validation");
    }

    @Test
    void validateToken_rejectsTokenSignedWithDifferentSecret() {
        // Token issued by a "rogue" provider with a different secret
        JwtTokenProvider rogue = new JwtTokenProvider();
        ReflectionTestUtils.setField(rogue, "jwtSecret",
                "totally-different-32-byte-secret-XXXXXXXXXXXXXXX");
        ReflectionTestUtils.setField(rogue, "jwtExpirationMs", EXPIRATION_MS);
        String foreignToken = rogue.generateToken("alice");

        assertFalse(provider.validateToken(foreignToken),
                "Token signed with wrong secret must be rejected");
    }

    @Test
    void validateToken_rejectsExpiredToken() throws InterruptedException {
        // 1ms expiration so the token is dead by the time we validate
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", 1L);
        String token = provider.generateToken("alice");
        Thread.sleep(50);

        assertFalse(provider.validateToken(token),
                "Expired token must be rejected");
    }

    @Test
    void validateToken_rejectsGarbage() {
        assertFalse(provider.validateToken("not.a.real.jwt"));
        assertFalse(provider.validateToken(""));
        assertFalse(provider.validateToken("xxx"));
    }

    @Test
    void validateToken_rejectsNullSafely() {
        // Should not throw; should return false
        assertFalse(provider.validateToken(null));
    }

    @Test
    void differentUsersGetDifferentTokens() {
        String tokenA = provider.generateToken("alice");
        String tokenB = provider.generateToken("bob");
        assertNotEquals(tokenA, tokenB);
        assertEquals("alice", provider.getUsernameFromToken(tokenA));
        assertEquals("bob", provider.getUsernameFromToken(tokenB));
    }
}
