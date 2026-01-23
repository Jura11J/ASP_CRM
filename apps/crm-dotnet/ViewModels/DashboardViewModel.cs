using AspCrm.Models;

namespace AspCrm.ViewModels
{
    public class DashboardViewModel
    {
        public int CustomersCount { get; set; }
        public int ActiveOrders { get; set; }
        public int OrdersThisMonth { get; set; }
        public decimal SalesValue { get; set; }

        public List<Order> RecentOrders { get; set; } = new();
        public List<SalesChartPoint> SalesChart { get; set; } = new();
    }

    public class SalesChartPoint
    {
        public DateTime Date { get; set; }
        public decimal Total { get; set; }
    }
}
