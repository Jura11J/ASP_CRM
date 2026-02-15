<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,java.util.Collections,com.aspcrm.shop.entity.ProductEntity" %>
<%!
    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>
<%
    String ctx = request.getContextPath();

    @SuppressWarnings("unchecked")
    List<ProductEntity> featured = (List<ProductEntity>) request.getAttribute("featuredProducts");
    if (featured == null) {
        featured = Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    List<ProductEntity> newest = (List<ProductEntity>) request.getAttribute("newestProducts");
    if (newest == null) {
        newest = Collections.emptyList();
    }

    String homeLoadError = (String) request.getAttribute("homeLoadError");
%>
<!DOCTYPE html>
<html lang="pl">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Sklep internetowy</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"/>
    <link rel="stylesheet" href="<%= ctx %>/resources/css/site.css"/>
</head>
<body class="bg-page d-flex flex-column min-vh-100">
<nav class="navbar navbar-expand-lg navbar-dark navbar-glass shadow-sm">
    <div class="container">
        <a class="navbar-brand fw-bold" href="<%= ctx %>/index.jsp">ShopFront</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav"
                aria-controls="mainNav" aria-expanded="false" aria-label="Przelacz nawigacje">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNav">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item"><a class="nav-link" href="<%= ctx %>/products.jsf">Produkty</a></li>
            </ul>
            <div class="d-flex align-items-center gap-2">
                <div class="dropdown">
                    <button class="btn btn-avatar d-flex align-items-center gap-2 rounded-pill" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <span class="avatar-circle"><i class="bi bi-person"></i></span>
                        <span class="d-none d-md-inline fw-semibold">Konto</span>
                    </button>
                    <div class="dropdown-menu dropdown-menu-end p-3 shadow wide-dropdown">
                        <h5 class="mb-2">Zaloguj sie</h5>
                        <form action="<%= ctx %>/account.jsf" method="get" class="vstack gap-2">
                            <label for="loginEmail" class="small text-muted">E-mail</label>
                            <input id="loginEmail" type="email" class="form-control" autocomplete="email"/>
                            <label for="loginPass" class="small text-muted">Haslo</label>
                            <input id="loginPass" type="password" class="form-control" autocomplete="current-password"/>
                            <button type="submit" class="btn btn-primary w-100">Przejdz do konta</button>
                            <a class="btn btn-link w-100" href="<%= ctx %>/register.jsf">Zarejestruj sie</a>
                        </form>
                    </div>
                </div>
                <a class="btn btn-cart position-relative" href="<%= ctx %>/cart.jsf">
                    <i class="bi bi-cart3 me-1"></i> Koszyk
                    <span class="badge bg-warning text-dark ms-1">0</span>
                </a>
            </div>
        </div>
    </div>
</nav>

<main class="container py-4 flex-grow-1 position-relative">
    <% if (homeLoadError != null) { %>
    <div class="alert alert-warning mb-3"><%= esc(homeLoadError) %></div>
    <% } %>

    <div class="bg-blob blob-1"></div>
    <div class="bg-blob blob-2"></div>
    <div class="bg-blob blob-3"></div>

    <section class="hero p-4 p-lg-5 mb-5">
        <div class="row align-items-center g-4">
            <div class="col-lg-7">
                <p class="kicker">Nowa kolekcja</p>
                <h1 class="display-5 fw-bolder mb-3">Zamawiaj produkty prosto</h1>
                <p class="lead text-light-50 mb-4">Przegladaj oferte, dodawaj do koszyka i skladaj zamowienia online. Statusy zsynchronizowane z CRM.</p>
                <div class="d-flex flex-wrap gap-3">
                    <a class="btn btn-hero" href="<%= ctx %>/products.jsf">Przegladaj produkty</a>
                    <a class="btn btn-outline-light" href="<%= ctx %>/orders.jsf">Sprawdz zamowienie</a>
                </div>
                <div class="d-flex flex-wrap gap-4 mt-4 text-light-60">
                    <div><i class="bi bi-shield-check me-1"></i> Wspolna baza z CRM</div>
                    <div><i class="bi bi-clock-history me-1"></i> Statusy na zywo</div>
                    <div><i class="bi bi-truck me-1"></i> Szybka realizacja</div>
                </div>
            </div>
            <div class="col-lg-5">
                <div class="glass mini-card">
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        <div>
                            <p class="text-light-60 mb-1">Aktywne produkty</p>
                            <h2 class="text-white fw-bold"><%= featured.size() %>+</h2>
                        </div>
                        <span class="badge bg-light text-dark">Live</span>
                    </div>
                    <div class="progress progress-thin mb-3">
                        <div class="progress-bar" role="progressbar" style="width: 80%"></div>
                    </div>
                    <p class="text-light-60 mb-3">Historia zamowien wspolna z CRM. Dane z tej samej bazy PostgreSQL.</p>
                    <div class="d-flex gap-2">
                        <a class="btn btn-sm btn-outline-light" href="<%= ctx %>/orders.jsf">Sprawdz zamowienie</a>
                        <a class="btn btn-sm btn-outline-light" href="<%= ctx %>/account.jsf">Moje konto</a>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section class="mb-5">
        <div class="row g-3">
            <div class="col-md-4">
                <div class="glass shortcut d-flex align-items-center gap-3">
                    <div class="icon-circle bg-primary-subtle text-primary"><i class="bi bi-search"></i></div>
                    <div>
                        <h6 class="mb-1">Szybkie wyszukiwanie</h6>
                        <small class="text-muted">Filtruj po nazwie lub SKU, tylko aktywne lub dostepne.</small>
                    </div>
                    <a href="<%= ctx %>/products.jsf" class="btn btn-sm btn-outline-primary ms-auto">Szukaj</a>
                </div>
            </div>
            <div class="col-md-4">
                <div class="glass shortcut d-flex align-items-center gap-3">
                    <div class="icon-circle bg-success-subtle text-success"><i class="bi bi-bag-check"></i></div>
                    <div>
                        <h6 class="mb-1">Koszyk sesyjny</h6>
                        <small class="text-muted">Bez zakladania konta, zapis w sesji do checkout.</small>
                    </div>
                    <a href="<%= ctx %>/cart.jsf" class="btn btn-sm btn-outline-success ms-auto">Koszyk</a>
                </div>
            </div>
            <div class="col-md-4">
                <div class="glass shortcut d-flex align-items-center gap-3">
                    <div class="icon-circle bg-warning-subtle text-warning"><i class="bi bi-receipt"></i></div>
                    <div>
                        <h6 class="mb-1">Status zamowienia</h6>
                        <small class="text-muted">Podglad historii statusow.</small>
                    </div>
                    <a href="<%= ctx %>/orders.jsf" class="btn btn-sm btn-outline-warning ms-auto">Sprawdz</a>
                </div>
            </div>
        </div>
    </section>

    <section class="mb-5">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <div>
                <p class="eyebrow">Dla Ciebie</p>
                <h3 class="section-title">Polecane produkty</h3>
            </div>
            <a href="<%= ctx %>/products.jsf" class="btn btn-outline-primary btn-sm">Zobacz katalog</a>
        </div>
        <div class="row row-cols-1 row-cols-md-4 g-3">
            <%
                int featuredLimit = Math.min(4, featured.size());
                for (int i = 0; i < featuredLimit; i++) {
                    ProductEntity p = featured.get(i);
            %>
            <div class="col">
                <div class="card h-100 shadow-sm product-card">
                    <div class="card-body d-flex flex-column">
                        <div class="d-flex justify-content-between align-items-start mb-2">
                            <h5 class="card-title mb-0"><%= esc(p.getName()) %></h5>
                            <span class="badge bg-secondary"><%= esc(p.getSku()) %></span>
                        </div>
                        <p class="card-text text-muted flex-grow-1 small"><%= esc(p.getDescription()) %></p>
                        <div class="d-flex justify-content-between align-items-center mt-2">
                            <div>
                                <div class="fw-bold fs-5"><%= p.getPrice() %> zl</div>
                                <small class="text-secondary">Stan: <%= p.getStockQuantity() %></small>
                            </div>
                            <div class="d-flex gap-2">
                                <a class="btn btn-sm btn-success" href="<%= ctx %>/cart.jsf">Dodaj</a>
                                <a class="btn btn-sm btn-outline-primary" href="<%= ctx %>/product.jsf?id=<%= p.getId() %>">Szczegoly</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <% } %>
        </div>
    </section>

    <section class="mb-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <div>
                <p class="eyebrow">Swieze dostawy</p>
                <h3 class="section-title">Nowosci i najczesciej kupowane</h3>
            </div>
            <a href="<%= ctx %>/products.jsf" class="btn btn-link">Pelna lista</a>
        </div>
        <div class="row row-cols-1 row-cols-md-4 g-3">
            <%
                int newestLimit = Math.min(4, newest.size());
                for (int i = 0; i < newestLimit; i++) {
                    ProductEntity p = newest.get(i);
            %>
            <div class="col">
                <div class="card h-100 shadow-sm product-card">
                    <div class="card-body d-flex flex-column">
                        <div class="d-flex justify-content-between align-items-start mb-2">
                            <h5 class="card-title mb-0"><%= esc(p.getName()) %></h5>
                            <span class="badge bg-secondary"><%= esc(p.getSku()) %></span>
                        </div>
                        <p class="card-text text-muted flex-grow-1 small"><%= esc(p.getDescription()) %></p>
                        <div class="d-flex justify-content-between align-items-center mt-2">
                            <div>
                                <div class="fw-bold fs-5"><%= p.getPrice() %> zl</div>
                                <small class="text-secondary">Stan: <%= p.getStockQuantity() %></small>
                            </div>
                            <div class="d-flex gap-2">
                                <a class="btn btn-sm btn-success" href="<%= ctx %>/cart.jsf">Dodaj</a>
                                <a class="btn btn-sm btn-outline-primary" href="<%= ctx %>/product.jsf?id=<%= p.getId() %>">Szczegoly</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <% } %>
        </div>
    </section>

    <% if (featured.isEmpty() && newest.isEmpty() && homeLoadError == null) { %>
    <div class="alert alert-info">Brak produktow do wyswietlenia.</div>
    <% } %>
</main>

<footer class="footer-bar mt-auto">
    <div class="container d-flex flex-column flex-md-row justify-content-between align-items-center">
        <span class="fw-semibold">ShopFront</span>
        <span class="small text-secondary">Kontakt: sklep@example.com • Regulamin • Polityka prywatnosci</span>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
        crossorigin="anonymous"></script>
<script src="<%= ctx %>/resources/js/site.js"></script>
</body>
</html>
