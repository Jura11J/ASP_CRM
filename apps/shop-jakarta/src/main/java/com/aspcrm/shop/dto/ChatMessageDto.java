package com.aspcrm.shop.dto;

import com.aspcrm.shop.entity.ChatSenderType;

public class ChatMessageDto {
    private final Integer id;
    private final Integer conversationId;
    private final ChatSenderType senderType;
    private final String senderLabel;
    private final String content;
    private final String sentAt;
    private final boolean own;

    public ChatMessageDto(Integer id, Integer conversationId, ChatSenderType senderType, String senderLabel, String content, String sentAt, boolean own) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderType = senderType;
        this.senderLabel = senderLabel;
        this.content = content;
        this.sentAt = sentAt;
        this.own = own;
    }

    public Integer getId() {
        return id;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public ChatSenderType getSenderType() {
        return senderType;
    }

    public String getSenderLabel() {
        return senderLabel;
    }

    public String getContent() {
        return content;
    }

    public String getSentAt() {
        return sentAt;
    }

    public boolean isOwn() {
        return own;
    }
}
