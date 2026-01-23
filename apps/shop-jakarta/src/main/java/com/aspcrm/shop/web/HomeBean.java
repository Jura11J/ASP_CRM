package com.aspcrm.shop.web;

import com.aspcrm.shop.entity.ProductEntity;
import com.aspcrm.shop.service.ProductService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

@Named("homeBean")
@RequestScoped
public class HomeBean {
    @Inject
    ProductService productService;

    private List<ProductEntity> featured;
    private List<ProductEntity> newest;

    @PostConstruct
    public void load() {
        featured = productService.search(null, false, false, "priceAsc", 0, 8);
        newest = productService.search(null, false, false, "nameAsc", 0, 8);
    }

    public List<ProductEntity> getFeatured() {
        return featured;
    }

    public List<ProductEntity> getNewest() {
        return newest;
    }
}
