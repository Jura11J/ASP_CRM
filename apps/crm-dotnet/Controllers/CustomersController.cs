using AspCrm.Data;
using AspCrm.Models;
using AspCrm.ViewModels;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace AspCrm.Controllers
{
    [Authorize]
    public class CustomersController : Controller
    {
        private readonly AppDbContext _context;

        public CustomersController(AppDbContext context)
        {
            _context = context;
        }

        // Filtruje klientow po tekscie/statusie i zwraca najnowsze rekordy na poczatku.
        public async Task<IActionResult> Index(string? search, string? status)
        {
            var query = _context.Customers.AsQueryable();

            if (!string.IsNullOrWhiteSpace(search))
            {
                var lower = search.ToLower();
                query = query.Where(c =>
                    c.FirstName.ToLower().Contains(lower) ||
                    c.LastName.ToLower().Contains(lower) ||
                    c.Email.ToLower().Contains(lower));
            }

            status = status?.ToLower();
            if (status == "active")
            {
                query = query.Where(c => c.IsActive);
            }
            else if (status == "blocked")
            {
                query = query.Where(c => !c.IsActive);
            }

            var customers = await query
                .OrderByDescending(c => c.CreatedAt)
                .ToListAsync();

            ViewBag.Search = search;
            ViewBag.Status = status;
            return View(customers);
        }

        // Laduje pelna karte klienta: zamowienia, notatki i zgloszenia.
        public async Task<IActionResult> Details(int id)
        {
            var customer = await _context.Customers
                .Include(c => c.Orders)
                .ThenInclude(o => o.Items)
                .Include(c => c.Notes)
                .Include(c => c.Tickets)
                .FirstOrDefaultAsync(c => c.Id == id);

            if (customer == null)
            {
                return NotFound();
            }

            var vm = new CustomerDetailViewModel
            {
                Customer = customer,
                Orders = customer.Orders.OrderByDescending(o => o.CreatedAt),
                Notes = customer.Notes.OrderByDescending(n => n.CreatedAt),
                Tickets = customer.Tickets.OrderByDescending(t => t.CreatedAt),
                NewNote = new CustomerNote { CustomerId = customer.Id, Author = "Opiekun" }
            };

            return View(vm);
        }

        public IActionResult Create()
        {
            return View();
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(Customer customer)
        {
            if (ModelState.IsValid)
            {
                customer.CreatedAt = DateTime.UtcNow;
                _context.Add(customer);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }

            return View(customer);
        }

        public async Task<IActionResult> Edit(int id)
        {
            var customer = await _context.Customers.IgnoreQueryFilters().FirstOrDefaultAsync(c => c.Id == id);
            if (customer == null)
            {
                return NotFound();
            }

            return View(customer);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        // Aktualizuje dane klienta, pomijajac globalny filtr soft-delete.
        public async Task<IActionResult> Edit(int id, Customer input)
        {
            var customer = await _context.Customers.IgnoreQueryFilters().FirstOrDefaultAsync(c => c.Id == id);
            if (customer == null)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                customer.FirstName = input.FirstName;
                customer.LastName = input.LastName;
                customer.Email = input.Email;
                customer.Phone = input.Phone;
                customer.AddressLine1 = input.AddressLine1;
                customer.City = input.City;
                customer.PreferredContactMethod = input.PreferredContactMethod;
                customer.MarketingConsent = input.MarketingConsent;
                customer.IsActive = input.IsActive;

                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Details), new { id = customer.Id });
            }

            return View(input);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Delete(int id)
        {
            var customer = await _context.Customers.IgnoreQueryFilters().FirstOrDefaultAsync(c => c.Id == id);
            if (customer == null)
            {
                return NotFound();
            }

            customer.IsDeleted = true;
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        // Dopisuje notatke operatora do osi czasu klienta.
        public async Task<IActionResult> AddNote(CustomerNote note)
        {
            var customer = await _context.Customers.IgnoreQueryFilters().FirstOrDefaultAsync(c => c.Id == note.CustomerId);
            if (customer == null)
            {
                return NotFound();
            }

            if (string.IsNullOrWhiteSpace(note.Content))
            {
                TempData["Error"] = "Treść notatki jest wymagana.";
                return RedirectToAction(nameof(Details), new { id = note.CustomerId });
            }

            note.CreatedAt = DateTime.UtcNow;
            _context.CustomerNotes.Add(note);
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Details), new { id = note.CustomerId });
        }
    }
}
