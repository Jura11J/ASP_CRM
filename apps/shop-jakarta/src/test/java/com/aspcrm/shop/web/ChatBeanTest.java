package com.aspcrm.shop.web;

import com.aspcrm.shop.dto.ChatConversationDto;
import com.aspcrm.shop.dto.ChatMessageDto;
import com.aspcrm.shop.entity.ChatSenderType;
import com.aspcrm.shop.entity.CustomerEntity;
import com.aspcrm.shop.service.LiveChatService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatBeanTest {
    @Mock
    AuthBean authBean;
    @Mock
    LiveChatService liveChatService;
    @Mock
    FacesContext facesContext;

    private ChatBean bean;

    @BeforeEach
    void setUp() {
        bean = new ChatBean();
        bean.authBean = authBean;
        bean.liveChatService = liveChatService;
    }

    @Test
    void init_DoesNothingWhenUserNotLoggedIn() {
        when(authBean.isLoggedIn()).thenReturn(false);

        bean.init();

        verify(liveChatService, never()).getConversationSummary(any());
    }

    @Test
    void init_LoadsConversationAndMessages() {
        CustomerEntity customer = new CustomerEntity();
        customer.setFirstName("Jan");
        customer.setLastName("Nowak");

        ChatConversationDto conversation = new ChatConversationDto(10, false, "now", "preview", 1);
        ChatMessageDto message = new ChatMessageDto(5, 10, ChatSenderType.CUSTOMER, "Jan Nowak", "Hi", "now", true);

        when(authBean.isLoggedIn()).thenReturn(true);
        when(authBean.getCurrent()).thenReturn(customer);
        when(liveChatService.getConversationSummary(customer)).thenReturn(conversation);
        when(liveChatService.getMessages(customer, 10, null)).thenReturn(List.of(message));

        bean.init();

        assertTrue(bean.isAvailable());
        assertEquals(10, bean.getConversationId());
        assertEquals(1, bean.getMessages().size());
        assertEquals(5, bean.getLastMessageId());
    }

    @Test
    void init_ShowsErrorWhenServiceFails() {
        CustomerEntity customer = new CustomerEntity();
        when(authBean.isLoggedIn()).thenReturn(true);
        when(authBean.getCurrent()).thenReturn(customer);
        when(liveChatService.getConversationSummary(customer)).thenThrow(new RuntimeException("boom"));

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.init();

            verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
        }
    }

    @Test
    void getters_ReturnDefaultValuesWithoutConversation() {
        when(authBean.isLoggedIn()).thenReturn(false);

        assertFalse(bean.isAvailable());
        assertNull(bean.getConversationId());
        assertFalse(bean.isClosed());
        assertEquals(0, bean.getUnreadCount());
        assertNull(bean.getLastMessageAt());
        assertEquals(0, bean.getLastMessageId());
    }
}
