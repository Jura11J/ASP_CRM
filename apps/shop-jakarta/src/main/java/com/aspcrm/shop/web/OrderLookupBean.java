package com.aspcrm.shop.web;

import com.aspcrm.shop.entity.OrderEntity;
import com.aspcrm.shop.service.OrderService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Named("orderLookupBean")
@ViewScoped
public class OrderLookupBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    OrderService orderService;
    @Inject
    AuthBean authBean;

    private String email;
    private Integer orderId;
    private List<OrderEntity> orders = Collections.emptyList();

    @PostConstruct
    public void init() {
        if (authBean != null && authBean.isLoggedIn()) {
            email = authBean.getEmail();
        }
    }

    public void search() {
        if (email == null || email.isBlank()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Podaj adres e-mail", null));
            return;
        }
        orders = orderService.findOrders(email, orderId);
        if (orders.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Brak zamowien dla podanych danych", null));
        }
    }

    public List<OrderEntity> getOrders() {
        return orders;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
}
