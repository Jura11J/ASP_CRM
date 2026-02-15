# ShopFront Jakarta EE - Dokumentacja produktu i dokumentacja techniczna

## 1. Podsumowanie
ShopFront to frontend sklepu internetowego oparty o Jakarta EE 10. Aplikacja pozwala klientowi koncowemu przegladac katalog, zarzadzac koszykiem, skladac zamowienia, sledzic statusy, obslugiwac konto oraz prowadzic live chat z CRM. System jest zintegrowany z aplikacja ASP CRM i wspoldzieli z nia relacyjna baze PostgreSQL.

Najwazniejsza wartosc produktu:
- szybki i nowoczesny frontend klienta koncowego,
- wspolny model danych z CRM (brak duplikacji informacji),
- obsluga procesu zakupowego end-to-end wraz ze wsparciem posprzedazowym.

## 2. Problem biznesowy i cele produktu
### Problem
Sklep internetowy bez integracji z CRM zwykle cierpi na:
- rozjazd danych klienta i zamowien,
- brak centralnego supportu,
- ograniczona widocznosc statusu zamowienia dla klienta.

### Cele
- dostarczyc klientowi intuicyjny frontend zakupowy,
- zapewnic natychmiastowa synchronizacje z CRM,
- umozliwic obsluge supportu przez ticket + live chat.

## 3. Uzytkownicy i persony
- Klient sklepu: przeglada produkty, kupuje, monitoruje zamowienia.
- Klient zalogowany: zarzadza profilem, widzi swoje zamowienia, korzysta z czatu.
- Zespol supportu (posrednio): odbiera ticket i czat w CRM.

## 4. Zakres funkcjonalny produktu
### 4.1 Strona glowna i katalog
- strona glowna z sekcjami produktow i CTA,
- filtrowanie/sortowanie katalogu,
- podglad detalu produktu.

### 4.2 Koszyk i checkout
- koszyk sesyjny (`SessionScoped`),
- zmiana ilosci i usuwanie pozycji,
- checkout z formularzem klienta,
- zapis zamowienia i pozycji do wspolnej bazy.

### 4.3 Konto klienta
- rejestracja i logowanie,
- edycja profilu klienta,
- usuniecie konta (z ograniczeniami biznesowymi).

### 4.4 Moje zamowienia
- wyszukiwanie zamowien po email i opcjonalnie numerze,
- podglad pozycji i historii statusow.

### 4.5 Tickety i support
- formularz ticketu w panelu konta,
- wysylka ticketu do API CRM.

### 4.6 Czat live
- interfejs czatu dla zalogowanego klienta,
- polling wiadomosci,
- REST endpointy `GET/POST` pod `/api/chat/*`.

### 4.7 JSP
- aplikacja posiada takze wariant strony glownej oparty o `Servlet + JSP`:
  - servlet: `JspHomeServlet`,
  - widok JSP: `/WEB-INF/jsp/index.jsp`.

## 5. Architektura
Architektura MVC (w stylu Jakarta EE):
1. View:
- JSF/Facelets (`*.xhtml`) + Bootstrap,
- JSP (`index.jsp`) w wariancie Servlet+JSP.
2. Controller:
- beany web/CDI (`@Named`) oraz servlet API.
3. Model:
- encje JPA mapowane do tabel wspolnych z CRM.
4. Service:
- warstwa logiki i transakcji (order/auth/chat/customer/support).
5. Persistence:
- JPA + Hibernate + PostgreSQL.

## 6. Stack technologiczny
- Java 17
- Jakarta EE 10
- JSF 4 (Facelets)
- Servlet API
- CDI
- JPA (Hibernate 6)
- PostgreSQL
- Bootstrap 5 + custom CSS/JS
- JUnit 5 + Mockito

Plik projektu:
- `apps/shop-jakarta/pom.xml`

## 7. Model danych
Encje JPA:
- `CustomerEntity`
- `ProductEntity`
- `OrderEntity`
- `OrderItemEntity`
- `OrderStatusHistoryEntity`
- `ShopUser`
- `ChatConversationEntity`
- `ChatMessageEntity`

Konfiguracja persistence:
- `apps/shop-jakarta/src/main/resources/META-INF/persistence.xml`

Wazne ustawienia:
- `transaction-type=RESOURCE_LOCAL`,
- `hibernate.hbm2ddl.auto=validate`,
- `hibernate.globally_quoted_identifiers=true` (ustawiane w `JpaConfig`).

## 8. Integracja z CRM
### 8.1 Wspolna baza danych
ShopFront i CRM dzialaja na tej samej bazie PostgreSQL `aspcrm`.

### 8.2 Integracja ticketow
Serwis `SupportService` wysyla zgloszenia na:
- `POST /api/shop/tickets` w CRM.

### 8.3 Integracja czatu
ShopFront zapisuje i odczytuje wiadomosci czatu z tabel wspolnych z CRM.

## 9. Interfejsy i endpointy
### 9.1 Web UI (JSF)
Glowne ekrany:
- `/index.jsf`
- `/products.jsf`
- `/product.jsf`
- `/cart.jsf`
- `/checkout.jsf`
- `/orders.jsf`
- `/account.jsf`
- `/chat.jsf`
- `/register.jsf`

### 9.2 Web UI (Servlet + JSP)
- `/index.jsp` (obslugiwane przez `JspHomeServlet`)

### 9.3 REST chat API
Servlet:
- `apps/shop-jakarta/src/main/java/com/aspcrm/shop/web/ChatApiServlet.java`

Endpointy:
- `GET /api/chat/conversation`
- `GET /api/chat/messages?conversationId=...&afterId=...`
- `POST /api/chat/messages` (body form-url-encoded, `content=...`)
- `POST /api/chat/mark-read`

Przyklad testu czatu przez curl:
```bash
curl -X GET http://localhost:8080/api/chat/conversation
```

## 10. Struktura projektu
```text
apps/shop-jakarta
|- src/main/java/com/aspcrm/shop
|  |- config
|  |- entity
|  |- service
|  \- web
|- src/main/resources/META-INF
|  \- persistence.xml
|- src/main/webapp
|  |- WEB-INF/templates
|  |- WEB-INF/jsp
|  |- resources/css
|  |- resources/js
|  \- *.xhtml
|- src/test/java
|- Dockerfile
\- pom.xml
```

## 11. Konfiguracja i zmienne srodowiskowe
Najwazniejsze env:
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `DB_URL`
- `CRM_API_URL`
- opcjonalnie `HIBERNATE_DDL_AUTO`, `HIBERNATE_DIALECT`, `HIBERNATE_SHOW_SQL`

Konfiguracja runtime JPA:
- `apps/shop-jakarta/src/main/java/com/aspcrm/shop/config/JpaConfig.java`

## 12. Uruchomienie
### 12.1 Docker (rekomendowane)
Z katalogu repo:
```bash
docker compose -f infra/docker-compose.yml build
docker compose -f infra/docker-compose.yml up -d
```

Adres ShopFront:
- `http://localhost:8080`

### 12.2 Lokalnie (Maven + Payara Micro)
Build:
```bash
cd apps/shop-jakarta
mvn clean package -DskipTests
```

Run Payara Micro:
```bash
java -jar C:\tools\payara-micro-6.2024.4.jar --httpPort 8080 --contextRoot / --deploy target\shopfront.war
```

## 13. Testy
Uruchomienie lokalne:
```bash
mvn -f apps/shop-jakarta/pom.xml test
```

Jesli brak Maven lokalnie:
```bash
docker run --rm -v "${PWD}:/work" -w /work/apps/shop-jakarta maven:3.9.9-eclipse-temurin-17 mvn test
```

Zakres testow:
- konfiguracja JPA,
- encje i DTO,
- serwisy domenowe,
- beany web,
- servlet chat API.

## 14. Bezpieczenstwo i dostep
- logowanie/rejestracja konta klienta (`ShopUser`),
- haszowanie hasel (`SHA-256 + salt`) w `PasswordUtil`,
- ograniczenie dostepu do czatu po zalogowaniu (`AuthBean` + `ChatApiServlet`),
- separacja danych klienta po email/ID konwersacji.

## 15. Scenariusz prezentacji produktu (demo)
1. Wejscie na strone glowna i omowienie katalogu.
2. Dodanie produktow do koszyka.
3. Przejscie przez checkout i zlozenie zamowienia.
4. Wejscie w "Moje zamowienia" i pokaz statusow.
5. Rejestracja/logowanie i edycja profilu klienta.
6. Wyslanie ticketu do CRM z panelu konta.
7. Uruchomienie czatu live i wyslanie wiadomosci.
8. Pokazanie, ze odpowiedz CRM pojawia sie w sklepie.

## 16. Jakosc i utrzymanie
- testy automatyczne dla warstw krytycznych,
- centralna konfiguracja JPA przez `JpaConfig`,
- konteneryzacja (Maven build + Payara runtime),
- spojnosc danych przez wspolna baze i mapowanie encji.

## 17. Znane ograniczenia i dalszy rozwoj
Kierunki rozwoju:
- pelne role i uprawnienia po stronie sklepu,
- lepsze raportowanie analityczne zachowan klienta,
- rozszerzenie API publicznego,
- dodatkowe testy E2E.

## 18. Materialy wizualne
Screenshoty produktu:
- `SS/ShopFront.png`
- `SS/ShopProdukty.png`
- `SS/ShopKoszyk.png`
- `SS/ShopKasa.png`
- `SS/ShopKonto.png`
- `SS/ShopZamowienia.png`
- `SS/ShopCzat.png`

