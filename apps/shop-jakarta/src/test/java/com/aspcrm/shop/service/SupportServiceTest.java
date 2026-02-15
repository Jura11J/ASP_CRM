package com.aspcrm.shop.service;

import com.aspcrm.shop.testutil.TestReflection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupportServiceTest {
    @Test
    void submitTicket_ReturnsFalseWhenRequestFails() {
        SupportService service = new SupportService();
        TestReflection.setField(service, "apiBase", "http://127.0.0.1:1/api");

        boolean result = service.submitTicket("a@b.pl", "A", "B", "1", "Title", "Desc", "high");

        assertFalse(result);
    }

    @Test
    void escape_ReplacesSpecialCharacters() {
        SupportService service = new SupportService();

        String escaped = TestReflection.invokePrivate(service, "escape", new Class<?>[]{String.class}, "a\\b\"c");
        String nullEscaped = TestReflection.invokePrivate(service, "escape", new Class<?>[]{String.class}, new Object[]{null});

        assertEquals("a\\\\b\\\"c", escaped);
        assertEquals("", nullEscaped);
    }
}
