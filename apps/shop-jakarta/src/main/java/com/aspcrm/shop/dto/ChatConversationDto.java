package com.aspcrm.shop.dto;

public class ChatConversationDto {
    private final Integer conversationId;
    private final boolean closed;
    private final String lastMessageAt;
    private final String lastMessagePreview;
    private final int unreadCount;

    public ChatConversationDto(Integer conversationId, boolean closed, String lastMessageAt, String lastMessagePreview, int unreadCount) {
        this.conversationId = conversationId;
        this.closed = closed;
        this.lastMessageAt = lastMessageAt;
        this.lastMessagePreview = lastMessagePreview;
        this.unreadCount = unreadCount;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public boolean isClosed() {
        return closed;
    }

    public String getLastMessageAt() {
        return lastMessageAt;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public int getUnreadCount() {
        return unreadCount;
    }
}
