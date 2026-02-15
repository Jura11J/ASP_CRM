package com.aspcrm.shop.service;

import com.aspcrm.shop.dto.ChatConversationDto;
import com.aspcrm.shop.dto.ChatMessageDto;
import com.aspcrm.shop.entity.ChatConversationEntity;
import com.aspcrm.shop.entity.ChatMessageEntity;
import com.aspcrm.shop.entity.ChatSenderType;
import com.aspcrm.shop.entity.CustomerEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@RequestScoped
public class LiveChatService {
    @Inject
    EntityManager entityManager;

    public ChatConversationDto getConversationSummary(CustomerEntity customer) {
        ChatConversationEntity conversation = getOrCreateConversation(customer);
        int unread = countUnreadForCustomer(conversation.getId());
        return new ChatConversationDto(
                conversation.getId(),
                conversation.isClosed(),
                conversation.getLastMessageAt() != null ? conversation.getLastMessageAt().toString() : null,
                conversation.getLastMessagePreview(),
                unread);
    }

    public List<ChatMessageDto> getMessages(CustomerEntity customer, Integer conversationId, Integer afterId) {
        ChatConversationEntity conversation = requireConversation(customer, conversationId);

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            conversation = entityManager.find(ChatConversationEntity.class, conversation.getId());
            markCrmMessagesAsRead(conversation.getId());
            List<ChatMessageEntity> messages = fetchMessages(conversation.getId(), afterId);
            tx.commit();
            return messages.stream()
                    .map(m -> mapToDto(m, customer))
                    .toList();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    public ChatMessageDto sendCustomerMessage(CustomerEntity customer, String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Wiadomość nie może być pusta");
        }

        ChatConversationEntity conversation = getOrCreateConversation(customer);
        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            conversation = entityManager.find(ChatConversationEntity.class, conversation.getId());
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

            if (conversation.isClosed()) {
                conversation.setClosed(false);
                conversation.setClosedAt(null);
            }

            ChatMessageEntity message = new ChatMessageEntity();
            message.setConversationId(conversation.getId());
            message.setSenderType(ChatSenderType.CUSTOMER);
            message.setContent(trimmed);
            message.setSentAt(now);
            message.setReadByCustomer(true);
            message.setReadByCrm(false);

            entityManager.persist(message);

            conversation.setLastMessageAt(now);
            conversation.setLastMessagePreview(buildPreview(trimmed));

            tx.commit();
            return mapToDto(message, customer);
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    public ChatConversationEntity requireConversation(CustomerEntity customer, Integer conversationId) {
        if (customer == null || customer.getId() == null) {
            throw new IllegalArgumentException("Klient nie jest zalogowany");
        }

        ChatConversationEntity conversation;
        if (conversationId != null) {
            conversation = entityManager.find(ChatConversationEntity.class, conversationId);
            if (conversation == null || !customer.getId().equals(conversation.getCustomerId())) {
                throw new IllegalArgumentException("Nie znaleziono konwersacji");
            }
        } else {
            conversation = getOrCreateConversation(customer);
        }

        return conversation;
    }

    private ChatConversationEntity getOrCreateConversation(CustomerEntity customer) {
        Optional<ChatConversationEntity> existing = findConversationByCustomer(customer.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            ChatConversationEntity conversation = new ChatConversationEntity();
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            conversation.setCustomerId(customer.getId());
            conversation.setCreatedAt(now);
            conversation.setLastMessageAt(now);
            conversation.setClosed(false);
            entityManager.persist(conversation);
            tx.commit();
            return conversation;
        } catch (PersistenceException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            // możliwy wyścig przy unikalnym CustomerId
            return findConversationByCustomer(customer.getId())
                    .orElseThrow(() -> ex);
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    private Optional<ChatConversationEntity> findConversationByCustomer(Integer customerId) {
        TypedQuery<ChatConversationEntity> query = entityManager.createQuery(
                "SELECT c FROM ChatConversationEntity c WHERE c.customerId = :customerId",
                ChatConversationEntity.class);
        query.setParameter("customerId", customerId);
        return query.getResultList().stream().findFirst();
    }

    private int countUnreadForCustomer(Integer conversationId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(m) FROM ChatMessageEntity m WHERE m.conversationId = :conversationId AND m.senderType = :senderType AND m.isReadByCustomer = false",
                        Long.class)
                .setParameter("conversationId", conversationId)
                .setParameter("senderType", ChatSenderType.CRM_USER)
                .getSingleResult();
        return count == null ? 0 : count.intValue();
    }

    private void markCrmMessagesAsRead(Integer conversationId) {
        List<ChatMessageEntity> unread = entityManager.createQuery(
                        "SELECT m FROM ChatMessageEntity m WHERE m.conversationId = :conversationId AND m.senderType = :senderType AND m.isReadByCustomer = false",
                        ChatMessageEntity.class)
                .setParameter("conversationId", conversationId)
                .setParameter("senderType", ChatSenderType.CRM_USER)
                .getResultList();

        for (ChatMessageEntity message : unread) {
            message.setReadByCustomer(true);
        }
    }

    private List<ChatMessageEntity> fetchMessages(Integer conversationId, Integer afterId) {
        String jpql = "SELECT m FROM ChatMessageEntity m WHERE m.conversationId = :conversationId" +
                (afterId != null ? " AND m.id > :afterId" : "") +
                " ORDER BY m.id ASC";
        TypedQuery<ChatMessageEntity> query = entityManager.createQuery(jpql, ChatMessageEntity.class);
        query.setParameter("conversationId", conversationId);
        if (afterId != null) {
            query.setParameter("afterId", afterId);
        }
        return query.getResultList();
    }

    private ChatMessageDto mapToDto(ChatMessageEntity message, CustomerEntity customer) {
        boolean own = message.getSenderType() == ChatSenderType.CUSTOMER;
        String senderLabel = own
                ? customer.getFirstName() + " " + customer.getLastName()
                : "CRM";
        return new ChatMessageDto(
                message.getId(),
                message.getConversationId(),
                message.getSenderType(),
                senderLabel,
                message.getContent(),
                message.getSentAt() != null ? message.getSentAt().toString() : null,
                own);
    }

    private String buildPreview(String content) {
        int maxLength = 160;
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "…";
    }
}
