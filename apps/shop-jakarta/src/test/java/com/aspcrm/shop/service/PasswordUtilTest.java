package com.aspcrm.shop.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {
    @Test
    void generateSalt_ReturnsBase64String() {
        String salt = PasswordUtil.generateSalt();

        assertNotNull(salt);
        assertFalse(salt.isBlank());
    }

    @Test
    void hash_IsDeterministicForSameInput() {
        String hash1 = PasswordUtil.hash("secret", "salt");
        String hash2 = PasswordUtil.hash("secret", "salt");

        assertEquals(hash1, hash2);
    }

    @Test
    void verify_ReturnsTrueForCorrectPassword() {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash("password", salt);

        assertTrue(PasswordUtil.verify("password", salt, hash));
        assertFalse(PasswordUtil.verify("bad", salt, hash));
    }
}
