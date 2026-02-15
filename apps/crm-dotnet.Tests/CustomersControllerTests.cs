using AspCrm.Controllers;
using AspCrm.Models;
using AspCrm.ViewModels;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace AspCrm.Tests;

public class CustomersControllerTests
{
    [Fact]
    public async Task Index_FiltersBySearchAndStatus()
    {
        using var db = TestSupport.CreateDbContext();
        db.Customers.AddRange(
            new Customer { FirstName = "Anna", LastName = "Nowak", Email = "anna@demo.pl", Phone = "1", IsActive = true, CreatedAt = DateTime.UtcNow },
            new Customer { FirstName = "Jan", LastName = "Kowalski", Email = "jan@demo.pl", Phone = "2", IsActive = false, CreatedAt = DateTime.UtcNow.AddMinutes(-1) });
        await db.SaveChangesAsync();

        var controller = new CustomersController(db);

        var result = await controller.Index("anna", "active");

        var view = Assert.IsType<ViewResult>(result);
        var model = Assert.IsAssignableFrom<List<Customer>>(view.Model);
        Assert.Single(model);
        Assert.Equal("anna@demo.pl", model[0].Email);
    }

    [Fact]
    public async Task Details_ReturnsNotFound_WhenCustomerMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new CustomersController(db);

        var result = await controller.Details(42);

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Details_ReturnsViewModel_WhenCustomerExists()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "123" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        db.Orders.Add(new Order { CustomerId = customer.Id, Status = OrderStatus.New, TotalAmount = 10m, CreatedAt = DateTime.UtcNow });
        db.CustomerNotes.Add(new CustomerNote { CustomerId = customer.Id, Author = "op", Content = "note", CreatedAt = DateTime.UtcNow });
        db.Tickets.Add(new Ticket { CustomerId = customer.Id, Title = "t", Description = "d", CreatedAt = DateTime.UtcNow });
        await db.SaveChangesAsync();

        var controller = new CustomersController(db);

        var result = await controller.Details(customer.Id);

        var view = Assert.IsType<ViewResult>(result);
        var vm = Assert.IsType<CustomerDetailViewModel>(view.Model);
        Assert.Equal(customer.Id, vm.Customer.Id);
        Assert.Equal(customer.Id, vm.NewNote.CustomerId);
    }

    [Fact]
    public void Create_Get_ReturnsView()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new CustomersController(db);

        var result = controller.Create();

        Assert.IsType<ViewResult>(result);
    }

    [Fact]
    public async Task Create_Post_Valid_PersistsAndRedirects()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new CustomersController(db);
        var customer = new Customer { FirstName = "Jan", LastName = "Nowak", Email = "jn@demo.pl", Phone = "123" };

        var result = await controller.Create(customer);

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(CustomersController.Index), redirect.ActionName);
        Assert.Single(db.Customers);
    }

    [Fact]
    public async Task Create_Post_Invalid_ReturnsView()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new CustomersController(db);
        controller.ModelState.AddModelError("Email", "required");

        var result = await controller.Create(new Customer());

        Assert.IsType<ViewResult>(result);
    }

    [Fact]
    public async Task Edit_Get_ReturnsNotFound_WhenMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new CustomersController(db);

        var result = await controller.Edit(123);

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Edit_Get_ReturnsView_ForDeletedCustomerBecauseIgnoreFilters()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "Del", LastName = "User", Email = "d@u.pl", Phone = "123", IsDeleted = true };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        var controller = new CustomersController(db);

        var result = await controller.Edit(customer.Id);

        var view = Assert.IsType<ViewResult>(result);
        var model = Assert.IsType<Customer>(view.Model);
        Assert.Equal(customer.Id, model.Id);
    }

    [Fact]
    public async Task Edit_Post_ReturnsNotFound_WhenMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new CustomersController(db);

        var result = await controller.Edit(9, new Customer());

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Edit_Post_InvalidModel_ReturnsView()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "123" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        var controller = new CustomersController(db);
        controller.ModelState.AddModelError("FirstName", "required");

        var result = await controller.Edit(customer.Id, new Customer());

        Assert.IsType<ViewResult>(result);
    }

    [Fact]
    public async Task Edit_Post_Valid_UpdatesCustomerAndRedirects()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "123" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        var controller = new CustomersController(db);

        var result = await controller.Edit(customer.Id, new Customer
        {
            FirstName = "New",
            LastName = "Name",
            Email = "new@demo.pl",
            Phone = "999",
            IsActive = false
        });

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(CustomersController.Details), redirect.ActionName);

        var updated = await db.Customers.IgnoreQueryFilters().FirstAsync(c => c.Id == customer.Id);
        Assert.Equal("New", updated.FirstName);
        Assert.False(updated.IsActive);
    }

    [Fact]
    public async Task Delete_ReturnsNotFound_WhenMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new CustomersController(db);

        var result = await controller.Delete(99);

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Delete_SetsSoftDeleteFlag_AndRedirects()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "123" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        var controller = new CustomersController(db);

        var result = await controller.Delete(customer.Id);

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(CustomersController.Index), redirect.ActionName);
        Assert.True((await db.Customers.IgnoreQueryFilters().FirstAsync()).IsDeleted);
    }

    [Fact]
    public async Task AddNote_ReturnsNotFound_WhenCustomerMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new CustomersController(db) { TempData = TestSupport.CreateTempData() };

        var result = await controller.AddNote(new CustomerNote { CustomerId = 999, Content = "x", Author = "a" });

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task AddNote_EmptyContent_SetsErrorAndRedirects()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "123" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        var controller = new CustomersController(db) { TempData = TestSupport.CreateTempData() };

        var result = await controller.AddNote(new CustomerNote { CustomerId = customer.Id, Content = "   ", Author = "a" });

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(CustomersController.Details), redirect.ActionName);
        Assert.Equal("Treœæ notatki jest wymagana.", controller.TempData["Error"]);
    }

    [Fact]
    public async Task AddNote_Valid_PersistsAndRedirects()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "123" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        var controller = new CustomersController(db) { TempData = TestSupport.CreateTempData() };

        var result = await controller.AddNote(new CustomerNote { CustomerId = customer.Id, Content = "Hello", Author = "Op" });

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(CustomersController.Details), redirect.ActionName);
        Assert.Single(db.CustomerNotes);
    }
}
