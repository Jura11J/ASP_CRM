package com.aspcrm.shop.service;

import com.aspcrm.shop.entity.CustomerEntity;
import com.aspcrm.shop.entity.ShopUser;
import com.aspcrm.shop.testutil.TestReflection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AuthServiceTest {
    @Mock
    EntityManager entityManager;
    @Mock
    EntityTransaction transaction;
    @Mock
    TypedQuery<ShopUser> userQuery;
    @Mock
    CustomerService customerService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService();
        service.entityManager = entityManager;
        service.customerService = customerService;

        when(entityManager.getTransaction()).thenReturn(transaction);
        when(entityManager.createQuery(anyString(), eq(ShopUser.class))).thenReturn(userQuery);
        when(userQuery.setParameter(eq("email"), any())).thenReturn(userQuery);
    }

    @Test
    void findByEmail_ReturnsOptionalResult() {
        ShopUser user = new ShopUser();
        user.setEmail("john@demo.pl");
        when(userQuery.getResultList()).thenReturn(List.of(user));

        Optional<ShopUser> result = service.findByEmail("john@demo.pl");

        assertTrue(result.isPresent());
    }

    @Test
    void register_ThrowsWhenUserExists_AndRollsBack() {
        ShopUser existing = new ShopUser();
        existing.setEmail("existing@demo.pl");
        when(userQuery.getResultList()).thenReturn(List.of(existing));
        when(transaction.isActive()).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.register("existing@demo.pl", "secret", "A", "B", "1", "Adr", "City", "Email", true));

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(entityManager, never()).persist(any(ShopUser.class));
    }

    @Test
    void register_PersistsUserAndCommits() {
        CustomerEntity customer = new CustomerEntity();
        customer.setEmail("new@demo.pl");

        when(userQuery.getResultList()).thenReturn(List.of());
        when(customerService.findOrCreate(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(customer);

        ShopUser user = service.register("new@demo.pl", "secret", "A", "B", "1", "Adr", "City", "Email", true);

        assertEquals("new@demo.pl", user.getEmail());
        assertEquals(customer, user.getCustomer());
        assertNotNull(user.getPasswordSalt());
        assertNotNull(user.getPasswordHash());
        verify(entityManager).persist(any(ShopUser.class));
        verify(transaction).commit();
    }

    @Test
    void login_ThrowsWhenMissingUser() {
        when(userQuery.getResultList()).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> service.login("missing@demo.pl", "x"));
    }

    @Test
    void login_ThrowsWhenPasswordInvalid() {
        ShopUser user = new ShopUser();
        user.setEmail("john@demo.pl");
        user.setPasswordSalt("salt");
        user.setPasswordHash(PasswordUtil.hash("good", "salt"));

        when(userQuery.getResultList()).thenReturn(List.of(user));

        assertThrows(IllegalArgumentException.class, () -> service.login("john@demo.pl", "bad"));
    }

    @Test
    void login_ReturnsUserWhenPasswordMatches() {
        ShopUser user = new ShopUser();
        user.setEmail("john@demo.pl");
        user.setPasswordSalt("salt");
        user.setPasswordHash(PasswordUtil.hash("good", "salt"));
        when(userQuery.getResultList()).thenReturn(List.of(user));

        ShopUser result = service.login("john@demo.pl", "good");

        assertSame(user, result);
    }

    @Test
    void deleteUser_DoesNothingWhenNullOrNoId() {
        service.deleteUser(null);
        service.deleteUser(new ShopUser());

        verify(entityManager, never()).remove(any());
        verify(transaction, never()).begin();
    }

    @Test
    void deleteUser_RemovesManagedUser() {
        ShopUser user = new ShopUser();
        TestReflection.setField(user, "id", 10);
        ShopUser managed = new ShopUser();

        when(entityManager.find(ShopUser.class, 10)).thenReturn(managed);

        service.deleteUser(user);

        verify(transaction).begin();
        verify(entityManager).remove(managed);
        verify(transaction).commit();
    }
}
