package com.aspcrm.shop.web;

import com.aspcrm.shop.entity.ProductEntity;
import com.aspcrm.shop.service.ProductService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

@Named("productBean")
@RequestScoped
public class ProductBean {
    @Inject
    ProductService productService;

    private String query;
    private boolean showInactive = false;
    private boolean onlyInStock = false;
    private String sortBy = "nameAsc";
    private int page = 0;
    private int pageSize = 9;
    private boolean hasMore;
    private List<ProductEntity> products;

    @PostConstruct
    public void init() {
        search();
    }

    public String search() {
        products = productService.search(query, showInactive, onlyInStock, sortBy, page, pageSize);
        hasMore = products.size() == pageSize;
        return null;
    }

    public String nextPage() {
        page++;
        search();
        return null;
    }

    public String resetFilters() {
        query = null;
        showInactive = false;
        onlyInStock = false;
        sortBy = "nameAsc";
        page = 0;
        search();
        return null;
    }

    public List<ProductEntity> getProducts() {
        return products;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isShowInactive() {
        return showInactive;
    }

    public void setShowInactive(boolean showInactive) {
        this.showInactive = showInactive;
    }

    public boolean isOnlyInStock() {
        return onlyInStock;
    }

    public void setOnlyInStock(boolean onlyInStock) {
        this.onlyInStock = onlyInStock;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isHasMore() {
        return hasMore;
    }
}
