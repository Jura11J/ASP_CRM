using AspCrm.Controllers;
using AspCrm.Models;
using AspCrm.ViewModels;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace AspCrm.Tests;

public class TicketsControllerTests
{
    [Fact]
    public async Task Index_FiltersAndReturnsViewModel()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        db.Tickets.AddRange(
            new Ticket { CustomerId = customer.Id, Title = "T1", Description = "D", Status = TicketStatus.Open, Priority = TicketPriority.High, CreatedAt = DateTime.UtcNow },
            new Ticket { CustomerId = customer.Id, Title = "T2", Description = "D", Status = TicketStatus.Closed, Priority = TicketPriority.Low, CreatedAt = DateTime.UtcNow.AddMinutes(-1) });
        await db.SaveChangesAsync();

        var controller = new TicketsController(db);

        var result = await controller.Index(TicketStatus.Open, TicketPriority.High, customer.Id);

        var view = Assert.IsType<ViewResult>(result);
        var vm = Assert.IsType<TicketListViewModel>(view.Model);
        Assert.Single(vm.Tickets);
        Assert.Single(vm.Customers);
    }

    [Fact]
    public async Task Details_ReturnsNotFound_WhenMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new TicketsController(db);

        var result = await controller.Details(99);

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Details_ReturnsTicketWithSortedComments()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        var ticket = new Ticket { CustomerId = customer.Id, Title = "T", Description = "D", CreatedAt = DateTime.UtcNow };
        ticket.Comments.Add(new TicketComment { Author = "1", Content = "old", CreatedAt = DateTime.UtcNow.AddHours(-2) });
        ticket.Comments.Add(new TicketComment { Author = "2", Content = "new", CreatedAt = DateTime.UtcNow.AddHours(-1) });
        db.Tickets.Add(ticket);
        await db.SaveChangesAsync();

        var controller = new TicketsController(db);

        var result = await controller.Details(ticket.Id);

        var view = Assert.IsType<ViewResult>(result);
        var model = Assert.IsType<Ticket>(view.Model);
        Assert.Equal(2, model.Comments.Count);
        Assert.True(model.Comments.First().CreatedAt >= model.Comments.Last().CreatedAt);
    }

    [Fact]
    public async Task Create_Get_ReturnsViewWithCustomerList()
    {
        using var db = TestSupport.CreateDbContext();
        db.Customers.Add(new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" });
        await db.SaveChangesAsync();
        var controller = new TicketsController(db);

        var result = await controller.Create((int?)null);

        var view = Assert.IsType<ViewResult>(result);
        Assert.IsType<Ticket>(view.Model);
        Assert.NotNull(controller.ViewBag.Customers);
    }

    [Fact]
    public async Task Create_Post_Valid_PersistsAndRedirects()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        var controller = new TicketsController(db);

        var result = await controller.Create(new Ticket { CustomerId = customer.Id, Title = "X", Description = "Y" });

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(TicketsController.Index), redirect.ActionName);
        Assert.Single(db.Tickets);
    }

    [Fact]
    public async Task Create_Post_Invalid_ReturnsViewAndCustomers()
    {
        using var db = TestSupport.CreateDbContext();
        db.Customers.Add(new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" });
        await db.SaveChangesAsync();

        var controller = new TicketsController(db);
        controller.ModelState.AddModelError("Title", "required");

        var result = await controller.Create(new Ticket());

        Assert.IsType<ViewResult>(result);
        Assert.NotNull(controller.ViewBag.Customers);
    }

    [Fact]
    public async Task Edit_Get_ReturnsNotFound_WhenMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new TicketsController(db);

        var result = await controller.Edit(11);

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Edit_Get_ReturnsView_WhenFound()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();
        var ticket = new Ticket { CustomerId = customer.Id, Title = "X", Description = "Y" };
        db.Tickets.Add(ticket);
        await db.SaveChangesAsync();

        var controller = new TicketsController(db);

        var result = await controller.Edit(ticket.Id);

        var view = Assert.IsType<ViewResult>(result);
        Assert.IsType<Ticket>(view.Model);
        Assert.NotNull(controller.ViewBag.Customers);
    }

    [Fact]
    public async Task Edit_Post_ReturnsNotFound_WhenMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new TicketsController(db);

        var result = await controller.Edit(22, new Ticket());

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Edit_Post_Invalid_ReturnsViewAndCustomers()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();
        var ticket = new Ticket { CustomerId = customer.Id, Title = "X", Description = "Y" };
        db.Tickets.Add(ticket);
        await db.SaveChangesAsync();

        var controller = new TicketsController(db);
        controller.ModelState.AddModelError("Title", "required");

        var result = await controller.Edit(ticket.Id, new Ticket { CustomerId = customer.Id });

        Assert.IsType<ViewResult>(result);
        Assert.NotNull(controller.ViewBag.Customers);
    }

    [Fact]
    public async Task Edit_Post_Valid_UpdatesAndRedirects()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();
        var ticket = new Ticket { CustomerId = customer.Id, Title = "X", Description = "Y", Status = TicketStatus.Open, Priority = TicketPriority.Low };
        db.Tickets.Add(ticket);
        await db.SaveChangesAsync();

        var controller = new TicketsController(db);

        var result = await controller.Edit(ticket.Id, new Ticket
        {
            CustomerId = customer.Id,
            Title = "New",
            Description = "Desc",
            Status = TicketStatus.Closed,
            Priority = TicketPriority.High
        });

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(TicketsController.Details), redirect.ActionName);

        var updated = await db.Tickets.FirstAsync();
        Assert.Equal("New", updated.Title);
        Assert.Equal(TicketStatus.Closed, updated.Status);
    }

    [Fact]
    public async Task Delete_ReturnsNotFound_WhenMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new TicketsController(db);

        var result = await controller.Delete(4);

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Delete_RemovesTicketAndComments()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        var ticket = new Ticket { CustomerId = customer.Id, Title = "X", Description = "Y" };
        ticket.Comments.Add(new TicketComment { Author = "A", Content = "C" });
        db.Tickets.Add(ticket);
        await db.SaveChangesAsync();

        var controller = new TicketsController(db);

        var result = await controller.Delete(ticket.Id);

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(TicketsController.Index), redirect.ActionName);
        Assert.Empty(db.Tickets);
        Assert.Empty(db.TicketComments);
    }

    [Fact]
    public async Task AddComment_ReturnsNotFound_WhenTicketMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new TicketsController(db) { TempData = TestSupport.CreateTempData() };

        var result = await controller.AddComment(1, "hi", "me");

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task AddComment_EmptyContent_SetsErrorAndRedirects()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();
        var ticket = new Ticket { CustomerId = customer.Id, Title = "X", Description = "Y" };
        db.Tickets.Add(ticket);
        await db.SaveChangesAsync();

        var controller = new TicketsController(db) { TempData = TestSupport.CreateTempData() };

        var result = await controller.AddComment(ticket.Id, "   ", "me");

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(TicketsController.Details), redirect.ActionName);
        Assert.NotNull(controller.TempData["Error"]);
    }

    [Fact]
    public async Task AddComment_Valid_AddsCommentAndRedirects()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();
        var ticket = new Ticket { CustomerId = customer.Id, Title = "X", Description = "Y" };
        db.Tickets.Add(ticket);
        await db.SaveChangesAsync();

        var controller = new TicketsController(db) { TempData = TestSupport.CreateTempData() };

        var result = await controller.AddComment(ticket.Id, "ok", "");

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal(nameof(TicketsController.Details), redirect.ActionName);
        var comment = await db.TicketComments.FirstAsync();
        Assert.Equal("Użytkownik", comment.Author);
    }
}

