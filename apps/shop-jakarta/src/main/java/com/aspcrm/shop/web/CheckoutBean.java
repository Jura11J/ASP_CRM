package com.aspcrm.shop.web;

import com.aspcrm.shop.dto.CartLine;
import com.aspcrm.shop.entity.OrderEntity;
import com.aspcrm.shop.service.OrderService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;

@Named("checkoutBean")
@ViewScoped
public class CheckoutBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    CartBean cartBean;

    @Inject
    OrderService orderService;

    @Inject
    AuthBean authBean;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String addressLine1;
    private String city;
    private String preferredContactMethod;
    private boolean marketingConsent;

    private Integer confirmationOrderId;

    @PostConstruct
    public void prefill() {
        if (authBean != null && authBean.isLoggedIn()) {
            firstName = authBean.getFirstName();
            lastName = authBean.getLastName();
            email = authBean.getEmail();
            phone = authBean.getPhone();
            addressLine1 = authBean.getAddress();
            city = authBean.getCity();
            preferredContactMethod = authBean.getPreferredContact();
            marketingConsent = authBean.isMarketingConsent();
        }
    }

    public String placeOrder() {
        if (cartBean.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Koszyk jest pusty", null));
            return null;
        }
        if (email == null || !email.contains("@")) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Podaj poprawny email", null));
            return null;
        }
        try {
            List<CartLine> items = cartBean.getItems();
            OrderEntity order = orderService.placeOrder(email, firstName, lastName, phone, addressLine1, city, preferredContactMethod, marketingConsent, items);
            confirmationOrderId = order.getId();
            cartBean.clear();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Zamowienie zlozone", "Numer: " + confirmationOrderId));
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Nie udalo sie zlozyc zamowienia", ex.getMessage()));
        }
        return null;
    }

    public boolean isConfirmed() {
        return confirmationOrderId != null;
    }

    public Integer getConfirmationOrderId() {
        return confirmationOrderId;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPreferredContactMethod() { return preferredContactMethod; }
    public void setPreferredContactMethod(String preferredContactMethod) { this.preferredContactMethod = preferredContactMethod; }

    public boolean isMarketingConsent() { return marketingConsent; }
    public void setMarketingConsent(boolean marketingConsent) { this.marketingConsent = marketingConsent; }
}
