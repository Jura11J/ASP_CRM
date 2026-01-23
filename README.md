# ASP CRM mono repo

Monorepo zawiera dwie aplikacje korzystajace ze wspolnej bazy Postgres:
- **CRM (ASP.NET Core MVC, .NET 8)** – backoffice z Identity, produktami, zamowieniami, ticketami.
- **ShopFront (Java 17 + Jakarta EE 10 na Payara Micro)** – prosty frontend sklepu z koszykiem w sesji, skladaniem zamowienia i podgladem statusu.

## Struktura katalogow
- `apps/crm-dotnet` – dotychczasowy projekt CRM (.sln, .csproj, kod, Dockerfile).
- `apps/shop-jakarta` – nowa aplikacja JSF/Payara (pom.xml, kod Java, Dockerfile).
- `infra/docker-compose.yml` – uruchomienie DB + CRM + ShopFront.
- `archive/` – przeniesione artefakty bin/obj ze starego ukladu (nie uzywane w buildach).

## Uruchomienie (Docker Compose)
```bash
docker compose -f infra/docker-compose.yml build
docker compose -f infra/docker-compose.yml up -d
```
- CRM: http://localhost:5284 (ASPNETCORE_ENVIRONMENT=Docker, provider Postgres)
- ShopFront: http://localhost:8080 (Payara Micro)
- Postgres: localhost:5432, baza `aspcrm`, user/password `postgres`/`postgres`

## Migracje bazy (EF Core, z poziomu CRM)
Przy pierwszym uruchomieniu baza jest pusta – uruchom migracje z kontenera lub lokalnie:
```bash
# lokalnie (wymaga .NET 8 SDK)
dotnet ef database update --project apps/crm-dotnet/AspCrm.csproj
```
W Dockerze migracja jest wykonywana recznie; kontener CRM uzywa connection stringa z compose.

## ShopFront (skrot funkcji)
- Lista produktow z filtrem aktywnosci i szukaniem po nazwie/SKU.
- Szczegoly produktu + dodanie do koszyka (ilosc).
- Koszyk w sesji (bez tabel), edycja ilosci/usuwanie, suma.
- Checkout: klient na podstawie email (tworzy nowego gdy brak), Order + OrderItems + wpis w OrderStatusHistory (status NEW, nota "Order placed from ShopFront"). Statusy zgodne z enumem CRM (ordinal).
- Podglad zamowien: e-mail + opcjonalnie numer, widok pozycji i historii statusow.
- JPA/Hibernate z `hibernate.hbm2ddl.auto=validate` – nie zmienia schematu, tylko waliduje.

## CRM (bez zmian logiki)
Kod pozostaje w `apps/crm-dotnet`; ustawienia Docker w `appsettings.Docker.json`. Po przeniesieniu sciezek Dockerfile/compose zostaly zaktualizowane, sama aplikacja dziala jak dotychczas.

## Uzyte technologie
- CRM: ASP.NET Core 8, EF Core, Identity, Bootstrap, Chart.js
- ShopFront: Java 17, Jakarta EE 10 (JSF 4), Hibernate 6, Payara Micro 6, Bootstrap 5

## Konta demo CRM
- email: `admin@demo.pl`
- haslo: `Admin123!`
Logowanie: `/Account/Login`
