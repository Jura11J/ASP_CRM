package com.aspcrm.shop.service;

import com.aspcrm.shop.entity.ProductEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@RequestScoped
public class ProductService {
    @Inject
    EntityManager entityManager;

    public List<ProductEntity> search(String queryText,
                                      boolean includeInactive,
                                      boolean onlyInStock,
                                      String sortBy,
                                      int page,
                                      int pageSize) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM ProductEntity p WHERE p.isDeleted = false");
        if (!includeInactive) {
            jpql.append(" AND p.isActive = true");
        }
        if (onlyInStock) {
            jpql.append(" AND p.stockQuantity > 0");
        }
        if (queryText != null && !queryText.isBlank()) {
            jpql.append(" AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :q, '%')))");
        }
        String order = "p.name";
        if ("priceAsc".equals(sortBy)) order = "p.price ASC";
        else if ("priceDesc".equals(sortBy)) order = "p.price DESC";
        else if ("nameAsc".equals(sortBy)) order = "p.name ASC";
        jpql.append(" ORDER BY ").append(order);

        TypedQuery<ProductEntity> query = entityManager.createQuery(jpql.toString(), ProductEntity.class);
        if (queryText != null && !queryText.isBlank()) {
            query.setParameter("q", queryText.trim());
        }
        query.setFirstResult(Math.max(0, page) * Math.max(1, pageSize));
        query.setMaxResults(Math.max(1, pageSize));
        return query.getResultList();
    }

    public Optional<ProductEntity> findActiveById(int productId) {
        String jpql = "SELECT p FROM ProductEntity p WHERE p.id = :id AND p.isDeleted = false AND p.isActive = true";
        TypedQuery<ProductEntity> query = entityManager.createQuery(jpql, ProductEntity.class);
        query.setParameter("id", productId);
        return query.getResultList().stream().findFirst();
    }

    public Optional<ProductEntity> findById(int productId) {
        return Optional.ofNullable(entityManager.find(ProductEntity.class, productId));
    }
}
