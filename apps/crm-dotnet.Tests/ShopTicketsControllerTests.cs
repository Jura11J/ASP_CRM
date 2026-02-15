using AspCrm.Controllers.Api;
using AspCrm.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace AspCrm.Tests;

public class ShopTicketsControllerTests
{
    [Fact]
    public async Task Post_ReturnsBadRequest_WhenRequiredFieldsMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new ShopTicketsController(db);

        var result = await controller.Post(new ShopTicketsController.TicketRequest());

        Assert.IsType<BadRequestObjectResult>(result);
    }

    [Fact]
    public async Task Post_CreatesCustomerAndTicket_WhenCustomerMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new ShopTicketsController(db);

        var result = await controller.Post(new ShopTicketsController.TicketRequest
        {
            Email = "new@demo.pl",
            FirstName = "New",
            LastName = "User",
            Phone = "123",
            Title = "Issue",
            Description = "Description",
            Priority = "high"
        });

        var ok = Assert.IsType<OkObjectResult>(result);
        Assert.NotNull(ok.Value);
        Assert.Single(db.Customers);
        var ticket = await db.Tickets.FirstAsync();
        Assert.Equal(TicketPriority.High, ticket.Priority);
    }

    [Fact]
    public async Task Post_UsesExistingCustomer_AndDefaultsPriorityToMedium()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer
        {
            FirstName = "Existing",
            LastName = "User",
            Email = "existing@demo.pl",
            Phone = "111",
            IsActive = true
        };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        var controller = new ShopTicketsController(db);

        var result = await controller.Post(new ShopTicketsController.TicketRequest
        {
            Email = "existing@demo.pl",
            Title = "Issue",
            Description = "Description",
            Priority = "unknown"
        });

        Assert.IsType<OkObjectResult>(result);
        Assert.Single(db.Customers);
        var ticket = await db.Tickets.FirstAsync();
        Assert.Equal(customer.Id, ticket.CustomerId);
        Assert.Equal(TicketPriority.Medium, ticket.Priority);
    }
}
