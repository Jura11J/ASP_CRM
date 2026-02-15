package com.aspcrm.shop.service;

import com.aspcrm.shop.dto.CartLine;
import com.aspcrm.shop.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class OrderServiceTest {
    @Mock
    EntityManager entityManager;
    @Mock
    EntityTransaction transaction;
    @Mock
    CustomerService customerService;
    @Mock
    TypedQuery<OrderEntity> orderQuery;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService();
        service.entityManager = entityManager;
        service.customerService = customerService;

        when(entityManager.getTransaction()).thenReturn(transaction);
    }

    @Test
    void placeOrder_ThrowsWhenCartEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> service.placeOrder("a@b.pl", "A", "B", "1", "Adr", "City", "Email", true, List.of()));
    }

    @Test
    void placeOrder_ThrowsAndRollsBackWhenProductMissing() {
        CustomerEntity customer = new CustomerEntity();
        customer.setEmail("a@b.pl");

        when(customerService.findOrCreate(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(customer);
        when(entityManager.find(ProductEntity.class, 1)).thenReturn(null);
        when(transaction.isActive()).thenReturn(true);

        List<CartLine> lines = List.of(new CartLine(1, "P", "SKU", BigDecimal.TEN, 2, 5));

        assertThrows(IllegalArgumentException.class,
                () -> service.placeOrder("a@b.pl", "A", "B", "1", "Adr", "City", "Email", true, lines));

        verify(transaction).begin();
        verify(transaction).rollback();
    }

    @Test
    void placeOrder_PersistsOrderAndCommits() {
        CustomerEntity customer = new CustomerEntity();
        customer.setEmail("a@b.pl");

        ProductEntity product = new ProductEntity();
        product.setName("Prod");
        product.setPrice(new BigDecimal("12.50"));
        product.setDeleted(false);

        when(customerService.findOrCreate(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(customer);
        when(entityManager.find(ProductEntity.class, 1)).thenReturn(product);

        List<CartLine> lines = List.of(new CartLine(1, "P", "SKU", BigDecimal.TEN, 2, 5));

        OrderEntity order = service.placeOrder("a@b.pl", "A", "B", "1", "Adr", "City", "Email", true, lines);

        assertEquals(OrderStatus.NEW, order.getStatus());
        assertEquals(new BigDecimal("25.00"), order.getTotalAmount());
        assertEquals(1, order.getItems().size());
        assertEquals(1, order.getStatusHistory().size());

        verify(entityManager).persist(order);
        verify(transaction).commit();
    }

    @Test
    void findOrders_ReturnsDistinctResults_AndInitializesStatusHistory() {
        OrderEntity order = new OrderEntity();
        order.addStatusHistory(new OrderStatusHistoryEntity());

        when(entityManager.createQuery(anyString(), eq(OrderEntity.class))).thenReturn(orderQuery);
        when(orderQuery.setParameter(eq("email"), eq("user@demo.pl"))).thenReturn(orderQuery);
        when(orderQuery.getResultList()).thenReturn(List.of(order, order));

        List<OrderEntity> result = service.findOrders("user@demo.pl", null);

        assertEquals(1, result.size());
        verify(orderQuery, never()).setParameter(eq("orderId"), any());
    }

    @Test
    void findOrders_AppliesOrderIdFilterWhenProvided() {
        when(entityManager.createQuery(anyString(), eq(OrderEntity.class))).thenReturn(orderQuery);
        when(orderQuery.setParameter(anyString(), any())).thenReturn(orderQuery);
        when(orderQuery.getResultList()).thenReturn(List.of());

        service.findOrders("user@demo.pl", 15);

        verify(orderQuery).setParameter("orderId", 15);
    }

    @Test
    void findOrderWithDetails_ReturnsOptionalOrder() {
        OrderEntity order = new OrderEntity();
        order.addStatusHistory(new OrderStatusHistoryEntity());

        when(entityManager.createQuery(anyString(), eq(OrderEntity.class))).thenReturn(orderQuery);
        when(orderQuery.setParameter(anyString(), any())).thenReturn(orderQuery);
        when(orderQuery.getResultList()).thenReturn(List.of(order));

        Optional<OrderEntity> result = service.findOrderWithDetails(1, "u@d.pl");

        assertTrue(result.isPresent());
    }
}
