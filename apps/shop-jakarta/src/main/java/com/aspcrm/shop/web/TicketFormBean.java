package com.aspcrm.shop.web;

import com.aspcrm.shop.service.SupportService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("ticketFormBean")
@ViewScoped
public class TicketFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    AuthBean authBean;
    @Inject
    SupportService supportService;

    private String title;
    private String description;
    private String priority = "medium";

    public String submit() {
        if (!authBean.isLoggedIn()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Zaloguj się, aby zgłosić problem", null));
            return null;
        }
        boolean ok = supportService.submitTicket(
                authBean.getEmail(),
                authBean.getFirstName(),
                authBean.getLastName(),
                authBean.getPhone(),
                title,
                description,
                priority
        );
        if (ok) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Zgłoszenie wysłane do CRM", null));
            title = description = "";
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Nie udało się wysłać zgłoszenia", null));
        }
        return null;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
