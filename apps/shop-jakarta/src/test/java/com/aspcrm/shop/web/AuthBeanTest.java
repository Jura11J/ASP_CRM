package com.aspcrm.shop.web;

import com.aspcrm.shop.entity.CustomerEntity;
import com.aspcrm.shop.entity.ShopUser;
import com.aspcrm.shop.service.AuthService;
import com.aspcrm.shop.service.CustomerService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthBeanTest {
    @Mock
    CustomerService customerService;
    @Mock
    AuthService authService;
    @Mock
    FacesContext facesContext;
    @Mock
    ExternalContext externalContext;

    private AuthBean bean;

    @BeforeEach
    void setUp() {
        bean = new AuthBean();
        bean.customerService = customerService;
        bean.authService = authService;
    }

    @Test
    void isLoggedIn_ReturnsFalseByDefault() {
        assertFalse(bean.isLoggedIn());
    }

    @Test
    void login_SetsCurrentUserOnSuccess() {
        bean.setEmail("user@demo.pl");
        bean.setPassword("secret");

        CustomerEntity customer = new CustomerEntity();
        customer.setEmail("user@demo.pl");
        ShopUser user = new ShopUser();
        user.setEmail("user@demo.pl");
        user.setCustomer(customer);
        when(authService.login("user@demo.pl", "secret")).thenReturn(user);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            String result = bean.login();
            assertNull(result);
            assertTrue(bean.isLoggedIn());
            assertEquals("user@demo.pl", bean.getEmail());
            verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
        }
    }

    @Test
    void login_AddsErrorMessageOnFailure() {
        bean.setEmail("user@demo.pl");
        bean.setPassword("bad");
        when(authService.login(anyString(), anyString())).thenThrow(new IllegalArgumentException("bad"));

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.login();

            verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
            assertFalse(bean.isLoggedIn());
        }
    }

    @Test
    void register_ValidatesPasswordsAndRegisters() {
        bean.setEmail("new@demo.pl");
        bean.setNewPassword("123456");
        bean.setRepeatPassword("123456");
        bean.setFirstName("A");
        bean.setLastName("B");

        CustomerEntity customer = new CustomerEntity();
        customer.setEmail("new@demo.pl");
        ShopUser user = new ShopUser();
        user.setEmail("new@demo.pl");
        user.setCustomer(customer);
        when(authService.register(any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(user);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.register();

            assertTrue(bean.isLoggedIn());
            verify(authService).register(eq("new@demo.pl"), eq("123456"), anyString(), anyString(), any(), any(), any(), anyString(), anyBoolean());
        }
    }

    @Test
    void updateProfile_UsesRegisterWhenNoCurrentCustomer() {
        bean.setEmail("new@demo.pl");
        bean.setNewPassword("123456");
        bean.setRepeatPassword("123456");

        ShopUser user = new ShopUser();
        user.setEmail("new@demo.pl");
        user.setCustomer(new CustomerEntity());
        when(authService.register(any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(user);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.updateProfile();

            verify(authService).register(any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean());
        }
    }

    @Test
    void updateProfile_UpdatesExistingCustomer() {
        CustomerEntity customer = new CustomerEntity();
        customer.setEmail("x@demo.pl");
        ShopUser user = new ShopUser();
        user.setEmail("x@demo.pl");
        user.setCustomer(customer);
        TestReflection.setField(bean, "currentCustomer", customer);
        TestReflection.setField(bean, "currentUser", user);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.updateProfile();

            verify(customerService).updateCustomer(eq(customer), any(), any(), any(), any(), any(), any(), anyBoolean());
        }
    }

    @Test
    void deleteAccount_DeletesWhenNoOrdersAndLogsOut() {
        CustomerEntity customer = new CustomerEntity();
        ShopUser user = new ShopUser();
        user.setCustomer(customer);
        TestReflection.setField(bean, "currentCustomer", customer);
        TestReflection.setField(bean, "currentUser", user);

        when(customerService.softDeleteCustomer(customer)).thenReturn(true);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            when(facesContext.getExternalContext()).thenReturn(externalContext);

            bean.deleteAccount();

            verify(authService).deleteUser(user);
            verify(externalContext).invalidateSession();
            assertFalse(bean.isLoggedIn());
        }
    }

    @Test
    void deleteAccount_ShowsErrorWhenCannotDelete() {
        CustomerEntity customer = new CustomerEntity();
        TestReflection.setField(bean, "currentCustomer", customer);
        TestReflection.setField(bean, "currentUser", new ShopUser());
        when(customerService.softDeleteCustomer(customer)).thenReturn(false);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.deleteAccount();

            verify(authService, never()).deleteUser(any());
            assertTrue(bean.isLoggedIn());
        }
    }

    @Test
    void logout_ClearsStateAndInvalidatesSession() {
        TestReflection.setField(bean, "currentUser", new ShopUser());
        TestReflection.setField(bean, "currentCustomer", new CustomerEntity());
        bean.setEmail("x@demo.pl");

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            when(facesContext.getExternalContext()).thenReturn(externalContext);

            bean.logout();

            assertFalse(bean.isLoggedIn());
            assertNull(bean.getCurrent());
            verify(externalContext).invalidateSession();
        }
    }

    @Test
    void gettersAndSetters_WorkForEditableFields() {
        bean.setEmail("e@d.pl");
        bean.setFirstName("Jan");
        bean.setLastName("Nowak");
        bean.setPhone("123");
        bean.setAddress("Street");
        bean.setCity("City");
        bean.setPreferredContact("Phone");
        bean.setMarketingConsent(true);
        bean.setPassword("p");
        bean.setNewPassword("n");
        bean.setRepeatPassword("r");

        assertEquals("e@d.pl", bean.getEmail());
        assertEquals("Jan", bean.getFirstName());
        assertEquals("Nowak", bean.getLastName());
        assertEquals("123", bean.getPhone());
        assertEquals("Street", bean.getAddress());
        assertEquals("City", bean.getCity());
        assertEquals("Phone", bean.getPreferredContact());
        assertTrue(bean.isMarketingConsent());
        assertEquals("p", bean.getPassword());
        assertEquals("n", bean.getNewPassword());
        assertEquals("r", bean.getRepeatPassword());
    }
}
