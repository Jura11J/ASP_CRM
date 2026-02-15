package com.aspcrm.shop.web;

import com.aspcrm.shop.entity.ProductEntity;
import com.aspcrm.shop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductBeanTest {
    @Mock
    ProductService productService;

    private ProductBean bean;

    @BeforeEach
    void setUp() {
        bean = new ProductBean();
        bean.productService = productService;
    }

    @Test
    void init_CallsSearch() {
        when(productService.search(any(), anyBoolean(), anyBoolean(), anyString(), anyInt(), anyInt())).thenReturn(List.of(new ProductEntity()));

        bean.init();

        assertNotNull(bean.getProducts());
    }

    @Test
    void search_SetsHasMoreWhenPageIsFull() {
        List<ProductEntity> fullPage = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            fullPage.add(new ProductEntity());
        }
        when(productService.search(any(), anyBoolean(), anyBoolean(), anyString(), anyInt(), anyInt())).thenReturn(fullPage);

        bean.search();

        assertTrue(bean.isHasMore());
    }

    @Test
    void nextPage_IncrementsPageAndSearches() {
        when(productService.search(any(), anyBoolean(), anyBoolean(), anyString(), anyInt(), anyInt())).thenReturn(List.of());

        bean.nextPage();

        verify(productService).search(any(), anyBoolean(), anyBoolean(), anyString(), eq(1), anyInt());
    }

    @Test
    void resetFilters_ResetsValuesAndSearches() {
        when(productService.search(any(), anyBoolean(), anyBoolean(), anyString(), anyInt(), anyInt())).thenReturn(List.of());
        bean.setQuery("abc");
        bean.setShowInactive(true);
        bean.setOnlyInStock(true);
        bean.setSortBy("priceDesc");
        bean.nextPage();

        bean.resetFilters();

        assertNull(bean.getQuery());
        assertFalse(bean.isShowInactive());
        assertFalse(bean.isOnlyInStock());
        assertEquals("nameAsc", bean.getSortBy());
        verify(productService, atLeastOnce()).search(any(), anyBoolean(), anyBoolean(), anyString(), eq(0), anyInt());
    }

    @Test
    void gettersAndSetters_Work() {
        bean.setQuery("q");
        bean.setShowInactive(true);
        bean.setOnlyInStock(true);
        bean.setSortBy("priceAsc");

        assertEquals("q", bean.getQuery());
        assertTrue(bean.isShowInactive());
        assertTrue(bean.isOnlyInStock());
        assertEquals("priceAsc", bean.getSortBy());
    }
}
