package com.aspcrm.shop.web;

import com.aspcrm.shop.dto.ChatConversationDto;
import com.aspcrm.shop.dto.ChatMessageDto;
import com.aspcrm.shop.entity.CustomerEntity;
import com.aspcrm.shop.service.LiveChatService;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

@WebServlet(urlPatterns = "/api/chat/*")
public class ChatApiServlet extends HttpServlet {
    @Inject
    AuthBean authBean;
    @Inject
    LiveChatService liveChatService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CustomerEntity customer = requireCustomer(resp);
        if (customer == null) {
            return;
        }

        String path = req.getPathInfo() == null ? "/" : req.getPathInfo();
        try {
            if ("/conversation".equals(path)) {
                ChatConversationDto conversation = liveChatService.getConversationSummary(customer);
                writeJson(resp, Json.createObjectBuilder()
                        .add("conversation", toJson(conversation))
                        .build());
                return;
            }

            if ("/messages".equals(path)) {
                Integer conversationId = parseInt(req.getParameter("conversationId"));
                Integer afterId = parseInt(req.getParameter("afterId"));

                ChatConversationDto conversation = liveChatService.getConversationSummary(customer);
                if (conversationId == null) {
                    conversationId = conversation.getConversationId();
                }

                List<ChatMessageDto> messages = liveChatService.getMessages(customer, conversationId, afterId);
                conversation = liveChatService.getConversationSummary(customer);

                writeJson(resp, Json.createObjectBuilder()
                        .add("conversation", toJson(conversation))
                        .add("messages", toJson(messages))
                        .build());
                return;
            }

            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IllegalArgumentException ex) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Błąd serwera czatu");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CustomerEntity customer = requireCustomer(resp);
        if (customer == null) {
            return;
        }

        String path = req.getPathInfo() == null ? "/" : req.getPathInfo();
        try {
            if ("/messages".equals(path)) {
                String content = req.getParameter("content");
                ChatMessageDto message = liveChatService.sendCustomerMessage(customer, content);
                ChatConversationDto conversation = liveChatService.getConversationSummary(customer);

                writeJson(resp, Json.createObjectBuilder()
                        .add("conversation", toJson(conversation))
                        .add("message", toJson(message))
                        .build());
                return;
            }

            if ("/mark-read".equals(path)) {
                Integer conversationId = parseInt(req.getParameter("conversationId"));
                liveChatService.getMessages(customer, conversationId, Integer.MAX_VALUE);
                ChatConversationDto conversation = liveChatService.getConversationSummary(customer);
                writeJson(resp, Json.createObjectBuilder()
                        .add("conversation", toJson(conversation))
                        .build());
                return;
            }

            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IllegalArgumentException ex) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Błąd serwera czatu");
        }
    }

    private CustomerEntity requireCustomer(HttpServletResponse resp) throws IOException {
        if (authBean == null || !authBean.isLoggedIn() || authBean.getCurrent() == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        return authBean.getCurrent();
    }

    private Integer parseInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private JsonObject toJson(ChatConversationDto dto) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("conversationId", dto.getConversationId())
                .add("isClosed", dto.isClosed())
                .add("unreadCount", dto.getUnreadCount());

        if (dto.getLastMessageAt() != null) {
            builder.add("lastMessageAt", dto.getLastMessageAt());
        } else {
            builder.add("lastMessageAt", "");
        }

        if (dto.getLastMessagePreview() != null) {
            builder.add("lastMessagePreview", dto.getLastMessagePreview());
        } else {
            builder.add("lastMessagePreview", "");
        }

        return builder.build();
    }

    private JsonObject toJson(ChatMessageDto dto) {
        return Json.createObjectBuilder()
                .add("id", dto.getId())
                .add("conversationId", dto.getConversationId())
                .add("senderType", dto.getSenderType().name())
                .add("senderLabel", dto.getSenderLabel())
                .add("content", dto.getContent())
                .add("sentAt", dto.getSentAt() == null ? "" : dto.getSentAt())
                .add("own", dto.isOwn())
                .build();
    }

    private jakarta.json.JsonArray toJson(List<ChatMessageDto> messages) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (ChatMessageDto message : messages) {
            builder.add(toJson(message));
        }
        return builder.build();
    }

    private void writeJson(HttpServletResponse resp, JsonObject json) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter writer = Json.createWriter(stringWriter)) {
            writer.writeObject(json);
        }
        resp.getWriter().write(stringWriter.toString());
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        writeJson(resp, Json.createObjectBuilder()
                .add("error", message == null ? "Błąd" : message)
                .build());
    }
}
