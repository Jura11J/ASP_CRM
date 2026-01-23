using AspCrm.Data;
using AspCrm.Models;
using AspCrm.ViewModels;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Diagnostics;

namespace AspCrm.Controllers;

[Authorize]
public class HomeController : Controller
{
    private readonly AppDbContext _context;

    public HomeController(AppDbContext context)
    {
        _context = context;
    }

    // Dashboard z podsumowaniami sprzedaży i ostatnimi zamówieniami.
    public async Task<IActionResult> Index()
    {
        var now = DateTime.UtcNow;
        var monthStart = new DateTime(now.Year, now.Month, 1, 0, 0, 0, DateTimeKind.Utc);
        var activeStatuses = new[] { OrderStatus.New, OrderStatus.Paid, OrderStatus.Shipped };

        var salesValue = await _context.Orders
            .Where(o => o.Status != OrderStatus.Cancelled)
            .Select(o => (double)o.TotalAmount)
            .SumAsync();

        var model = new DashboardViewModel
        {
            CustomersCount = await _context.Customers.CountAsync(),
            ActiveOrders = await _context.Orders.CountAsync(o => activeStatuses.Contains(o.Status)),
            OrdersThisMonth = await _context.Orders.CountAsync(o => o.CreatedAt >= monthStart),
            SalesValue = (decimal)salesValue
        };

        model.RecentOrders = await _context.Orders
            .Include(o => o.Customer)
            .Include(o => o.Items)
            .OrderByDescending(o => o.CreatedAt)
            .Take(10)
            .ToListAsync();

        var chartFromDate = DateTime.SpecifyKind(now.Date.AddDays(-29), DateTimeKind.Utc);
        model.SalesChart = await _context.Orders
            .Where(o => o.CreatedAt >= chartFromDate && o.Status != OrderStatus.Cancelled)
            .Select(o => new
            {
                Date = DateTime.SpecifyKind(o.CreatedAt, DateTimeKind.Utc).Date,
                o.TotalAmount
            })
            .GroupBy(x => x.Date)
            .Select(g => new SalesChartPoint
            {
                Date = g.Key,
                Total = (decimal)g.Sum(x => (double)x.TotalAmount)
            })
            .OrderBy(g => g.Date)
            .ToListAsync();

        return View(model);
    }

    [ResponseCache(Duration = 0, Location = ResponseCacheLocation.None, NoStore = true)]
    public IActionResult Error()
    {
        return View(new ErrorViewModel { RequestId = Activity.Current?.Id ?? HttpContext.TraceIdentifier });
    }
}
