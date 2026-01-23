using AspCrm.Models;
using Microsoft.AspNetCore.Mvc.Rendering;

namespace AspCrm.ViewModels
{
    public class OrderListViewModel
    {
        public int? CustomerId { get; set; }
        public OrderStatus? Status { get; set; }
        public DateTime? FromDate { get; set; }
        public DateTime? ToDate { get; set; }

        public IEnumerable<SelectListItem> Customers { get; set; } = Enumerable.Empty<SelectListItem>();
        public IEnumerable<Order> Orders { get; set; } = Enumerable.Empty<Order>();
    }
}
