package com.aspcrm.shop.web;

import com.aspcrm.shop.dto.ChatConversationDto;
import com.aspcrm.shop.dto.ChatMessageDto;
import com.aspcrm.shop.entity.ChatSenderType;
import com.aspcrm.shop.entity.CustomerEntity;
import com.aspcrm.shop.service.LiveChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatApiServletTest {
    @Mock
    AuthBean authBean;
    @Mock
    LiveChatService liveChatService;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;

    private TestableChatApiServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new TestableChatApiServlet();
        servlet.authBean = authBean;
        servlet.liveChatService = liveChatService;
    }

    @Test
    void doGet_ReturnsUnauthorizedWhenNoCustomer() throws Exception {
        when(authBean.isLoggedIn()).thenReturn(false);

        servlet.invokeGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void doGet_Conversation_ReturnsJson() throws Exception {
        CustomerEntity customer = new CustomerEntity();
        when(authBean.isLoggedIn()).thenReturn(true);
        when(authBean.getCurrent()).thenReturn(customer);
        when(request.getPathInfo()).thenReturn("/conversation");
        when(liveChatService.getConversationSummary(customer)).thenReturn(new ChatConversationDto(10, false, null, "preview", 0));

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw, true));

        servlet.invokeGet(request, response);

        assertTrue(sw.toString().contains("\"conversationId\":10"));
        verify(response).setContentType("application/json;charset=UTF-8");
    }

    @Test
    void doGet_Messages_UsesProvidedConversationId() throws Exception {
        CustomerEntity customer = new CustomerEntity();
        when(authBean.isLoggedIn()).thenReturn(true);
        when(authBean.getCurrent()).thenReturn(customer);
        when(request.getPathInfo()).thenReturn("/messages");
        when(request.getParameter("conversationId")).thenReturn("10");
        when(request.getParameter("afterId")).thenReturn("2");

        ChatConversationDto summary = new ChatConversationDto(10, false, null, "preview", 0);
        ChatMessageDto message = new ChatMessageDto(3, 10, ChatSenderType.CUSTOMER, "C", "Hi", "now", true);
        when(liveChatService.getConversationSummary(customer)).thenReturn(summary);
        when(liveChatService.getMessages(customer, 10, 2)).thenReturn(List.of(message));

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw, true));

        servlet.invokeGet(request, response);

        assertTrue(sw.toString().contains("\"messages\""));
        verify(liveChatService).getMessages(customer, 10, 2);
    }

    @Test
    void doPost_Messages_ReturnsCreatedMessageJson() throws Exception {
        CustomerEntity customer = new CustomerEntity();
        when(authBean.isLoggedIn()).thenReturn(true);
        when(authBean.getCurrent()).thenReturn(customer);
        when(request.getPathInfo()).thenReturn("/messages");
        when(request.getParameter("content")).thenReturn("hello");

        ChatMessageDto message = new ChatMessageDto(5, 10, ChatSenderType.CUSTOMER, "C", "hello", "now", true);
        ChatConversationDto summary = new ChatConversationDto(10, false, "now", "hello", 0);
        when(liveChatService.sendCustomerMessage(customer, "hello")).thenReturn(message);
        when(liveChatService.getConversationSummary(customer)).thenReturn(summary);

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw, true));

        servlet.invokePost(request, response);

        assertTrue(sw.toString().contains("\"message\""));
    }

    @Test
    void doPost_MarkRead_UsesMaxIntegerAfterId() throws Exception {
        CustomerEntity customer = new CustomerEntity();
        when(authBean.isLoggedIn()).thenReturn(true);
        when(authBean.getCurrent()).thenReturn(customer);
        when(request.getPathInfo()).thenReturn("/mark-read");
        when(request.getParameter("conversationId")).thenReturn("10");
        when(liveChatService.getConversationSummary(customer)).thenReturn(new ChatConversationDto(10, false, null, null, 0));

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw, true));

        servlet.invokePost(request, response);

        verify(liveChatService).getMessages(customer, 10, Integer.MAX_VALUE);
    }

    @Test
    void doGet_ReturnsBadRequestForDomainError() throws Exception {
        CustomerEntity customer = new CustomerEntity();
        when(authBean.isLoggedIn()).thenReturn(true);
        when(authBean.getCurrent()).thenReturn(customer);
        when(request.getPathInfo()).thenReturn("/conversation");
        when(liveChatService.getConversationSummary(customer)).thenThrow(new IllegalArgumentException("bad"));

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw, true));

        servlet.invokeGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(sw.toString().contains("bad"));
    }

    private static class TestableChatApiServlet extends ChatApiServlet {
        void invokeGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
            super.doGet(request, response);
        }

        void invokePost(HttpServletRequest request, HttpServletResponse response) throws Exception {
            super.doPost(request, response);
        }
    }
}
