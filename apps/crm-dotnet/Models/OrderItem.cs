using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspCrm.Models
{
    public class OrderItem
    {
        public int Id { get; set; }

        [Required]
        public int OrderId { get; set; }
        public Order? Order { get; set; }

        [Required]
        public int ProductId { get; set; }
        public Product? Product { get; set; }

        [Range(1, 10000)]
        public int Quantity { get; set; }

        [Column(TypeName = "decimal(18,2)"), Range(0, 100000)]
        public decimal UnitPrice { get; set; }

        [Column(TypeName = "decimal(18,2)"), Range(0, 1000000)]
        public decimal LineTotal { get; set; }
    }
}
