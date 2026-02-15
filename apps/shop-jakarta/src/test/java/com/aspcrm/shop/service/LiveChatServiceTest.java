package com.aspcrm.shop.service;

import com.aspcrm.shop.dto.ChatConversationDto;
import com.aspcrm.shop.dto.ChatMessageDto;
import com.aspcrm.shop.entity.ChatConversationEntity;
import com.aspcrm.shop.entity.ChatMessageEntity;
import com.aspcrm.shop.entity.ChatSenderType;
import com.aspcrm.shop.entity.CustomerEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class LiveChatServiceTest {
    @Mock
    EntityManager entityManager;
    @Mock
    EntityTransaction transaction;
    @Mock
    TypedQuery<ChatConversationEntity> conversationQuery;
    @Mock
    TypedQuery<Long> countQuery;
    @Mock
    TypedQuery<ChatMessageEntity> unreadMessagesQuery;
    @Mock
    TypedQuery<ChatMessageEntity> fetchMessagesQuery;

    private LiveChatService service;

    @BeforeEach
    void setUp() {
        service = new LiveChatService();
        service.entityManager = entityManager;

        when(entityManager.getTransaction()).thenReturn(transaction);
    }

    @Test
    void getConversationSummary_UsesExistingConversation() {
        CustomerEntity customer = customer(10, "Jan", "Nowak");
        ChatConversationEntity conversation = conversation(5, customer.getId(), false);
        conversation.setLastMessagePreview("preview");

        when(entityManager.createQuery(contains("FROM ChatConversationEntity"), eq(ChatConversationEntity.class))).thenReturn(conversationQuery);
        when(conversationQuery.setParameter("customerId", customer.getId())).thenReturn(conversationQuery);
        when(conversationQuery.getResultList()).thenReturn(List.of(conversation));

        when(entityManager.createQuery(contains("SELECT COUNT"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(2L);

        ChatConversationDto dto = service.getConversationSummary(customer);

        assertEquals(5, dto.getConversationId());
        assertEquals(2, dto.getUnreadCount());
        assertEquals("preview", dto.getLastMessagePreview());
    }

    @Test
    void getMessages_MarksCrmMessagesAsRead_AndMapsDtos() {
        CustomerEntity customer = customer(10, "Jan", "Nowak");
        ChatConversationEntity conversation = conversation(5, customer.getId(), false);

        ChatMessageEntity unreadCrm = new ChatMessageEntity();
        com.aspcrm.shop.testutil.TestReflection.setField(unreadCrm, "id", 1);
        unreadCrm.setConversationId(5);
        unreadCrm.setSenderType(ChatSenderType.CRM_USER);
        unreadCrm.setContent("crm");
        unreadCrm.setReadByCustomer(false);

        ChatMessageEntity customerMessage = new ChatMessageEntity();
        com.aspcrm.shop.testutil.TestReflection.setField(customerMessage, "id", 2);
        customerMessage.setConversationId(5);
        customerMessage.setSenderType(ChatSenderType.CUSTOMER);
        customerMessage.setContent("hello");
        customerMessage.setSentAt(OffsetDateTime.now(ZoneOffset.UTC));

        when(entityManager.find(ChatConversationEntity.class, 5)).thenReturn(conversation);
        when(entityManager.createQuery(contains("FROM ChatMessageEntity"), eq(ChatMessageEntity.class)))
                .thenReturn(unreadMessagesQuery, fetchMessagesQuery);

        when(unreadMessagesQuery.setParameter(anyString(), any())).thenReturn(unreadMessagesQuery);
        when(unreadMessagesQuery.getResultList()).thenReturn(List.of(unreadCrm));

        when(fetchMessagesQuery.setParameter(anyString(), any())).thenReturn(fetchMessagesQuery);
        when(fetchMessagesQuery.getResultList()).thenReturn(List.of(customerMessage));

        List<ChatMessageDto> messages = service.getMessages(customer, 5, null);

        assertEquals(1, messages.size());
        assertTrue(unreadCrm.isReadByCustomer());
        verify(transaction).begin();
        verify(transaction).commit();
    }

    @Test
    void sendCustomerMessage_ThrowsForEmptyContent() {
        CustomerEntity customer = customer(1, "A", "B");

        assertThrows(IllegalArgumentException.class, () -> service.sendCustomerMessage(customer, "   "));
    }

    @Test
    void sendCustomerMessage_PersistsMessageAndReopensConversation() {
        CustomerEntity customer = customer(7, "Jan", "Nowak");
        ChatConversationEntity conversation = conversation(9, customer.getId(), true);

        when(entityManager.createQuery(contains("FROM ChatConversationEntity"), eq(ChatConversationEntity.class))).thenReturn(conversationQuery);
        when(conversationQuery.setParameter("customerId", customer.getId())).thenReturn(conversationQuery);
        when(conversationQuery.getResultList()).thenReturn(List.of(conversation));

        when(entityManager.find(ChatConversationEntity.class, 9)).thenReturn(conversation);

        ChatMessageDto dto = service.sendCustomerMessage(customer, "  test message  ");

        assertEquals(ChatSenderType.CUSTOMER, dto.getSenderType());
        assertTrue(dto.isOwn());
        assertFalse(conversation.isClosed());
        assertNotNull(conversation.getLastMessagePreview());
        verify(entityManager).persist(any(ChatMessageEntity.class));
        verify(transaction).begin();
        verify(transaction).commit();
    }

    @Test
    void requireConversation_ThrowsWhenCustomerMissing() {
        assertThrows(IllegalArgumentException.class, () -> service.requireConversation(null, 1));

        CustomerEntity customer = new CustomerEntity();
        assertThrows(IllegalArgumentException.class, () -> service.requireConversation(customer, 1));
    }

    @Test
    void requireConversation_ThrowsWhenConversationNotFoundOrOwnedByDifferentCustomer() {
        CustomerEntity customer = customer(3, "A", "B");
        ChatConversationEntity other = conversation(22, 5, false);

        when(entityManager.find(ChatConversationEntity.class, 11)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> service.requireConversation(customer, 11));

        when(entityManager.find(ChatConversationEntity.class, 11)).thenReturn(other);
        assertThrows(IllegalArgumentException.class, () -> service.requireConversation(customer, 11));
    }

    @Test
    void requireConversation_WhenIdNull_ReturnsExistingOrCreated() {
        CustomerEntity customer = customer(6, "A", "B");
        ChatConversationEntity existing = conversation(12, 6, false);

        when(entityManager.createQuery(contains("FROM ChatConversationEntity"), eq(ChatConversationEntity.class))).thenReturn(conversationQuery);
        when(conversationQuery.setParameter("customerId", 6)).thenReturn(conversationQuery);
        when(conversationQuery.getResultList()).thenReturn(List.of(existing));

        ChatConversationEntity result = service.requireConversation(customer, null);

        assertSame(existing, result);
    }

    private static CustomerEntity customer(int id, String firstName, String lastName) {
        CustomerEntity customer = new CustomerEntity();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(firstName.toLowerCase() + "@demo.pl");
        customer.setPhone("1");
        com.aspcrm.shop.testutil.TestReflection.setField(customer, "id", id);
        return customer;
    }

    private static ChatConversationEntity conversation(int id, int customerId, boolean closed) {
        ChatConversationEntity conversation = new ChatConversationEntity();
        com.aspcrm.shop.testutil.TestReflection.setField(conversation, "id", id);
        conversation.setCustomerId(customerId);
        conversation.setClosed(closed);
        conversation.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        conversation.setLastMessageAt(OffsetDateTime.now(ZoneOffset.UTC));
        return conversation;
    }
}

