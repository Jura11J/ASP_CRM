package com.aspcrm.shop.service;

import com.aspcrm.shop.dto.CartLine;
import com.aspcrm.shop.entity.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@RequestScoped
public class OrderService {
    @Inject
    EntityManager entityManager;

    @Inject
    CustomerService customerService;

    public OrderEntity placeOrder(String email, String firstName, String lastName, String phone,
                                  String address, String city, String preferredContact, boolean marketingConsent,
                                  List<CartLine> cartLines) {
        if (cartLines == null || cartLines.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();

            CustomerEntity customer = customerService.findOrCreate(email, firstName, lastName, phone, address, city, preferredContact, marketingConsent);

            OrderEntity order = new OrderEntity();
            order.setCustomer(customer);
            order.setStatus(OrderStatus.NEW);
            order.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

            BigDecimal total = BigDecimal.ZERO;
            for (CartLine line : cartLines) {
                ProductEntity product = entityManager.find(ProductEntity.class, line.getProductId());
                if (product == null || product.isDeleted()) {
                    throw new IllegalArgumentException("Product not available: " + line.getProductId());
                }

                OrderItemEntity item = new OrderItemEntity();
                item.setProduct(product);
                item.setQuantity(line.getQuantity());
                item.setUnitPrice(product.getPrice());
                BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
                item.setLineTotal(lineTotal);
                order.addItem(item);

                total = total.add(lineTotal);
            }
            order.setTotalAmount(total);

            OrderStatusHistoryEntity history = new OrderStatusHistoryEntity();
            history.setStatus(OrderStatus.NEW);
            history.setChangedAt(OffsetDateTime.now(ZoneOffset.UTC));
            history.setNote("Order placed from ShopFront");
            order.addStatusHistory(history);

            entityManager.persist(order);

            tx.commit();
            return order;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }
    // poprawić pobranie plus filtry na pobranych
    public List<OrderEntity> findOrders(String email, Integer orderId) {
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT o FROM OrderEntity o " +
                "LEFT JOIN FETCH o.items items " +
                "LEFT JOIN FETCH items.product prod " +
                "WHERE LOWER(o.customer.email) = LOWER(:email)");
        if (orderId != null) {
            jpql.append(" AND o.id = :orderId");
        }
        jpql.append(" ORDER BY o.createdAt DESC");

        TypedQuery<OrderEntity> query = entityManager.createQuery(jpql.toString(), OrderEntity.class);
        query.setParameter("email", email);
        if (orderId != null) {
            query.setParameter("orderId", orderId);
        }
        List<OrderEntity> result = new ArrayList<>(new LinkedHashSet<>(query.getResultList()));
        // initialize status history lazily to avoid multiple bag fetch exception
        result.forEach(o -> o.getStatusHistory().size());
        return result;
    }

    public Optional<OrderEntity> findOrderWithDetails(int orderId, String email) {
        String jpql = "SELECT DISTINCT o FROM OrderEntity o " +
                "LEFT JOIN FETCH o.items items " +
                "LEFT JOIN FETCH items.product prod " +
                "WHERE o.id = :orderId AND LOWER(o.customer.email) = LOWER(:email)";
        TypedQuery<OrderEntity> query = entityManager.createQuery(jpql, OrderEntity.class);
        query.setParameter("orderId", orderId);
        query.setParameter("email", email);
        Optional<OrderEntity> order = query.getResultList().stream().findFirst();
        order.ifPresent(o -> o.getStatusHistory().size());
        return order;
    }
}
