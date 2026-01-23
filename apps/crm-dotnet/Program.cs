using AspCrm.Data;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using System.Globalization;

var builder = WebApplication.CreateBuilder(args);

var defaultCulture = new CultureInfo("pl-PL");
CultureInfo.DefaultThreadCurrentCulture = defaultCulture;
CultureInfo.DefaultThreadCurrentUICulture = defaultCulture;

var connectionString = builder.Configuration.GetConnectionString("DefaultConnection")
    ?? "Server=(localdb)\\mssqllocaldb;Database=AspCrmDb;Trusted_Connection=True;MultipleActiveResultSets=true;";
var sqliteConnection = builder.Configuration.GetConnectionString("SqliteConnection")
    ?? "Data Source=aspcrm.db";
var postgresConnection = builder.Configuration.GetConnectionString("PostgresConnection")
    ?? "Host=localhost;Port=5432;Database=aspcrm;Username=postgres;Password=postgres";
var provider = builder.Configuration.GetValue<string>("DatabaseProvider") ?? "SqlServer";

builder.Services.AddDbContext<AppDbContext>(options =>
{
    if (provider.Equals("Sqlite", StringComparison.OrdinalIgnoreCase))
    {
        options.UseSqlite(sqliteConnection);
    }
    else if (provider.Equals("Postgres", StringComparison.OrdinalIgnoreCase))
    {
        options.UseNpgsql(postgresConnection);
    }
    else
    {
        options.UseSqlServer(connectionString);
    }
});

builder.Services.AddDefaultIdentity<IdentityUser>(options =>
{
    options.SignIn.RequireConfirmedAccount = false;
    options.Password.RequiredLength = 6;
    options.Password.RequireNonAlphanumeric = false;
    options.Password.RequireUppercase = false;
})
.AddEntityFrameworkStores<AppDbContext>();

builder.Services.ConfigureApplicationCookie(options =>
{
    options.LoginPath = "/Account/Login";
    options.LogoutPath = "/Account/Logout";
    options.AccessDeniedPath = "/Account/Login";
});

builder.Services.AddControllersWithViews();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    // The default HSTS value is 30 days. You may want to change this for production scenarios, see https://aka.ms/aspnetcore-hsts.
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();

app.UseRouting();

app.UseAuthentication();
app.UseAuthorization();

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}");

await AspCrm.Data.DataSeeder.SeedAsync(app.Services);
app.Run();
