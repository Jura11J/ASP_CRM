package com.aspcrm.shop.web;

import com.aspcrm.shop.dto.ChatConversationDto;
import com.aspcrm.shop.dto.ChatMessageDto;
import com.aspcrm.shop.entity.CustomerEntity;
import com.aspcrm.shop.service.LiveChatService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("chatBean")
@ViewScoped
public class ChatBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    AuthBean authBean;
    @Inject
    LiveChatService liveChatService;

    private ChatConversationDto conversation;
    private List<ChatMessageDto> messages = new ArrayList<>();

    @PostConstruct
    public void init() {
        if (!authBean.isLoggedIn()) {
            return;
        }
        CustomerEntity customer = authBean.getCurrent();
        try {
            conversation = liveChatService.getConversationSummary(customer);
            messages = liveChatService.getMessages(customer, conversation.getConversationId(), null);
            conversation = liveChatService.getConversationSummary(customer);
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Nie udało się załadować czatu", ex.getMessage()));
        }
    }

    public boolean isAvailable() {
        return authBean.isLoggedIn();
    }

    public Integer getConversationId() {
        return conversation != null ? conversation.getConversationId() : null;
    }

    public boolean isClosed() {
        return conversation != null && conversation.isClosed();
    }

    public int getUnreadCount() {
        return conversation != null ? conversation.getUnreadCount() : 0;
    }

    public String getLastMessageAt() {
        return conversation != null ? conversation.getLastMessageAt() : null;
    }

    public List<ChatMessageDto> getMessages() {
        return messages;
    }

    public int getLastMessageId() {
        return messages.isEmpty() ? 0 : messages.get(messages.size() - 1).getId();
    }
}
