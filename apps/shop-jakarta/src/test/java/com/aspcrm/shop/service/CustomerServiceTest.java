package com.aspcrm.shop.service;

import com.aspcrm.shop.entity.CustomerEntity;
import com.aspcrm.shop.entity.OrderEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock
    EntityManager entityManager;
    @Mock
    TypedQuery<CustomerEntity> customerQuery;

    private CustomerService service;

    @BeforeEach
    void setUp() {
        service = new CustomerService();
        service.entityManager = entityManager;
    }

    @Test
    void findByEmail_ReturnsCustomerWhenExists() {
        CustomerEntity customer = new CustomerEntity();
        customer.setEmail("john@demo.pl");

        when(entityManager.createQuery(anyString(), eq(CustomerEntity.class))).thenReturn(customerQuery);
        when(customerQuery.setParameter(eq("email"), eq("john@demo.pl"))).thenReturn(customerQuery);
        when(customerQuery.getResultList()).thenReturn(List.of(customer));

        Optional<CustomerEntity> result = service.findByEmail("john@demo.pl");

        assertTrue(result.isPresent());
        assertEquals("john@demo.pl", result.get().getEmail());
    }

    @Test
    void updateCustomer_UpdatesOnlyProvidedValues() {
        CustomerEntity customer = new CustomerEntity();
        customer.setFirstName("Old");
        customer.setLastName("Name");

        CustomerEntity updated = service.updateCustomer(customer,
                "New", "Surname", "555", "Street", "City", "Phone", true);

        assertSame(customer, updated);
        assertEquals("New", updated.getFirstName());
        assertEquals("Surname", updated.getLastName());
        assertEquals("555", updated.getPhone());
        assertTrue(updated.isMarketingConsent());
        assertTrue(updated.isActive());
    }

    @Test
    void softDeleteCustomer_ReturnsFalseWhenHasOrders() {
        CustomerEntity customer = new CustomerEntity();
        customer.getOrders().add(new OrderEntity());

        boolean result = service.softDeleteCustomer(customer);

        assertFalse(result);
        assertFalse(customer.isDeleted());
    }

    @Test
    void softDeleteCustomer_ReturnsTrueWhenNoOrders() {
        CustomerEntity customer = new CustomerEntity();

        boolean result = service.softDeleteCustomer(customer);

        assertTrue(result);
        assertTrue(customer.isDeleted());
        assertFalse(customer.isActive());
    }

    @Test
    void findOrCreate_ReturnsExistingCustomerWhenFound() {
        CustomerEntity existing = new CustomerEntity();
        existing.setEmail("x@demo.pl");

        when(entityManager.createQuery(anyString(), eq(CustomerEntity.class))).thenReturn(customerQuery);
        when(customerQuery.setParameter(eq("email"), eq("x@demo.pl"))).thenReturn(customerQuery);
        when(customerQuery.getResultList()).thenReturn(List.of(existing));

        CustomerEntity result = service.findOrCreate("x@demo.pl", "A", "B", "1", "Adr", "City", "Email", true);

        assertSame(existing, result);
        verify(entityManager, never()).persist(any(CustomerEntity.class));
    }

    @Test
    void findOrCreate_CreatesAndPersistsWhenMissing() {
        when(entityManager.createQuery(anyString(), eq(CustomerEntity.class))).thenReturn(customerQuery);
        when(customerQuery.setParameter(eq("email"), eq("new@demo.pl"))).thenReturn(customerQuery);
        when(customerQuery.getResultList()).thenReturn(List.of());

        CustomerEntity created = service.findOrCreate("new@demo.pl", "A", "B", "1", "Adr", "City", "Phone", false);

        assertEquals("new@demo.pl", created.getEmail());
        assertEquals("A", created.getFirstName());
        verify(entityManager).persist(created);
    }
}
