package com.aspcrm.shop.web;

import com.aspcrm.shop.entity.ProductEntity;
import com.aspcrm.shop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeBeanTest {
    @Mock
    ProductService productService;

    private HomeBean bean;

    @BeforeEach
    void setUp() {
        bean = new HomeBean();
        bean.productService = productService;
    }

    @Test
    void load_PopulatesFeaturedAndNewest() {
        ProductEntity product = new ProductEntity();
        product.setName("Prod");
        when(productService.search(isNull(), eq(false), eq(false), eq("priceAsc"), eq(0), eq(8))).thenReturn(List.of(product));
        when(productService.search(isNull(), eq(false), eq(false), eq("nameAsc"), eq(0), eq(8))).thenReturn(List.of(product));

        bean.load();

        assertEquals(1, bean.getFeatured().size());
        assertEquals(1, bean.getNewest().size());
    }
}
