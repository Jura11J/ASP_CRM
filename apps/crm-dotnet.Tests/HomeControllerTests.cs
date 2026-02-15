using AspCrm.Controllers;
using AspCrm.Models;
using AspCrm.ViewModels;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace AspCrm.Tests;

public class HomeControllerTests
{
    [Fact]
    public async Task Index_BuildsDashboardSummary()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1", IsActive = true };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        db.Orders.AddRange(
            new Order { CustomerId = customer.Id, Status = OrderStatus.New, TotalAmount = 50m, CreatedAt = DateTime.UtcNow },
            new Order { CustomerId = customer.Id, Status = OrderStatus.Cancelled, TotalAmount = 30m, CreatedAt = DateTime.UtcNow.AddDays(-1) });
        await db.SaveChangesAsync();

        var controller = new HomeController(db);

        var result = await controller.Index();

        var view = Assert.IsType<ViewResult>(result);
        var model = Assert.IsType<DashboardViewModel>(view.Model);
        Assert.Equal(1, model.CustomersCount);
        Assert.True(model.ActiveOrders >= 1);
        Assert.True(model.SalesValue >= 50m);
        Assert.NotNull(model.RecentOrders);
        Assert.NotNull(model.SalesChart);
    }

    [Fact]
    public void Error_ReturnsViewModelWithRequestId()
    {
        using var db = TestSupport.CreateDbContext();
        var controller = new HomeController(db)
        {
            ControllerContext = new ControllerContext
            {
                HttpContext = new DefaultHttpContext { TraceIdentifier = "trace-1" }
            }
        };

        var result = controller.Error();

        var view = Assert.IsType<ViewResult>(result);
        var model = Assert.IsType<ErrorViewModel>(view.Model);
        Assert.True(model.ShowRequestId);
    }
}
