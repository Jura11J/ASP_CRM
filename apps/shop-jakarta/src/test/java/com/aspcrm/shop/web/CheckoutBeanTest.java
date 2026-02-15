package com.aspcrm.shop.web;

import com.aspcrm.shop.dto.CartLine;
import com.aspcrm.shop.entity.OrderEntity;
import com.aspcrm.shop.service.OrderService;
import com.aspcrm.shop.testutil.TestReflection;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutBeanTest {
    @Mock
    CartBean cartBean;
    @Mock
    OrderService orderService;
    @Mock
    AuthBean authBean;
    @Mock
    FacesContext facesContext;

    private CheckoutBean bean;

    @BeforeEach
    void setUp() {
        bean = new CheckoutBean();
        bean.cartBean = cartBean;
        bean.orderService = orderService;
        bean.authBean = authBean;
    }

    @Test
    void prefill_LoadsDataFromLoggedUser() {
        when(authBean.isLoggedIn()).thenReturn(true);
        when(authBean.getFirstName()).thenReturn("Jan");
        when(authBean.getLastName()).thenReturn("Nowak");
        when(authBean.getEmail()).thenReturn("jan@demo.pl");
        when(authBean.getPhone()).thenReturn("123");
        when(authBean.getAddress()).thenReturn("Street");
        when(authBean.getCity()).thenReturn("City");
        when(authBean.getPreferredContact()).thenReturn("Phone");
        when(authBean.isMarketingConsent()).thenReturn(true);

        bean.prefill();

        assertEquals("Jan", bean.getFirstName());
        assertEquals("jan@demo.pl", bean.getEmail());
        assertTrue(bean.isMarketingConsent());
    }

    @Test
    void placeOrder_ShowsWarningWhenCartEmpty() {
        when(cartBean.isEmpty()).thenReturn(true);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            String result = bean.placeOrder();
            assertNull(result);
            verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
        }
    }

    @Test
    void placeOrder_ShowsWarningForInvalidEmail() {
        when(cartBean.isEmpty()).thenReturn(false);
        bean.setEmail("invalid");

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.placeOrder();

            verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
            verify(orderService, never()).placeOrder(any(), any(), any(), any(), any(), any(), any(), anyBoolean(), anyList());
        }
    }

    @Test
    void placeOrder_SubmitsOrderAndClearsCart() {
        when(cartBean.isEmpty()).thenReturn(false);
        when(cartBean.getItems()).thenReturn(List.of(new CartLine(1, "P", "S", BigDecimal.TEN, 2, 5)));

        OrderEntity order = new OrderEntity();
        TestReflection.setField(order, "id", 101);
        when(orderService.placeOrder(any(), any(), any(), any(), any(), any(), any(), anyBoolean(), anyList())).thenReturn(order);

        bean.setEmail("ok@demo.pl");

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.placeOrder();

            assertTrue(bean.isConfirmed());
            assertEquals(101, bean.getConfirmationOrderId());
            verify(cartBean).clear();
        }
    }

    @Test
    void placeOrder_HandlesServiceException() {
        when(cartBean.isEmpty()).thenReturn(false);
        when(cartBean.getItems()).thenReturn(List.of(new CartLine(1, "P", "S", BigDecimal.TEN, 2, 5)));
        when(orderService.placeOrder(any(), any(), any(), any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenThrow(new RuntimeException("boom"));
        bean.setEmail("ok@demo.pl");

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.placeOrder();

            assertFalse(bean.isConfirmed());
            verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
        }
    }

    @Test
    void gettersAndSetters_Work() {
        bean.setFirstName("A");
        bean.setLastName("B");
        bean.setEmail("a@b.pl");
        bean.setPhone("1");
        bean.setAddressLine1("Adr");
        bean.setCity("City");
        bean.setPreferredContactMethod("Email");
        bean.setMarketingConsent(true);

        assertEquals("A", bean.getFirstName());
        assertEquals("B", bean.getLastName());
        assertEquals("a@b.pl", bean.getEmail());
        assertEquals("1", bean.getPhone());
        assertEquals("Adr", bean.getAddressLine1());
        assertEquals("City", bean.getCity());
        assertEquals("Email", bean.getPreferredContactMethod());
        assertTrue(bean.isMarketingConsent());
    }
}
