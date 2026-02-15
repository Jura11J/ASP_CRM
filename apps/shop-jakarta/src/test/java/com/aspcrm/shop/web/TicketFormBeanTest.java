package com.aspcrm.shop.web;

import com.aspcrm.shop.service.SupportService;
import jakarta.faces.application.FacesMessage;
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
class TicketFormBeanTest {
    @Mock
    AuthBean authBean;
    @Mock
    SupportService supportService;
    @Mock
    FacesContext facesContext;

    private TicketFormBean bean;

    @BeforeEach
    void setUp() {
        bean = new TicketFormBean();
        bean.authBean = authBean;
        bean.supportService = supportService;
    }

    @Test
    void submit_RequiresLogin() {
        when(authBean.isLoggedIn()).thenReturn(false);

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            String result = bean.submit();

            assertNull(result);
            verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
            verify(supportService, never()).submitTicket(any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Test
    void submit_Success_ClearsForm() {
        when(authBean.isLoggedIn()).thenReturn(true);
        when(authBean.getEmail()).thenReturn("u@d.pl");
        when(authBean.getFirstName()).thenReturn("A");
        when(authBean.getLastName()).thenReturn("B");
        when(authBean.getPhone()).thenReturn("1");
        when(supportService.submitTicket(any(), any(), any(), any(), any(), any(), any())).thenReturn(true);

        bean.setTitle("Issue");
        bean.setDescription("Desc");
        bean.setPriority("high");

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.submit();

            assertEquals("", bean.getTitle());
            assertEquals("", bean.getDescription());
            verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
        }
    }

    @Test
    void submit_Failure_LeavesFormValues() {
        when(authBean.isLoggedIn()).thenReturn(true);
        when(authBean.getEmail()).thenReturn("u@d.pl");
        when(authBean.getFirstName()).thenReturn("A");
        when(authBean.getLastName()).thenReturn("B");
        when(authBean.getPhone()).thenReturn("1");
        when(supportService.submitTicket(any(), any(), any(), any(), any(), any(), any())).thenReturn(false);

        bean.setTitle("Issue");
        bean.setDescription("Desc");

        try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {
            mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            bean.submit();

            assertEquals("Issue", bean.getTitle());
            assertEquals("Desc", bean.getDescription());
        }
    }

    @Test
    void gettersAndSetters_Work() {
        bean.setTitle("T");
        bean.setDescription("D");
        bean.setPriority("low");

        assertEquals("T", bean.getTitle());
        assertEquals("D", bean.getDescription());
        assertEquals("low", bean.getPriority());
    }
}
