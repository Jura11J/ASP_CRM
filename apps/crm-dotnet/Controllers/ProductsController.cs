using AspCrm.Data;
using AspCrm.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace AspCrm.Controllers
{
    [Authorize]
    public class ProductsController : Controller
    {
        private readonly AppDbContext _context;

        public ProductsController(AppDbContext context)
        {
            _context = context;
        }

        public async Task<IActionResult> Index(string? search, string? status)
        {
            var query = _context.Products.AsQueryable();

            if (!string.IsNullOrWhiteSpace(search))
            {
                var lower = search.ToLower();
                query = query.Where(p =>
                    p.Name.ToLower().Contains(lower) ||
                    p.Sku.ToLower().Contains(lower));
            }

            status = status?.ToLower();
            if (status == "active")
            {
                query = query.Where(p => p.IsActive);
            }
            else if (status == "inactive")
            {
                query = query.Where(p => !p.IsActive);
            }

            var products = await query
                .OrderBy(p => p.Name)
                .ToListAsync();

            ViewBag.Search = search;
            ViewBag.Status = status;
            return View(products);
        }

        public async Task<IActionResult> Details(int id)
        {
            var product = await _context.Products.IgnoreQueryFilters().FirstOrDefaultAsync(p => p.Id == id);
            if (product == null)
            {
                return NotFound();
            }

            return View(product);
        }

        public IActionResult Create()
        {
            return View();
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(Product product)
        {
            if (ModelState.IsValid)
            {
                _context.Add(product);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }

            return View(product);
        }

        public async Task<IActionResult> Edit(int id)
        {
            var product = await _context.Products.IgnoreQueryFilters().FirstOrDefaultAsync(p => p.Id == id);
            if (product == null)
            {
                return NotFound();
            }

            return View(product);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, Product input)
        {
            var product = await _context.Products.IgnoreQueryFilters().FirstOrDefaultAsync(p => p.Id == id);
            if (product == null)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                product.Name = input.Name;
                product.Sku = input.Sku;
                product.Price = input.Price;
                product.StockQuantity = input.StockQuantity;
                product.Description = input.Description;
                product.IsActive = input.IsActive;

                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Details), new { id = product.Id });
            }

            return View(input);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Delete(int id)
        {
            var product = await _context.Products.IgnoreQueryFilters().FirstOrDefaultAsync(p => p.Id == id);
            if (product == null)
            {
                return NotFound();
            }

            product.IsDeleted = true;
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }
    }
}
