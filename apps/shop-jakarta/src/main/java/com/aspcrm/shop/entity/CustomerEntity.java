package com.aspcrm.shop.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Customers")
public class CustomerEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "FirstName", nullable = false, length = 80)
    private String firstName;

    @Column(name = "LastName", nullable = false, length = 80)
    private String lastName;

    @Column(name = "Email", nullable = false, length = 120)
    private String email;

    @Column(name = "Phone", nullable = false, length = 40)
    private String phone;

    @Column(name = "AddressLine1", length = 160)
    private String addressLine1;

    @Column(name = "City", length = 80)
    private String city;

    @Column(name = "PreferredContactMethod", length = 50)
    private String preferredContactMethod;

    @Column(name = "MarketingConsent", nullable = false)
    private boolean marketingConsent;

    @Column(name = "IsActive", nullable = false)
    private boolean isActive = true;

    @Column(name = "IsDeleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "CreatedAt", nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<OrderEntity> orders = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
        isActive = true;
        isDeleted = false;
    }

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPreferredContactMethod() {
        return preferredContactMethod;
    }

    public void setPreferredContactMethod(String preferredContactMethod) {
        this.preferredContactMethod = preferredContactMethod;
    }

    public boolean isMarketingConsent() {
        return marketingConsent;
    }

    public void setMarketingConsent(boolean marketingConsent) {
        this.marketingConsent = marketingConsent;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderEntity> getOrders() {
        return orders;
    }
}
