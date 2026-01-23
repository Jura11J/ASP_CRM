package com.aspcrm.shop.web;

import com.aspcrm.shop.entity.ProductEntity;
import com.aspcrm.shop.service.ProductService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

@Named("productDetailBean")
@ViewScoped
public class ProductDetailBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    ProductService productService;

    @Inject
    CartBean cartBean;

    private Integer productId;
    private ProductEntity product;
    private int quantity = 1;

    public void load() throws IOException {
        if (product != null || productId == null) {
            return;
        }
        Optional<ProductEntity> found = productService.findActiveById(productId);
        if (found.isPresent()) {
            product = found.get();
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Produkt nie zostal znaleziony", null));
            FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
        }
    }

    public String addToCart() {
        if (product != null) {
            int safeQty = Math.max(1, Math.min(quantity, product.getStockQuantity()));
            cartBean.addProduct(product.getId(), safeQty);
        }
        return null;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }
}
