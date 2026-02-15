using AspCrm.Data;
using AspCrm.Models;
using AspCrm.ViewModels;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;

namespace AspCrm.Controllers
{
    [Authorize]
    public class TicketsController : Controller
    {
        private readonly AppDbContext _context;

        public TicketsController(AppDbContext context)
        {
            _context = context;
        }

        // Naklada opcjonalne filtry i przygotowuje liste zgloszen z wyborem klienta.
        public async Task<IActionResult> Index(TicketStatus? status, TicketPriority? priority, int? customerId)
        {
            var query = _context.Tickets
                .Include(t => t.Customer)
                .AsQueryable();

            if (status.HasValue)
            {
                query = query.Where(t => t.Status == status);
            }

            if (priority.HasValue)
            {
                query = query.Where(t => t.Priority == priority);
            }

            if (customerId.HasValue)
            {
                query = query.Where(t => t.CustomerId == customerId.Value);
            }

            var tickets = await query.OrderByDescending(t => t.CreatedAt).ToListAsync();
            var customers = await _context.Customers
                .Select(c => new SelectListItem { Value = c.Id.ToString(), Text = c.FullName })
                .ToListAsync();

            var vm = new TicketListViewModel
            {
                Tickets = tickets,
                Status = status,
                Priority = priority,
                CustomerId = customerId,
                Customers = customers
            };

            return View(vm);
        }

        public async Task<IActionResult> Details(int id)
        {
            var ticket = await _context.Tickets
                .Include(t => t.Customer)
                .Include(t => t.Comments)
                .FirstOrDefaultAsync(t => t.Id == id);

            if (ticket == null)
            {
                return NotFound();
            }

            ticket.Comments = ticket.Comments.OrderByDescending(c => c.CreatedAt).ToList();
            return View(ticket);
        }

        public async Task<IActionResult> Create(int? customerId)
        {
            ViewBag.Customers = await _context.Customers
                .Select(c => new SelectListItem { Value = c.Id.ToString(), Text = c.FullName })
                .ToListAsync();
            return View(new Ticket { CustomerId = customerId ?? 0 });
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(Ticket ticket)
        {
            if (ModelState.IsValid)
            {
                ticket.CreatedAt = DateTime.UtcNow;
                _context.Add(ticket);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }

            ViewBag.Customers = await _context.Customers
                .Select(c => new SelectListItem { Value = c.Id.ToString(), Text = c.FullName })
                .ToListAsync();
            return View(ticket);
        }

        public async Task<IActionResult> Edit(int id)
        {
            var ticket = await _context.Tickets.FirstOrDefaultAsync(t => t.Id == id);
            if (ticket == null)
            {
                return NotFound();
            }

            ViewBag.Customers = await _context.Customers
                .Select(c => new SelectListItem { Value = c.Id.ToString(), Text = c.FullName })
                .ToListAsync();
            return View(ticket);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, Ticket input)
        {
            var ticket = await _context.Tickets.FirstOrDefaultAsync(t => t.Id == id);
            if (ticket == null)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                ticket.Title = input.Title;
                ticket.Description = input.Description;
                ticket.Status = input.Status;
                ticket.Priority = input.Priority;
                ticket.CustomerId = input.CustomerId;

                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Details), new { id = ticket.Id });
            }

            ViewBag.Customers = await _context.Customers
                .Select(c => new SelectListItem { Value = c.Id.ToString(), Text = c.FullName })
                .ToListAsync();
            return View(input);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Delete(int id)
        {
            var ticket = await _context.Tickets
                .Include(t => t.Comments)
                .FirstOrDefaultAsync(t => t.Id == id);

            if (ticket == null)
            {
                return NotFound();
            }

            _context.TicketComments.RemoveRange(ticket.Comments);
            _context.Tickets.Remove(ticket);
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        // Dodaje komentarz do zgloszenia, a przy braku autora ustawia wartosc domyslna.
        public async Task<IActionResult> AddComment(int id, string content, string author)
        {
            var ticket = await _context.Tickets.FirstOrDefaultAsync(t => t.Id == id);
            if (ticket == null)
            {
                return NotFound();
            }

            if (string.IsNullOrWhiteSpace(content))
            {
                TempData["Error"] = "Komentarz nie może być pusty.";
                return RedirectToAction(nameof(Details), new { id });
            }

            var comment = new TicketComment
            {
                TicketId = id,
                Content = content,
                Author = string.IsNullOrWhiteSpace(author) ? "Użytkownik" : author,
                CreatedAt = DateTime.UtcNow
            };

            _context.TicketComments.Add(comment);
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Details), new { id });
        }
    }
}
