using AspCrm.Data;
using AspCrm.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace AspCrm.Controllers.Api
{
    [ApiController]
    [Route("api/shop/tickets")]
    public class ShopTicketsController : ControllerBase
    {
        private readonly AppDbContext _ctx;

        public ShopTicketsController(AppDbContext ctx)
        {
            _ctx = ctx;
        }

        public class TicketRequest
        {
            public string Email { get; set; } = string.Empty;
            public string FirstName { get; set; } = string.Empty;
            public string LastName { get; set; } = string.Empty;
            public string? Phone { get; set; }
            public string Title { get; set; } = string.Empty;
            public string Description { get; set; } = string.Empty;
            public string? Priority { get; set; }
        }

        [HttpPost]
        public async Task<IActionResult> Post([FromBody] TicketRequest req)
        {
            if (string.IsNullOrWhiteSpace(req.Email) || string.IsNullOrWhiteSpace(req.Title) || string.IsNullOrWhiteSpace(req.Description))
            {
                return BadRequest("Email, Title i Description są wymagane.");
            }

            var email = req.Email.Trim().ToLower();
            var customer = await _ctx.Customers.FirstOrDefaultAsync(c => c.Email.ToLower() == email);
            if (customer == null)
            {
                customer = new Customer
                {
                    Email = req.Email.Trim(),
                    FirstName = req.FirstName ?? string.Empty,
                    LastName = req.LastName ?? string.Empty,
                    Phone = req.Phone ?? string.Empty,
                    IsActive = true,
                    City = string.Empty,
                    AddressLine1 = string.Empty
                };
                _ctx.Customers.Add(customer);
                await _ctx.SaveChangesAsync();
            }

            var ticket = new Ticket
            {
                CustomerId = customer.Id,
                Title = req.Title.Trim(),
                Description = req.Description.Trim(),
                Priority = MapPriority(req.Priority),
                Status = TicketStatus.Open,
                CreatedAt = DateTime.UtcNow
            };

            _ctx.Tickets.Add(ticket);
            await _ctx.SaveChangesAsync();

            return Ok(new { ticket.Id });
        }

        private TicketPriority MapPriority(string? p)
        {
            return p?.ToLower() switch
            {
                "low" => TicketPriority.Low,
                "high" => TicketPriority.High,
                _ => TicketPriority.Medium
            };
        }
    }
}
