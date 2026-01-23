package com.aspcrm.shop.service;

import com.aspcrm.shop.entity.CustomerEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Optional;

@RequestScoped
public class CustomerService {
    @Inject
    EntityManager entityManager;

    public Optional<CustomerEntity> findByEmail(String email) {
        String jpql = "SELECT c FROM CustomerEntity c WHERE LOWER(c.email) = LOWER(:email) AND c.isDeleted = false";
        TypedQuery<CustomerEntity> query = entityManager.createQuery(jpql, CustomerEntity.class);
        query.setParameter("email", email);
        return query.getResultList().stream().findFirst();
    }

    public CustomerEntity updateCustomer(CustomerEntity customer,
                                         String firstName,
                                         String lastName,
                                         String phone,
                                         String address,
                                         String city,
                                         String preferredContact,
                                         boolean marketingConsent) {
        if (firstName != null && !firstName.isBlank()) customer.setFirstName(firstName);
        if (lastName != null && !lastName.isBlank()) customer.setLastName(lastName);
        if (phone != null) customer.setPhone(phone);
        if (address != null) customer.setAddressLine1(address);
        if (city != null) customer.setCity(city);
        customer.setPreferredContactMethod(preferredContact);
        customer.setMarketingConsent(marketingConsent);
        customer.setActive(true);
        return customer;
    }

    public boolean softDeleteCustomer(CustomerEntity customer) {
        if (customer.getOrders() != null && !customer.getOrders().isEmpty()) {
            return false;
        }
        customer.setActive(false);
        customer.setDeleted(true);
        return true;
    }

    public CustomerEntity findOrCreate(String email, String firstName, String lastName, String phone,
                                       String address, String city, String preferredContact, boolean marketingConsent) {
        Optional<CustomerEntity> existing = findByEmail(email);
        if (existing.isPresent()) {
            return updateCustomer(existing.get(), firstName, lastName, phone, address, city, preferredContact, marketingConsent);
        }

        CustomerEntity created = new CustomerEntity();
        created.setEmail(email);
        created.setFirstName(firstName);
        created.setLastName(lastName);
        created.setPhone(phone);
        created.setAddressLine1(address);
        created.setCity(city);
        created.setPreferredContactMethod(preferredContact);
        created.setMarketingConsent(marketingConsent);
        entityManager.persist(created);
        return created;
    }
}
