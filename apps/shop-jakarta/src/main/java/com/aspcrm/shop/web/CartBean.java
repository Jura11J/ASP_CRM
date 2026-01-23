package com.aspcrm.shop.web;

import com.aspcrm.shop.dto.CartLine;
import com.aspcrm.shop.entity.ProductEntity;
import com.aspcrm.shop.service.ProductService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Named("cartBean")
@SessionScoped
public class CartBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    ProductService productService;

    private final Map<Integer, CartLine> lines = new LinkedHashMap<>();

    public List<CartLine> getItems() {
        return new ArrayList<>(lines.values());
    }

    public BigDecimal getTotal() {
        return lines.values().stream()
                .map(CartLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getCount() {
        return lines.values().stream().mapToInt(CartLine::getQuantity).sum();
    }

    public String addProduct(int productId) {
        return addProduct(productId, 1);
    }

    public String addProduct(int productId, int quantity) {
        Optional<ProductEntity> productOpt = productService.findActiveById(productId);
        if (productOpt.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Produkt nie jest dostepny", null));
            return null;
        }

        ProductEntity product = productOpt.get();
        int qty = Math.max(1, Math.min(quantity, product.getStockQuantity()));
        CartLine line = lines.get(productId);
        if (line == null) {
            line = new CartLine(product.getId(), product.getName(), product.getSku(), product.getPrice(), qty, product.getStockQuantity());
            lines.put(productId, line);
        } else {
            int newQty = Math.min(line.getQuantity() + qty, product.getStockQuantity());
            line.setQuantity(newQty);
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Dodano do koszyka", product.getName()));
        return null;
    }

    public void updateQuantity(int productId, int quantity) {
        CartLine line = lines.get(productId);
        if (line != null) {
            if (quantity <= 0) {
                lines.remove(productId);
            } else {
                int capped = Math.min(quantity, line.getStockQuantity());
                line.setQuantity(capped);
            }
        }
    }

    public void remove(int productId) {
        lines.remove(productId);
    }

    public void clear() {
        lines.clear();
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }
}
