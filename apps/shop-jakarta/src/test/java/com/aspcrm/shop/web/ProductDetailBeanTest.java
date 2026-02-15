package com.aspcrm.shop.web;

import com.aspcrm.shop.entity.ProductEntity;
import com.aspcrm.shop.service.ProductService;
import com.aspcrm.shop.testutil.TestReflection;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductDetailBeanTest {
    @Mock
    ProductService productService;
    @Mock
    CartBean cartBean;
    @Mock
    FacesContext facesContext;
    @Mock
    ExternalContext externalContext;

    private ProductDetailBean bean;

    @BeforeEach
    void setUp() {
        bean = new ProductDetailBean();
        bean.productService = productService;
        bean.cartBean = cartBean;
    }

    @Test
    void load_DoesNothingWhenProductIdMissing() throws IOException {
        bean.load();

        verify(productService, never()).findActiveById(anyInt());
    }

    @Test
    void load_LoadsProductWhenFound() throws IOException {
        ProductEntity product = new ProductEntity();
        TestReflection.setField(product, "id", 5);
        product.setStockQuantity(10);
        bean.setProductId(5);
        when(productService.findActiveById(5)).thenReturn(Optional.of(product));

        bean.load();

        assertNotNull(bean.getProduct());
        assertEquals(5, bean.getProduct().getId());
    }

    @Test
    void load_ShowsMessageAndRedirectsWhenNotFound() throws IOException {
        bean.setProductId(9);
        when(productService.findActiveById(9)).thenReturn(Optional.empty());

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            when(facesContext.getExternalContext()).thenReturn(externalContext);

            bean.load();

            verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
            verify(externalContext).redirect("index.xhtml");
        }
    }

    @Test
    void addToCart_UsesSafeQuantity() {
        ProductEntity product = new ProductEntity();
        TestReflection.setField(product, "id", 5);
        product.setStockQuantity(2);
        TestReflection.setField(bean, "product", product);
        bean.setQuantity(10);

        bean.addToCart();

        verify(cartBean).addProduct(5, 2);
    }

    @Test
    void setQuantity_EnforcesMinimumOne() {
        bean.setQuantity(0);

        assertEquals(1, bean.getQuantity());
    }
}
