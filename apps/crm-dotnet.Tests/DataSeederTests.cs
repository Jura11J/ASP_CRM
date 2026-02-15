using AspCrm.Data;
using Microsoft.AspNetCore.Identity;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;

namespace AspCrm.Tests;

public class DataSeederTests
{
    [Fact]
    public async Task SeedAsync_CreatesData_AndIsIdempotent()
    {
        await using var connection = new SqliteConnection("DataSource=:memory:");
        await connection.OpenAsync();

        var services = new ServiceCollection();
        services.AddLogging();
        services.AddDbContext<AppDbContext>(options => options.UseSqlite(connection));
        services
            .AddIdentityCore<IdentityUser>(options =>
            {
                options.Password.RequiredLength = 6;
                options.Password.RequireNonAlphanumeric = false;
                options.Password.RequireUppercase = false;
                options.Password.RequireLowercase = false;
                options.Password.RequireDigit = false;
            })
            .AddEntityFrameworkStores<AppDbContext>();

        using var provider = services.BuildServiceProvider();

        await DataSeeder.SeedAsync(provider);

        using (var scope = provider.CreateScope())
        {
            var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();
            Assert.True(await db.Customers.AnyAsync());
            Assert.True(await db.Products.AnyAsync());
            Assert.True(await db.Orders.AnyAsync());
            Assert.True(await db.Tickets.AnyAsync());
        }

        int initialCustomerCount;
        using (var scope = provider.CreateScope())
        {
            var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();
            initialCustomerCount = await db.Customers.CountAsync();
        }

        await DataSeeder.SeedAsync(provider);

        using (var scope = provider.CreateScope())
        {
            var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();
            var userManager = scope.ServiceProvider.GetRequiredService<UserManager<IdentityUser>>();
            Assert.Equal(initialCustomerCount, await db.Customers.CountAsync());
            Assert.True(await userManager.Users.AnyAsync());
        }
    }
}
