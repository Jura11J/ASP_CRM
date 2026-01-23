using AspCrm.Models;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;

namespace AspCrm.Data
{
    public static class DataSeeder
    {
        // Tworzy bazę, uruchamia migracje, zakłada użytkownika demo i zasila danymi testowymi (z prostym retry pod Dockerem).
        public static async Task SeedAsync(IServiceProvider services)
        {
            using var scope = services.CreateScope();
            var context = scope.ServiceProvider.GetRequiredService<AppDbContext>();
            var userManager = scope.ServiceProvider.GetRequiredService<UserManager<IdentityUser>>();

            // Retry na czas startu kontenera Postgres.
            var retries = 10;
            for (var attempt = 1; attempt <= retries; attempt++)
            {
                try
                {
                    await context.Database.MigrateAsync();
                    break;
                }
                catch (Exception) when (attempt < retries)
                {
                    await Task.Delay(TimeSpan.FromSeconds(3));
                }
            }

            // Użytkownik demo (login: admin@demo.pl, hasło: Admin123!)
            if (!await userManager.Users.AnyAsync())
            {
                var demoUser = new IdentityUser
                {
                    UserName = "admin@demo.pl",
                    Email = "admin@demo.pl",
                    EmailConfirmed = true
                };
                await userManager.CreateAsync(demoUser, "Admin123!");
            }

            if (await context.Customers.AnyAsync())
            {
                return;
            }

            var random = new Random(42);
            var now = DateTime.UtcNow;

            var customers = Enumerable.Range(1, 10).Select(i => new Customer
            {
                FirstName = $"Customer{i}",
                LastName = "Demo",
                Email = $"customer{i}@shop.test",
                Phone = $"+48 600 00{i:000}",
                AddressLine1 = $"Street {i}",
                City = "Warsaw",
                PreferredContactMethod = i % 2 == 0 ? "Email" : "Phone",
                MarketingConsent = i % 3 != 0,
                IsActive = i % 4 != 0,
                CreatedAt = now.AddDays(-i)
            }).ToList();

            await context.Customers.AddRangeAsync(customers);
            await context.SaveChangesAsync();

            var products = Enumerable.Range(1, 10).Select(i => new Product
            {
                Name = $"Product {i}",
                Sku = $"SKU-{1000 + i}",
                Price = 50 + i * 10,
                StockQuantity = 100 - i * 3,
                Description = $"Sample description for product {i}",
                IsActive = i % 5 != 0
            }).ToList();

            await context.Products.AddRangeAsync(products);
            await context.SaveChangesAsync();

            var orders = new List<Order>();
            for (var i = 0; i < 12; i++)
            {
                var created = now.AddDays(-random.Next(0, 28));
                var status = (OrderStatus)random.Next(0, 5);
                var customer = customers[random.Next(customers.Count)];

                var order = new Order
                {
                    CustomerId = customer.Id,
                    CreatedAt = created,
                    Status = status
                };

                var itemCount = random.Next(1, 4);
                for (var j = 0; j < itemCount; j++)
                {
                    var product = products[random.Next(products.Count)];
                    var quantity = random.Next(1, 4);
                    var unitPrice = product.Price;
                    order.Items.Add(new OrderItem
                    {
                        ProductId = product.Id,
                        Quantity = quantity,
                        UnitPrice = unitPrice,
                        LineTotal = unitPrice * quantity
                    });
                }

                order.TotalAmount = order.Items.Sum(i => i.LineTotal);
                order.StatusHistory.Add(new OrderStatusHistory
                {
                    Status = status,
                    ChangedAt = created,
                    Note = "Status poczatkowy"
                });

                orders.Add(order);
            }

            await context.Orders.AddRangeAsync(orders);
            await context.SaveChangesAsync();

            var tickets = new List<Ticket>();
            foreach (var customer in customers.Take(6))
            {
                var ticket = new Ticket
                {
                    CustomerId = customer.Id,
                    Title = $"Issue for {customer.FullName}",
                    Description = "Klient zglosil problem z ostatnim zamowieniem.",
                    Status = TicketStatus.Open,
                    Priority = (TicketPriority)random.Next(0, 3),
                    CreatedAt = now.AddDays(-random.Next(0, 15))
                };

                ticket.Comments.Add(new TicketComment
                {
                    Author = "Wsparcie",
                    Content = "Sprawdzamy zgloszenie.",
                    CreatedAt = ticket.CreatedAt.AddHours(2)
                });

                tickets.Add(ticket);
            }

            await context.Tickets.AddRangeAsync(tickets);

            var notes = new List<CustomerNote>();
            foreach (var customer in customers)
            {
                notes.Add(new CustomerNote
                {
                    CustomerId = customer.Id,
                    Author = "System",
                    Content = "Notatka poczatkowa o kliencie.",
                    CreatedAt = now.AddDays(-random.Next(1, 10))
                });
            }

            await context.CustomerNotes.AddRangeAsync(notes);
            await context.SaveChangesAsync();
        }
    }
}
