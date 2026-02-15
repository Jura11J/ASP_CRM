# ASP CRM (.NET 8) - Dokumentacja produktu i dokumentacja techniczna

## 1. Podsumowanie
ASP CRM to aplikacja backoffice dla sklepu internetowego. System centralizuje obsluge klientow, produktow, zamowien, zgloszen supportowych i czatu live. Projekt jest przygotowany jako aplikacja webowa ASP.NET Core MVC i wspoldzieli baze PostgreSQL z aplikacja ShopFront (Jakarta EE), dzieki czemu dane sa spojne w czasie rzeczywistym.

Najwazniejsza wartosc produktu:
- jedno miejsce pracy dla zespolu operacyjnego sklepu,
- szybka obsluga procesu od klienta do ticketu i zamowienia,
- natychmiastowa synchronizacja danych miedzy CRM i frontendem sklepu.

## 2. Problem biznesowy i cele produktu
### Problem
W klasycznym sklepie internetowym dane sa rozproszone:
- zamowienia sa w panelu sklepu,
- support jest prowadzony osobno,
- historia klienta nie jest latwo dostepna dla operatora.

### Cele
- skrocic czas obslugi klienta i zamowienia,
- zapewnic pelny kontekst klienta (zamowienia + notatki + tickety + chat),
- utrzymac wspolna baze dla CRM i sklepu frontendowego.

## 3. Uzytkownicy i persony
- Operator CRM: codzienna obsluga klientow, ticketow i zamowien.
- Team Lead/Manager: monitoring KPI i statusow na dashboardzie.
- Support agent: obsluga zgloszen i czatu live.

## 4. Zakres funkcjonalny
### 4.1 Dashboard
- KPI: liczba klientow, aktywne zamowienia, zamowienia z miesiaca, wartosc sprzedazy.
- wykres sprzedazy (Chart.js),
- lista ostatnich zamowien.

### 4.2 Klienci
- lista i filtrowanie (search/status),
- podglad szczegolow klienta,
- CRUD klienta (delete jako soft-delete),
- notatki operatora.

### 4.3 Produkty
- lista i filtrowanie,
- szczegoly produktu,
- CRUD produktu (delete jako soft-delete).

### 4.4 Zamowienia
- lista z filtrami (status, klient, zakres dat),
- tworzenie i edycja zamowien z pozycjami,
- historia statusow (`OrderStatusHistory`),
- usuwanie zamowienia wraz z pozycjami i historia.

### 4.5 Tickety
- lista i filtrowanie (status, priorytet, klient),
- CRUD ticketow,
- komentarze do ticketu.

### 4.6 Czat live
- lista konwersacji z klientami,
- podglad i wysylka wiadomosci,
- status rozmowy open/closed,
- polling po stronie frontu do odswiezania.

### 4.7 API dla sklepu
- endpoint `POST /api/shop/tickets`,
- tworzenie klienta (jesli brak) + zalozenie ticketu z poziomu ShopFront.

## 5. Architektura systemu
System jest oparty o MVC:
- Controllers: logika request/response i walidacja flow,
- Views (Razor `.cshtml`): UI panelu CRM,
- Models + DbContext: model domeny i mapowanie do bazy.

Warstwy:
1. UI: Razor + Bootstrap + custom CSS/JS.
2. Application: kontrolery i view modele.
3. Data access: Entity Framework Core (`AppDbContext`).
4. Persistence: PostgreSQL / SQL Server / SQLite (wybierane konfiguracyjnie).

## 6. Stack technologiczny
- .NET 8 (`net8.0`)
- ASP.NET Core MVC
- ASP.NET Core Identity
- Entity Framework Core 8
- Npgsql (PostgreSQL), SQL Server provider, SQLite provider
- Bootstrap 5, jQuery, Chart.js
- xUnit, Moq, EF InMemory/SQLite (testy)

Plik projektu:
- `apps/crm-dotnet/AspCrm.csproj`

## 7. Model danych
Glowne encje domenowe:
- `Customer`
- `Product`
- `Order`
- `OrderItem`
- `OrderStatusHistory`
- `Ticket`
- `TicketComment`
- `CustomerNote`
- `ChatConversation`
- `ChatMessage`

Wazne cechy:
- soft-delete przez global query filters dla `Customer` i `Product`,
- precyzja finansowa `decimal(18,2)` dla pol kwotowych,
- relacje ograniczajace usuniecia (`Restrict`) tam, gdzie to krytyczne biznesowo.

Konfiguracja modelu:
- `apps/crm-dotnet/Data/AppDbContext.cs`

## 8. Bezpieczenstwo
- logowanie przez ASP.NET Core Identity,
- cookie auth i sciezki logowania/wylogowania,
- `[Authorize]` na kontrolerach CRM,
- anty-CSRF przez `ValidateAntiForgeryToken` w akcjach POST.

Konto demo seedowane automatycznie:
- login: `admin@demo.pl`
- haslo: `Admin123!`

Seeder:
- `apps/crm-dotnet/Data/DataSeeder.cs`

## 9. Integracja z ShopFront
### 9.1 Wspolna baza
CRM i ShopFront korzystaja z tej samej bazy PostgreSQL (`aspcrm`), co zapewnia wspolna historie klienta i zamowien.

### 9.2 API ticketowe
Endpoint:
- `POST /api/shop/tickets`

Przykladowy payload:
```json
{
  "email": "klient@example.com",
  "firstName": "Jan",
  "lastName": "Kowalski",
  "phone": "+48123456789",
  "title": "Problem z dostawa",
  "description": "Paczka nie dotarla",
  "priority": "high"
}
```

Przykladowa odpowiedz:
```json
{ "id": 123 }
```

Kontroler API:
- `apps/crm-dotnet/Controllers/Api/ShopTicketsController.cs`

## 10. Struktura projektu
```text
apps/crm-dotnet
|- Controllers
|  |- HomeController.cs
|  |- CustomersController.cs
|  |- ProductsController.cs
|  |- OrdersController.cs
|  |- TicketsController.cs
|  |- LiveChatController.cs
|  \- Api/ShopTicketsController.cs
|- Data
|  |- AppDbContext.cs
|  \- DataSeeder.cs
|- Models
|- ViewModels
|- Views
|- wwwroot
|- Migrations
|- Program.cs
\- AspCrm.csproj
```

## 11. Konfiguracja i zmienne
Najwazniejsze ustawienia:
- `DatabaseProvider`: `Postgres` / `SqlServer` / `Sqlite`
- `ConnectionStrings__PostgresConnection`
- `ConnectionStrings__DefaultConnection`
- `ConnectionStrings__SqliteConnection`

Pliki:
- `apps/crm-dotnet/appsettings.json`
- `apps/crm-dotnet/appsettings.Docker.json`
- `apps/crm-dotnet/appsettings.Development.json`

## 12. Uruchomienie
### 12.1 Docker (rekomendowane)
Z katalogu repo:
```bash
docker compose -f infra/docker-compose.yml build
docker compose -f infra/docker-compose.yml up -d
```

Adres aplikacji CRM:
- `http://localhost:5284`

### 12.2 Lokalnie
Wymagania:
- .NET SDK 8
- PostgreSQL (lub SQL Server/SQLite)

Uruchomienie:
```bash
cd apps/crm-dotnet
dotnet run
```

## 13. Migracje
```bash
dotnet ef database update --project apps/crm-dotnet/AspCrm.csproj
```

## 14. Testy
Projekt testowy:
- `apps/crm-dotnet.Tests`

Uruchomienie:
```bash
dotnet test apps/crm-dotnet.Tests/AspCrm.Tests.csproj
```

Zakres testow:
- kontrolery MVC/API,
- modele i enumy,
- seedowanie danych,
- scenariusze czatu.

## 15. Scenariusz prezentacji produktu (demo)
1. Logowanie do CRM jako `admin@demo.pl`.
2. Dashboard: omowienie KPI i wykresu.
3. Klienci: wyszukiwanie + wejscie w szczegoly + dodanie notatki.
4. Produkty: edycja wybranego produktu.
5. Zamowienia: podglad historii statusow i zmiana statusu.
6. Tickety: utworzenie ticketu i dodanie komentarza.
7. Czat live: odczyt i odpowiedz na wiadomosc klienta.
8. API: wyslanie ticketu ze sklepu i pokazanie rekordu w CRM.

## 16. Jakosc i utrzymanie
- migracje EF utrzymuja spojnosc schematu,
- seeder przygotowuje srodowisko demo,
- testy automatyczne wspieraja regresje,
- separacja warstw (Controller/ViewModel/Model) ulatwia dalszy rozwoj.

## 17. Znane ograniczenia i dalszy rozwoj
Mozliwe kierunki rozwoju:
- role i uprawnienia granularne (RBAC),
- paginacja i eksport danych,
- observability (metrics/tracing),
- integracje z zewnetrznymi systemami ticketowymi.

## 18. Materialy wizualne
Screenshoty produktu:
- `SS/CRMDashboard.png`
- `SS/CRMKlienci.png`
- `SS/CRMProdukty.png`
- `SS/CRMZamowienia.png`
- `SS/CRMZgloszenia.png`
- `SS/CRMCzat.png`

