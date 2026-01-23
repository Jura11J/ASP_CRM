using System.ComponentModel.DataAnnotations;
using AspCrm.Models;
using Microsoft.AspNetCore.Mvc.Rendering;

namespace AspCrm.ViewModels
{
    public class OrderItemInput
    {
        [Required]
        public int ProductId { get; set; }

        [Range(1, 10000)]
        public int Quantity { get; set; } = 1;
    }

    public class OrderFormViewModel
    {
        public int? Id { get; set; }

        [Required(ErrorMessage = "Wybierz klienta")]
        [Display(Name = "Klient")]
        public int CustomerId { get; set; }

        [Display(Name = "Status")]
        public OrderStatus Status { get; set; } = OrderStatus.New;

        public List<OrderItemInput> Items { get; set; } = new();

        public IEnumerable<SelectListItem> CustomerOptions { get; set; } = Enumerable.Empty<SelectListItem>();
        public IEnumerable<Product> Products { get; set; } = Enumerable.Empty<Product>();
    }
}
