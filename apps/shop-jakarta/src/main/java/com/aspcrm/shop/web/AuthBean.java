package com.aspcrm.shop.web;

import com.aspcrm.shop.entity.CustomerEntity;
import com.aspcrm.shop.entity.ShopUser;
import com.aspcrm.shop.service.AuthService;
import com.aspcrm.shop.service.CustomerService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("authBean")
@SessionScoped
public class AuthBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    CustomerService customerService;
    @Inject
    AuthService authService;

    private CustomerEntity currentCustomer;
    private ShopUser currentUser;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String city;
    private String preferredContact = "Email";
    private boolean marketingConsent;
    private String password;
    private String newPassword;
    private String repeatPassword;

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String login() {
        try {
            currentUser = authService.login(email, password);
            currentCustomer = currentUser.getCustomer();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Zalogowano", currentUser.getEmail()));
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Nie udało się zalogować", ex.getMessage()));
        }
        return null;
    }

    public String register() {
        if (newPassword == null || newPassword.length() < 6 || repeatPassword == null || !newPassword.equals(repeatPassword)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Hasło musi mieć min. 6 znaków i być powtórzone", null));
            return null;
        }
        currentUser = authService.register(email, newPassword, firstName, lastName, phone, address, city, preferredContact, marketingConsent);
        currentCustomer = currentUser.getCustomer();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Utworzono konto", currentUser.getEmail()));
        return null;
    }

    public String updateProfile() {
        if (currentCustomer == null) {
            return register();
        }
        customerService.updateCustomer(currentCustomer, firstName, lastName, phone, address, city, preferredContact, marketingConsent);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Dane zaktualizowane", null));
        return null;
    }

    public String deleteAccount() {
        if (currentCustomer == null) return null;
        boolean ok = customerService.softDeleteCustomer(currentCustomer);
        if (ok) {
            authService.deleteUser(currentUser);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Konto usunięte", null));
            logout();
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Nie można usunąć konta z historią zamówień", null));
        }
        return null;
    }

    public void logout() {
        currentCustomer = null;
        currentUser = null;
        email = firstName = lastName = phone = address = city = null;
        preferredContact = "Email";
        marketingConsent = false;
        password = newPassword = repeatPassword = null;
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null && context.getExternalContext() != null) {
            context.getExternalContext().invalidateSession();
        }
    }

    public CustomerEntity getCurrent() { return currentCustomer; }
    public String getEmail() { return currentCustomer != null ? currentCustomer.getEmail() : email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return currentCustomer != null ? currentCustomer.getFirstName() : firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return currentCustomer != null ? currentCustomer.getLastName() : lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return currentCustomer != null ? currentCustomer.getPhone() : phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return currentCustomer != null ? currentCustomer.getAddressLine1() : address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return currentCustomer != null ? currentCustomer.getCity() : city; }
    public void setCity(String city) { this.city = city; }
    public String getPreferredContact() { return currentCustomer != null ? currentCustomer.getPreferredContactMethod() : preferredContact; }
    public void setPreferredContact(String preferredContact) { this.preferredContact = preferredContact; }
    public boolean isMarketingConsent() { return currentCustomer != null ? currentCustomer.isMarketingConsent() : marketingConsent; }
    public void setMarketingConsent(boolean marketingConsent) { this.marketingConsent = marketingConsent; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getRepeatPassword() { return repeatPassword; }
    public void setRepeatPassword(String repeatPassword) { this.repeatPassword = repeatPassword; }
}
