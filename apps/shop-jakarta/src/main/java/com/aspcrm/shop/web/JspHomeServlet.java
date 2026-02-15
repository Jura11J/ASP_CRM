package com.aspcrm.shop.web;

import com.aspcrm.shop.entity.ProductEntity;
import com.aspcrm.shop.service.ProductService;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet(urlPatterns = "/index.jsp")
public class JspHomeServlet extends HttpServlet {
    @Inject
    ProductService productService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<ProductEntity> featured;
        List<ProductEntity> newest;
        try {
            featured = productService.search(null, false, false, "priceAsc", 0, 8);
            newest = productService.search(null, false, false, "nameAsc", 0, 8);
        } catch (Exception ex) {
            featured = Collections.emptyList();
            newest = Collections.emptyList();
            req.setAttribute("homeLoadError", "Nie udalo sie pobrac produktow.");
        }

        req.setAttribute("featuredProducts", featured);
        req.setAttribute("newestProducts", newest);
        req.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(req, resp);
    }
}
