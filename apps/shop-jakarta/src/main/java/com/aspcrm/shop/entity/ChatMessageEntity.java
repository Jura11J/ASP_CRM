package com.aspcrm.shop.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "ChatMessages")
public class ChatMessageEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "ConversationId", nullable = false)
    private Integer conversationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ConversationId", insertable = false, updatable = false)
    private ChatConversationEntity conversation;

    @Enumerated(EnumType.ORDINAL)
    @JdbcTypeCode(SqlTypes.INTEGER)
    @Column(name = "SenderType", nullable = false)
    private ChatSenderType senderType;

    @Column(name = "SenderCrmUserId", length = 450)
    private String senderCrmUserId;

    @Column(name = "Content", nullable = false, length = 2000)
    private String content;

    @Column(name = "SentAt", nullable = false)
    private OffsetDateTime sentAt;

    @Column(name = "IsReadByCustomer", nullable = false)
    private boolean isReadByCustomer;

    @Column(name = "IsReadByCrm", nullable = false)
    private boolean isReadByCrm;

    @PrePersist
    public void prePersist() {
        if (sentAt == null) {
            sentAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public Integer getId() {
        return id;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    public ChatConversationEntity getConversation() {
        return conversation;
    }

    public ChatSenderType getSenderType() {
        return senderType;
    }

    public void setSenderType(ChatSenderType senderType) {
        this.senderType = senderType;
    }

    public String getSenderCrmUserId() {
        return senderCrmUserId;
    }

    public void setSenderCrmUserId(String senderCrmUserId) {
        this.senderCrmUserId = senderCrmUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(OffsetDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isReadByCustomer() {
        return isReadByCustomer;
    }

    public void setReadByCustomer(boolean readByCustomer) {
        isReadByCustomer = readByCustomer;
    }

    public boolean isReadByCrm() {
        return isReadByCrm;
    }

    public void setReadByCrm(boolean readByCrm) {
        isReadByCrm = readByCrm;
    }
}
