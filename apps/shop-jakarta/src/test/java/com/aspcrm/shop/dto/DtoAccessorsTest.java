package com.aspcrm.shop.dto;

import com.aspcrm.shop.entity.ChatSenderType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DtoAccessorsTest {
    @Test
    void cartLine_AccessorsAndLineTotal() {
        CartLine line = new CartLine(1, "Product", "SKU", new BigDecimal("9.99"), 2, 5);

        assertEquals(1, line.getProductId());
        assertEquals("Product", line.getName());
        assertEquals("SKU", line.getSku());
        assertEquals(new BigDecimal("9.99"), line.getUnitPrice());
        assertEquals(2, line.getQuantity());
        assertEquals(5, line.getStockQuantity());
        assertEquals(new BigDecimal("19.98"), line.getLineTotal());

        line.setQuantity(3);
        assertEquals(3, line.getQuantity());
        assertEquals(new BigDecimal("29.97"), line.getLineTotal());
    }

    @Test
    void chatConversationDto_Accessors() {
        ChatConversationDto dto = new ChatConversationDto(12, true, "now", "preview", 4);

        assertEquals(12, dto.getConversationId());
        assertTrue(dto.isClosed());
        assertEquals("now", dto.getLastMessageAt());
        assertEquals("preview", dto.getLastMessagePreview());
        assertEquals(4, dto.getUnreadCount());
    }

    @Test
    void chatMessageDto_Accessors() {
        ChatMessageDto dto = new ChatMessageDto(7, 12, ChatSenderType.CRM_USER, "CRM", "Hello", "now", false);

        assertEquals(7, dto.getId());
        assertEquals(12, dto.getConversationId());
        assertEquals(ChatSenderType.CRM_USER, dto.getSenderType());
        assertEquals("CRM", dto.getSenderLabel());
        assertEquals("Hello", dto.getContent());
        assertEquals("now", dto.getSentAt());
        assertFalse(dto.isOwn());
    }
}
