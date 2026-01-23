package com.aspcrm.shop.service;

import com.aspcrm.shop.entity.CustomerEntity;
import com.aspcrm.shop.entity.ShopUser;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.util.Optional;

@RequestScoped
public class AuthService {
    @Inject
    EntityManager entityManager;
    @Inject
    CustomerService customerService;

    public Optional<ShopUser> findByEmail(String email) {
        TypedQuery<ShopUser> query = entityManager.createQuery(
                "SELECT u FROM ShopUser u LEFT JOIN FETCH u.customer WHERE LOWER(u.email)=LOWER(:email)", ShopUser.class);
        query.setParameter("email", email);
        return query.getResultList().stream().findFirst();
    }

    public ShopUser register(String email, String password,
                             String firstName, String lastName, String phone,
                             String address, String city, String preferredContact, boolean marketing) {
        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            if (findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("Użytkownik już istnieje");
            }
            CustomerEntity customer = customerService.findOrCreate(email, firstName, lastName, phone, address, city, preferredContact, marketing);

            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hash(password, salt);

            ShopUser user = new ShopUser();
            user.setEmail(email);
            user.setPasswordSalt(salt);
            user.setPasswordHash(hash);
            user.setCustomer(customer);

            entityManager.persist(user);
            tx.commit();
            return user;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        }
    }

    public ShopUser login(String email, String password) {
        Optional<ShopUser> userOpt = findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Nie znaleziono użytkownika");
        }
        ShopUser user = userOpt.get();
        boolean ok = PasswordUtil.verify(password, user.getPasswordSalt(), user.getPasswordHash());
        if (!ok) {
            throw new IllegalArgumentException("Błędne hasło");
        }
        return user;
    }

    public void deleteUser(ShopUser user) {
        if (user == null || user.getId() == null) return;
        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            ShopUser managed = entityManager.find(ShopUser.class, user.getId());
            if (managed != null) {
                entityManager.remove(managed);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        }
    }
}
