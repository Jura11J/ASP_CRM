package com.aspcrm.shop.service;

import com.aspcrm.shop.entity.ProductEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    EntityManager entityManager;
    @Mock
    TypedQuery<ProductEntity> productQuery;

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService();
        service.entityManager = entityManager;
    }

    @Test
    void search_BuildsQueryWithFiltersAndPaging() {
        ProductEntity p = new ProductEntity();
        p.setName("A");

        ArgumentCaptor<String> jpql = ArgumentCaptor.forClass(String.class);
        when(entityManager.createQuery(jpql.capture(), eq(ProductEntity.class))).thenReturn(productQuery);
        when(productQuery.setParameter(eq("q"), eq("abc"))).thenReturn(productQuery);
        when(productQuery.setFirstResult(anyInt())).thenReturn(productQuery);
        when(productQuery.setMaxResults(anyInt())).thenReturn(productQuery);
        when(productQuery.getResultList()).thenReturn(List.of(p));

        List<ProductEntity> result = service.search(" abc ", false, true, "priceDesc", 2, 10);

        assertEquals(1, result.size());
        assertTrue(jpql.getValue().contains("p.isActive = true"));
        assertTrue(jpql.getValue().contains("p.stockQuantity > 0"));
        assertTrue(jpql.getValue().contains("p.price DESC"));
        verify(productQuery).setFirstResult(20);
        verify(productQuery).setMaxResults(10);
    }

    @Test
    void findActiveById_ReturnsOptional() {
        ProductEntity p = new ProductEntity();
        p.setName("A");

        when(entityManager.createQuery(anyString(), eq(ProductEntity.class))).thenReturn(productQuery);
        when(productQuery.setParameter("id", 5)).thenReturn(productQuery);
        when(productQuery.getResultList()).thenReturn(List.of(p));

        Optional<ProductEntity> found = service.findActiveById(5);

        assertTrue(found.isPresent());
    }

    @Test
    void findById_DelegatesToEntityManager() {
        ProductEntity p = new ProductEntity();
        when(entityManager.find(ProductEntity.class, 7)).thenReturn(p);

        Optional<ProductEntity> found = service.findById(7);

        assertTrue(found.isPresent());
        assertSame(p, found.get());
    }
}
