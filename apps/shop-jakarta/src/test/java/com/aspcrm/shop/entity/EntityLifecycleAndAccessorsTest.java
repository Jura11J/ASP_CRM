package com.aspcrm.shop.entity;

import com.aspcrm.shop.testutil.TestReflection;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityLifecycleAndAccessorsTest {
    @Test
    void customerEntity_PrePersistAndAccessors() {
        CustomerEntity customer = new CustomerEntity();
        customer.setFirstName("Jan");
        customer.setLastName("Nowak");
        customer.setEmail("jan@demo.pl");
        customer.setPhone("123");
        customer.setAddressLine1("Street");
        customer.setCity("City");
        customer.setPreferredContactMethod("Email");
        customer.setMarketingConsent(true);
        customer.setActive(false);
        customer.setDeleted(true);

        customer.prePersist();

        assertNotNull(customer.getCreatedAt());
        assertEquals("Jan", customer.getFirstName());
        assertEquals("Nowak", customer.getLastName());
        assertEquals("jan@demo.pl", customer.getEmail());
        assertEquals("123", customer.getPhone());
        assertEquals("Street", customer.getAddressLine1());
        assertEquals("City", customer.getCity());
        assertEquals("Email", customer.getPreferredContactMethod());
        assertTrue(customer.isMarketingConsent());
        assertTrue(customer.isActive());
        assertFalse(customer.isDeleted());
        assertNotNull(customer.getOrders());
        assertNotNull(customer.getChatConversations());
    }

    @Test
    void productEntity_Accessors() {
        ProductEntity product = new ProductEntity();
        TestReflection.setField(product, "id", 5);
        product.setName("Prod");
        product.setSku("SKU");
        product.setPrice(new BigDecimal("10.50"));
        product.setStockQuantity(7);
        product.setActive(true);
        product.setDeleted(false);
        product.setDescription("Desc");

        assertEquals(5, product.getId());
        assertEquals("Prod", product.getName());
        assertEquals("SKU", product.getSku());
        assertEquals(new BigDecimal("10.50"), product.getPrice());
        assertEquals(7, product.getStockQuantity());
        assertTrue(product.isActive());
        assertFalse(product.isDeleted());
        assertEquals("Desc", product.getDescription());
    }

    @Test
    void orderEntity_PrePersistAddItemAndHistory() {
        OrderEntity order = new OrderEntity();
        CustomerEntity customer = new CustomerEntity();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(BigDecimal.TEN);

        OrderItemEntity item = new OrderItemEntity();
        ProductEntity product = new ProductEntity();
        item.setProduct(product);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("5.00"));
        item.setLineTotal(BigDecimal.TEN);
        order.addItem(item);

        OrderStatusHistoryEntity history = new OrderStatusHistoryEntity();
        history.setStatus(OrderStatus.PAID);
        history.setNote("ok");
        order.addStatusHistory(history);

        order.prePersist();

        assertNotNull(order.getCreatedAt());
        assertEquals(customer, order.getCustomer());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(BigDecimal.TEN, order.getTotalAmount());
        assertEquals(1, order.getItems().size());
        assertEquals(order, order.getItems().get(0).getOrder());
        assertEquals(1, order.getStatusHistory().size());
        assertEquals(order, order.getStatusHistory().get(0).getOrder());
    }

    @Test
    void orderItemEntity_Accessors() {
        OrderItemEntity item = new OrderItemEntity();
        TestReflection.setField(item, "id", 3);
        OrderEntity order = new OrderEntity();
        ProductEntity product = new ProductEntity();

        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(3);
        item.setUnitPrice(new BigDecimal("4.00"));
        item.setLineTotal(new BigDecimal("12.00"));

        assertEquals(3, item.getId());
        assertEquals(order, item.getOrder());
        assertEquals(product, item.getProduct());
        assertEquals(3, item.getQuantity());
        assertEquals(new BigDecimal("4.00"), item.getUnitPrice());
        assertEquals(new BigDecimal("12.00"), item.getLineTotal());
    }

    @Test
    void orderStatusHistoryEntity_PrePersistAndAccessors() {
        OrderStatusHistoryEntity history = new OrderStatusHistoryEntity();
        TestReflection.setField(history, "id", 2);
        OrderEntity order = new OrderEntity();

        history.setOrder(order);
        history.setStatus(OrderStatus.SHIPPED);
        history.setNote("shipped");
        history.prePersist();

        assertEquals(2, history.getId());
        assertEquals(order, history.getOrder());
        assertEquals(OrderStatus.SHIPPED, history.getStatus());
        assertNotNull(history.getChangedAt());
        assertEquals("shipped", history.getNote());
    }

    @Test
    void shopUser_PrePersistAndAccessors() {
        ShopUser user = new ShopUser();
        TestReflection.setField(user, "id", 7);
        CustomerEntity customer = new CustomerEntity();

        user.setEmail("u@d.pl");
        user.setPasswordSalt("salt");
        user.setPasswordHash("hash");
        user.setCustomer(customer);
        user.prePersist();

        assertEquals(7, user.getId());
        assertEquals("u@d.pl", user.getEmail());
        assertEquals("salt", user.getPasswordSalt());
        assertEquals("hash", user.getPasswordHash());
        assertNotNull(user.getCreatedAt());
        assertEquals(customer, user.getCustomer());
    }

    @Test
    void chatConversation_PrePersistAndAccessors() {
        ChatConversationEntity conversation = new ChatConversationEntity();
        TestReflection.setField(conversation, "id", 9);
        CustomerEntity customer = new CustomerEntity();
        TestReflection.setField(conversation, "customer", customer);

        conversation.setCustomerId(10);
        conversation.setLastMessagePreview("preview");
        conversation.setClosed(true);
        OffsetDateTime closedAt = OffsetDateTime.now();
        conversation.setClosedAt(closedAt);
        conversation.prePersist();

        assertEquals(9, conversation.getId());
        assertEquals(10, conversation.getCustomerId());
        assertEquals(customer, conversation.getCustomer());
        assertNotNull(conversation.getCreatedAt());
        assertNotNull(conversation.getLastMessageAt());
        assertEquals("preview", conversation.getLastMessagePreview());
        assertTrue(conversation.isClosed());
        assertEquals(closedAt, conversation.getClosedAt());
        assertNotNull(conversation.getMessages());
    }

    @Test
    void chatMessage_PrePersistAndAccessors() {
        ChatMessageEntity message = new ChatMessageEntity();
        TestReflection.setField(message, "id", 4);
        ChatConversationEntity conversation = new ChatConversationEntity();
        TestReflection.setField(message, "conversation", conversation);

        message.setConversationId(11);
        message.setSenderType(ChatSenderType.CUSTOMER);
        message.setSenderCrmUserId("crm1");
        message.setContent("hello");
        message.setReadByCustomer(true);
        message.setReadByCrm(false);
        message.prePersist();

        assertEquals(4, message.getId());
        assertEquals(11, message.getConversationId());
        assertEquals(conversation, message.getConversation());
        assertEquals(ChatSenderType.CUSTOMER, message.getSenderType());
        assertEquals("crm1", message.getSenderCrmUserId());
        assertEquals("hello", message.getContent());
        assertNotNull(message.getSentAt());
        assertTrue(message.isReadByCustomer());
        assertFalse(message.isReadByCrm());
    }
}
