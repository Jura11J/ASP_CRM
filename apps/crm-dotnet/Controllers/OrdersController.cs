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
    public class OrdersController : Controller
    {
        private readonly AppDbContext _context;

        public OrdersController(AppDbContext context)
        {
            _context = context;
        }

        public async Task<IActionResult> Index(OrderStatus? status, int? customerId, DateTime? fromDate, DateTime? toDate)
        {
            var query = _context.Orders
                .Include(o => o.Customer)
                .Include(o => o.Items)
                .AsQueryable();

            if (status.HasValue)
            {
                query = query.Where(o => o.Status == status);
            }

            if (customerId.HasValue)
            {
                query = query.Where(o => o.CustomerId == customerId.Value);
            }

            if (fromDate.HasValue)
            {
                query = query.Where(o => o.CreatedAt >= fromDate.Value);
            }

            if (toDate.HasValue)
            {
                query = query.Where(o => o.CreatedAt <= toDate.Value);
            }

            var orders = await query
                .OrderByDescending(o => o.CreatedAt)
                .ToListAsync();

            var customers = await _context.Customers
                .Select(c => new SelectListItem { Value = c.Id.ToString(), Text = c.FullName })
                .ToListAsync();

            var vm = new OrderListViewModel
            {
                Orders = orders,
                Status = status,
                CustomerId = customerId,
                FromDate = fromDate,
                ToDate = toDate,
                Customers = customers
            };

            return View(vm);
        }

        public async Task<IActionResult> Details(int id)
        {
            var order = await _context.Orders
                .Include(o => o.Customer)
                .Include(o => o.Items)
                    .ThenInclude(i => i.Product)
                .Include(o => o.StatusHistory)
                .FirstOrDefaultAsync(o => o.Id == id);

            if (order == null)
            {
                return NotFound();
            }

            order.StatusHistory = order.StatusHistory.OrderByDescending(s => s.ChangedAt).ToList();
            return View(order);
        }

        public async Task<IActionResult> Create(int? customerId)
        {
            var model = new OrderFormViewModel
            {
                Items = new List<OrderItemInput> { new() },
                CustomerId = customerId ?? 0
            };
            await PopulateLists(model);
            return View(model);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(OrderFormViewModel model)
        {
            model.Items = (model.Items ?? new List<OrderItemInput>()).Where(i => i.ProductId > 0 && i.Quantity > 0).ToList();
            if (!model.Items.Any())
            {
                ModelState.AddModelError("Items", "Dodaj co najmniej jedną pozycję.");
            }

            if (!ModelState.IsValid)
            {
                await PopulateLists(model);
                return View(model);
            }

            var order = new Order
            {
                CustomerId = model.CustomerId,
                CreatedAt = DateTime.UtcNow,
                Status = model.Status
            };

            var total = await BuildOrderItems(model, order);
            order.TotalAmount = total;
                order.StatusHistory.Add(new OrderStatusHistory
                {
                    Status = order.Status,
                    ChangedAt = DateTime.UtcNow,
                    Note = "Utworzono zamówienie"
                });

            _context.Orders.Add(order);
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        public async Task<IActionResult> Edit(int id)
        {
            var order = await _context.Orders
                .Include(o => o.Items)
                .FirstOrDefaultAsync(o => o.Id == id);

            if (order == null)
            {
                return NotFound();
            }

            var model = new OrderFormViewModel
            {
                Id = order.Id,
                CustomerId = order.CustomerId,
                Status = order.Status,
                Items = order.Items.Select(i => new OrderItemInput
                {
                    ProductId = i.ProductId,
                    Quantity = i.Quantity
                }).ToList()
            };

            await PopulateLists(model);
            return View(model);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, OrderFormViewModel model)
        {
            if (id != model.Id)
            {
                return NotFound();
            }

            var order = await _context.Orders
                .Include(o => o.Items)
                .Include(o => o.StatusHistory)
                .FirstOrDefaultAsync(o => o.Id == id);

            if (order == null)
            {
                return NotFound();
            }

            model.Items = (model.Items ?? new List<OrderItemInput>()).Where(i => i.ProductId > 0 && i.Quantity > 0).ToList();
            if (!model.Items.Any())
            {
                ModelState.AddModelError("Items", "Dodaj co najmniej jedną pozycję.");
            }

            if (!ModelState.IsValid)
            {
                await PopulateLists(model);
                return View(model);
            }

            var previousStatus = order.Status;
            order.CustomerId = model.CustomerId;
            order.Status = model.Status;

            var existingItems = order.Items.ToList();
            _context.OrderItems.RemoveRange(existingItems);
            order.Items.Clear();

            order.TotalAmount = await BuildOrderItems(model, order);

            if (previousStatus != order.Status)
            {
                order.StatusHistory.Add(new OrderStatusHistory
                {
                    Status = order.Status,
                    ChangedAt = DateTime.UtcNow,
                    Note = "Zmiana statusu"
                });
            }

            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Details), new { id = order.Id });
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Delete(int id)
        {
            var order = await _context.Orders
                .Include(o => o.Items)
                .Include(o => o.StatusHistory)
                .FirstOrDefaultAsync(o => o.Id == id);

            if (order == null)
            {
                return NotFound();
            }

            _context.OrderItems.RemoveRange(order.Items);
            _context.OrderStatusHistory.RemoveRange(order.StatusHistory);
            _context.Orders.Remove(order);
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private async Task PopulateLists(OrderFormViewModel model)
        {
            model.CustomerOptions = await _context.Customers
                .Select(c => new SelectListItem { Value = c.Id.ToString(), Text = c.FullName })
                .ToListAsync();

            model.Products = await _context.Products.OrderBy(p => p.Name).ToListAsync();

            if (!model.Items.Any())
            {
                model.Items.Add(new OrderItemInput());
            }
        }

        private async Task<decimal> BuildOrderItems(OrderFormViewModel model, Order order)
        {
            decimal total = 0;
            foreach (var input in model.Items)
            {
                var product = await _context.Products.FirstOrDefaultAsync(p => p.Id == input.ProductId);
                if (product == null)
                {
                    continue;
                }

                var lineTotal = product.Price * input.Quantity;
                order.Items.Add(new OrderItem
                {
                    ProductId = product.Id,
                    Quantity = input.Quantity,
                    UnitPrice = product.Price,
                    LineTotal = lineTotal
                });
                total += lineTotal;
            }

            return total;
        }
    }
}
