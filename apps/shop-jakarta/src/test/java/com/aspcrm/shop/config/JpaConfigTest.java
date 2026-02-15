package com.aspcrm.shop.config;

import com.aspcrm.shop.testutil.TestReflection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class JpaConfigTest {
    @Test
    void init_CreatesEntityManagerFactory_WithQuotedIdentifiersEnabled() {
        JpaConfig config = new JpaConfig();
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        AtomicReference<Map<String, Object>> capturedProps = new AtomicReference<>();

        try (MockedStatic<Persistence> mocked = mockStatic(Persistence.class)) {
            mocked.when(() -> Persistence.createEntityManagerFactory(eq("ShopPU"), anyMap()))
                    .thenAnswer(invocation -> {
                        capturedProps.set(invocation.getArgument(1));
                        return emf;
                    });

            config.init();
        }

        assertNotNull(capturedProps.get());
        assertEquals("true", capturedProps.get().get("hibernate.globally_quoted_identifiers"));
    }

    @Test
    void entityManager_ReturnsCreatedEntityManager() {
        JpaConfig config = new JpaConfig();
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        EntityManager em = mock(EntityManager.class);
        when(emf.createEntityManager()).thenReturn(em);
        TestReflection.setField(config, "emf", emf);

        EntityManager produced = config.entityManager();

        assertSame(em, produced);
    }

    @Test
    void close_ClosesEntityManagerIfOpen() {
        JpaConfig config = new JpaConfig();
        EntityManager em = mock(EntityManager.class);
        when(em.isOpen()).thenReturn(true);

        config.close(em);

        verify(em).close();
    }

    @Test
    void shutdown_ClosesFactoryIfOpen() {
        JpaConfig config = new JpaConfig();
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        when(emf.isOpen()).thenReturn(true);
        TestReflection.setField(config, "emf", emf);

        config.shutdown();

        verify(emf).close();
    }
}
