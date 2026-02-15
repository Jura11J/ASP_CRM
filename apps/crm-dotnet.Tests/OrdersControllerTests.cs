using AspCrm.Controllers;
using AspCrm.Models;
using AspCrm.ViewModels;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace AspCrm.Tests;

public class OrdersControllerTests
{
    [Fact]
    public async Task Index_ReturnsViewModelWithOrders()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();
        db.Orders.Add(new Order { CustomerId = customer.Id, Status = OrderStatus.New, TotalAmount = 12m, CreatedAt = DateTime.UtcNow });
        await db.SaveChangesAsync();

        var controller = new OrdersController(db);

        var result = await controller.Index(OrderStatus.New, customer.Id, null, null);

        var view = Assert.IsType<ViewResult>(result);
        var vm = Assert.IsType<OrderListViewModel>(view.Model);
        Assert.Single(vm.Orders);
        Assert.Single(vm.Customers);
    }

    [Fact]
    public async Task Details_ReturnsNotFound_WhenMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new OrdersController(db);

        var result = await controller.Details(100);

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Details_ReturnsOrderWithSortedHistory()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        var product = new Product { Name = "P", Sku = "S", Price = 2m, StockQuantity = 10 };
        db.AddRange(customer, product);
        await db.SaveChangesAsync();

        var order = new Order { CustomerId = customer.Id, Status = OrderStatus.New, TotalAmount = 2m, CreatedAt = DateTime.UtcNow };
        order.Items.Add(new OrderItem { ProductId = product.Id, Quantity = 1, UnitPrice = 2m, LineTotal = 2m });
        order.StatusHistory.Add(new OrderStatusHistory { Status = OrderStatus.Paid, ChangedAt = DateTime.UtcNow.AddHours(-2), Note = "old" });
        order.StatusHistory.Add(new OrderStatusHistory { Status = OrderStatus.New, ChangedAt = DateTime.UtcNow.AddHours(-1), Note = "new" });
        db.Orders.Add(order);
        await db.SaveChangesAsync();

        var controller = new OrdersController(db);

        var result = await controller.Details(order.Id);

        var view = Assert.IsType<ViewResult>(result);
        var model = Assert.IsType<Order>(view.Model);
        Assert.Equal(2, model.StatusHistory.Count);
        Assert.True(model.StatusHistory.First().ChangedAt >= model.StatusHistory.Last().ChangedAt);
    }

    [Fact]
    public async Task Create_Get_PopulatesLists()
    {
        using var db = TestSupport.CreateDbContext();
        db.Customers.Add(new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" });
        db.Products.Add(new Product { Name = "P", Sku = "S", Price = 2m, StockQuantity = 10 });
        await db.SaveChangesAsync();

        var controller = new OrdersController(db);

        var result = await controller.Create((int?)null);

        var view = Assert.IsType<ViewResult>(result);
        var vm = Assert.IsType<OrderFormViewModel>(view.Model);
        Assert.NotEmpty(vm.CustomerOptions);
        Assert.NotEmpty(vm.Products);
        Assert.NotEmpty(vm.Items);
    }

    [Fact]
    public async Task Create_Post_InvalidItems_ReturnsView()
    {
        using var db = TestSupport.CreateDbContext();
        db.Customers.Add(new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" });
        db.Products.Add(new Product { Name = "P", Sku = "S", Price = 2m, StockQuantity = 10 });
        await db.SaveChangesAsync();

        var controller = new OrdersController(db);
        var model = new OrderFormViewModel { CustomerId = 1, Items = new List<OrderItemInput>() };

        var result = await controller.Create(model);

        Assert.IsType<ViewResult>(result);
        Assert.False(controller.ModelState.IsValid);
    }

    [Fact]
    public async Task Create_Post_Valid_CreatesOrderAndHistory()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        var product = new Product { Name = "P", Sku = "S", Price = 5m, StockQuantity = 10 };
        db.AddRange(customer, product);
        await db.SaveChangesAsync();

        var controller = new OrdersController(db);
        var model = new OrderFormViewModel
        {
            CustomerId = customer.Id,
            Status = OrderStatus.Paid,
            Items = new List<OrderItemInput> { new() { ProductId = product.Id, Quantity = 3 } }
        };

        var result = await controller.Create(model);

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(OrdersController.Index), redirect.ActionName);

        var order = await db.Orders.Include(o => o.Items).Include(o => o.StatusHistory).FirstAsync();
        Assert.Equal(15m, order.TotalAmount);
        Assert.Single(order.Items);
        Assert.Single(order.StatusHistory);
        Assert.Equal(OrderStatus.Paid, order.StatusHistory.First().Status);
    }

    [Fact]
    public async Task Edit_Get_ReturnsNotFound_WhenMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new OrdersController(db);

        var result = await controller.Edit(99);

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Edit_Get_ReturnsViewModel_WhenFound()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        var product = new Product { Name = "P", Sku = "S", Price = 5m, StockQuantity = 10 };
        db.AddRange(customer, product);
        await db.SaveChangesAsync();

        var order = new Order { CustomerId = customer.Id, Status = OrderStatus.New, TotalAmount = 10m };
        order.Items.Add(new OrderItem { ProductId = product.Id, Quantity = 2, UnitPrice = 5m, LineTotal = 10m });
        db.Orders.Add(order);
        await db.SaveChangesAsync();

        var controller = new OrdersController(db);

        var result = await controller.Edit(order.Id);

        var view = Assert.IsType<ViewResult>(result);
        var vm = Assert.IsType<OrderFormViewModel>(view.Model);
        Assert.Equal(order.Id, vm.Id);
        Assert.Single(vm.Items);
    }

    [Fact]
    public async Task Edit_Post_ReturnsNotFound_ForIdMismatch()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new OrdersController(db);

        var result = await controller.Edit(2, new OrderFormViewModel { Id = 3 });

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Edit_Post_ReturnsNotFound_WhenOrderMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new OrdersController(db);

        var result = await controller.Edit(2, new OrderFormViewModel { Id = 2 });

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Edit_Post_InvalidItems_ReturnsView()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        db.Orders.Add(new Order { CustomerId = 1, Status = OrderStatus.New, TotalAmount = 0m });
        await db.SaveChangesAsync();

        var controller = new OrdersController(db);
        var model = new OrderFormViewModel { Id = 1, CustomerId = 1, Items = new List<OrderItemInput>() };

        var result = await controller.Edit(1, model);

        Assert.IsType<ViewResult>(result);
        Assert.False(controller.ModelState.IsValid);
    }

    [Fact]
    public async Task Edit_Post_Valid_ReplacesItems_AndAddsHistoryOnStatusChange()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        var product1 = new Product { Name = "P1", Sku = "S1", Price = 5m, StockQuantity = 10 };
        var product2 = new Product { Name = "P2", Sku = "S2", Price = 3m, StockQuantity = 10 };
        db.AddRange(customer, product1, product2);
        await db.SaveChangesAsync();

        var order = new Order { CustomerId = customer.Id, Status = OrderStatus.New, TotalAmount = 10m };
        order.Items.Add(new OrderItem { ProductId = product1.Id, Quantity = 2, UnitPrice = 5m, LineTotal = 10m });
        order.StatusHistory.Add(new OrderStatusHistory { Status = OrderStatus.New, ChangedAt = DateTime.UtcNow.AddMinutes(-10), Note = "start" });
        db.Orders.Add(order);
        await db.SaveChangesAsync();

        var controller = new OrdersController(db);
        var model = new OrderFormViewModel
        {
            Id = order.Id,
            CustomerId = customer.Id,
            Status = OrderStatus.Shipped,
            Items = new List<OrderItemInput> { new() { ProductId = product2.Id, Quantity = 2 } }
        };

        var result = await controller.Edit(order.Id, model);

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(OrdersController.Details), redirect.ActionName);

        var updated = await db.Orders.Include(o => o.Items).Include(o => o.StatusHistory).FirstAsync(o => o.Id == order.Id);
        Assert.Single(updated.Items);
        Assert.Equal(product2.Id, updated.Items.First().ProductId);
        Assert.Equal(6m, updated.TotalAmount);
        Assert.Equal(2, updated.StatusHistory.Count);
    }

    [Fact]
    public async Task Delete_ReturnsNotFound_WhenMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new OrdersController(db);

        var result = await controller.Delete(8);

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Delete_RemovesOrderAndRelatedData()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        var product = new Product { Name = "P", Sku = "S", Price = 1m, StockQuantity = 10 };
        db.AddRange(customer, product);
        await db.SaveChangesAsync();

        var order = new Order { CustomerId = customer.Id, Status = OrderStatus.New, TotalAmount = 2m };
        order.Items.Add(new OrderItem { ProductId = product.Id, Quantity = 2, UnitPrice = 1m, LineTotal = 2m });
        order.StatusHistory.Add(new OrderStatusHistory { Status = OrderStatus.New, ChangedAt = DateTime.UtcNow, Note = "n" });
        db.Orders.Add(order);
        await db.SaveChangesAsync();

        var controller = new OrdersController(db);

        var result = await controller.Delete(order.Id);

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(OrdersController.Index), redirect.ActionName);
        Assert.Empty(db.Orders);
        Assert.Empty(db.OrderItems);
        Assert.Empty(db.OrderStatusHistory);
    }
}
