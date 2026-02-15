package com.aspcrm.shop.web;

import com.aspcrm.shop.entity.OrderEntity;
import com.aspcrm.shop.service.OrderService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderLookupBeanTest {
    @Mock
    OrderService orderService;
    @Mock
    AuthBean authBean;
    @Mock
    FacesContext facesContext;

    private OrderLookupBean bean;

    @BeforeEach
    void setUp() {
        bean = new OrderLookupBean();
        bean.orderService = orderService;
        bean.authBean = authBean;
    }

    @Test
    void init_PrefillsEmailForLoggedUser() {
        when(authBean.isLoggedIn()).thenReturn(true);
        when(authBean.getEmail()).thenReturn("user@demo.pl");

        bean.init();

        assertEquals("user@demo.pl", bean.getEmail());
    }

    @Test
    void search_ShowsWarningWhenEmailMissing() {
        bean.setEmail("  ");

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.search();

            verify(orderService, never()).findOrders(anyString(), any());
            verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
        }
    }

    @Test
    void search_SetsOrdersWhenFound() {
        bean.setEmail("user@demo.pl");
        bean.setOrderId(10);
        when(orderService.findOrders("user@demo.pl", 10)).thenReturn(List.of(new OrderEntity()));

        bean.search();

        assertEquals(1, bean.getOrders().size());
    }

    @Test
    void search_ShowsInfoWhenNoOrders() {
        bean.setEmail("user@demo.pl");
        when(orderService.findOrders(anyString(), any())).thenReturn(List.of());

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.search();

            verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
        }
    }
}
