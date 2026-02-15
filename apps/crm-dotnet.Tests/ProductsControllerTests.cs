using AspCrm.Controllers;
using AspCrm.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace AspCrm.Tests;

public class ProductsControllerTests
{
    [Fact]
    public async Task Index_FiltersBySearchAndStatus()
    {
        using var db = TestSupport.CreateDbContext();
        db.Products.AddRange(
            new Product { Name = "Alpha", Sku = "A-1", Price = 10, StockQuantity = 10, IsActive = true },
            new Product { Name = "Beta", Sku = "B-2", Price = 20, StockQuantity = 10, IsActive = false });
        await db.SaveChangesAsync();

        var controller = new ProductsController(db);

        var result = await controller.Index("alpha", "active");

        var view = Assert.IsType<ViewResult>(result);
        var model = Assert.IsAssignableFrom<List<Product>>(view.Model);
        Assert.Single(model);
        Assert.Equal("Alpha", model[0].Name);
    }

    [Fact]
    public async Task Details_ReturnsNotFound_WhenMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new ProductsController(db);

        var result = await controller.Details(44);

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Details_ReturnsDeletedProduct_BecauseIgnoreFilters()
    {
        using var db = TestSupport.CreateDbContext();
        var product = new Product { Name = "X", Sku = "X1", Price = 1, StockQuantity = 1, IsDeleted = true };
        db.Products.Add(product);
        await db.SaveChangesAsync();

        var controller = new ProductsController(db);

        var result = await controller.Details(product.Id);

        var view = Assert.IsType<ViewResult>(result);
        Assert.IsType<Product>(view.Model);
    }

    [Fact]
    public void Create_Get_ReturnsView()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new ProductsController(db);

        Assert.IsType<ViewResult>(controller.Create());
    }

    [Fact]
    public async Task Create_Post_Valid_PersistsAndRedirects()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new ProductsController(db);

        var result = await controller.Create(new Product { Name = "N", Sku = "S", Price = 1, StockQuantity = 1 });

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(ProductsController.Index), redirect.ActionName);
        Assert.Single(db.Products);
    }

    [Fact]
    public async Task Create_Post_Invalid_ReturnsView()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new ProductsController(db);
        controller.ModelState.AddModelError("Name", "required");

        var result = await controller.Create(new Product());

        Assert.IsType<ViewResult>(result);
    }

    [Fact]
    public async Task Edit_Get_ReturnsNotFound_WhenMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new ProductsController(db);

        var result = await controller.Edit(5);

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Edit_Get_ReturnsView_WhenFound()
    {
        using var db = TestSupport.CreateDbContext();
        var product = new Product { Name = "X", Sku = "Y", Price = 1, StockQuantity = 1 };
        db.Products.Add(product);
        await db.SaveChangesAsync();
        var controller = new ProductsController(db);

        var result = await controller.Edit(product.Id);

        var view = Assert.IsType<ViewResult>(result);
        Assert.IsType<Product>(view.Model);
    }

    [Fact]
    public async Task Edit_Post_ReturnsNotFound_WhenMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new ProductsController(db);

        var result = await controller.Edit(7, new Product());

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Edit_Post_InvalidModel_ReturnsView()
    {
        using var db = TestSupport.CreateDbContext();
        var product = new Product { Name = "X", Sku = "Y", Price = 1, StockQuantity = 1 };
        db.Products.Add(product);
        await db.SaveChangesAsync();

        var controller = new ProductsController(db);
        controller.ModelState.AddModelError("Name", "required");

        var result = await controller.Edit(product.Id, new Product());

        Assert.IsType<ViewResult>(result);
    }

    [Fact]
    public async Task Edit_Post_Valid_UpdatesAndRedirects()
    {
        using var db = TestSupport.CreateDbContext();
        var product = new Product { Name = "Old", Sku = "S1", Price = 1, StockQuantity = 1 };
        db.Products.Add(product);
        await db.SaveChangesAsync();

        var controller = new ProductsController(db);

        var result = await controller.Edit(product.Id, new Product
        {
            Name = "New",
            Sku = "S2",
            Price = 9,
            StockQuantity = 20,
            Description = "Desc",
            IsActive = false
        });

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(ProductsController.Details), redirect.ActionName);

        var updated = await db.Products.IgnoreQueryFilters().FirstAsync();
        Assert.Equal("New", updated.Name);
        Assert.False(updated.IsActive);
    }

    [Fact]
    public async Task Delete_ReturnsNotFound_WhenMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new ProductsController(db);

        var result = await controller.Delete(3);

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Delete_SoftDeletesAndRedirects()
    {
        using var db = TestSupport.CreateDbContext();
        var product = new Product { Name = "Old", Sku = "S1", Price = 1, StockQuantity = 1 };
        db.Products.Add(product);
        await db.SaveChangesAsync();

        var controller = new ProductsController(db);

        var result = await controller.Delete(product.Id);

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(ProductsController.Index), redirect.ActionName);
        Assert.True((await db.Products.IgnoreQueryFilters().FirstAsync()).IsDeleted);
    }
}
