package com.aspcrm.shop.web;

import com.aspcrm.shop.entity.ProductEntity;
import com.aspcrm.shop.service.ProductService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartBeanTest {
    @Mock
    ProductService productService;
    @Mock
    FacesContext facesContext;

    private CartBean bean;

    @BeforeEach
    void setUp() {
        bean = new CartBean();
        bean.productService = productService;
    }

    @Test
    void addProduct_ShowsWarningWhenUnavailable() {
        when(productService.findActiveById(1)).thenReturn(Optional.empty());

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.addProduct(1);

            assertTrue(bean.isEmpty());
            verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
        }
    }

    @Test
    void addProduct_AddsNewLineAndCapsQuantity() {
        ProductEntity product = new ProductEntity();
        com.aspcrm.shop.testutil.TestReflection.setField(product, "id", 1);
        product.setName("Prod");
        product.setSku("SKU");
        product.setPrice(new BigDecimal("10.00"));
        product.setStockQuantity(3);
        product.setActive(true);
        when(productService.findActiveById(1)).thenReturn(Optional.of(product));

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.addProduct(1, 5);
            bean.addProduct(1, 2);

            assertEquals(1, bean.getItems().size());
            assertEquals(3, bean.getItems().get(0).getQuantity());
            assertEquals(new BigDecimal("30.00"), bean.getTotal());
            assertEquals(3, bean.getCount());
        }
    }

    @Test
    void updateQuantity_RemoveAndCap() {
        ProductEntity product = new ProductEntity();
        com.aspcrm.shop.testutil.TestReflection.setField(product, "id", 1);
        product.setName("Prod");
        product.setSku("SKU");
        product.setPrice(BigDecimal.TEN);
        product.setStockQuantity(4);
        product.setActive(true);
        when(productService.findActiveById(1)).thenReturn(Optional.of(product));

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            bean.addProduct(1, 1);
        }

        bean.updateQuantity(1, 10);
        assertEquals(4, bean.getItems().get(0).getQuantity());

        bean.updateQuantity(1, 0);
        assertTrue(bean.isEmpty());
    }

    @Test
    void removeAndClear_WorkAsExpected() {
        ProductEntity product = new ProductEntity();
        com.aspcrm.shop.testutil.TestReflection.setField(product, "id", 1);
        product.setName("Prod");
        product.setSku("SKU");
        product.setPrice(BigDecimal.TEN);
        product.setStockQuantity(4);
        product.setActive(true);
        when(productService.findActiveById(1)).thenReturn(Optional.of(product));

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            bean.addProduct(1, 1);
        }

        bean.remove(1);
        assertTrue(bean.isEmpty());

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            bean.addProduct(1, 1);
        }

        bean.clear();
        assertTrue(bean.isEmpty());
    }
}
