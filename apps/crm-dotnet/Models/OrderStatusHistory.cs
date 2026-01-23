using System.ComponentModel.DataAnnotations;

namespace AspCrm.Models
{
    public class OrderStatusHistory
    {
        public int Id { get; set; }

        [Required]
        public int OrderId { get; set; }
        public Order? Order { get; set; }

        [Required]
        public OrderStatus Status { get; set; }

        [Required]
        public DateTime ChangedAt { get; set; } = DateTime.UtcNow;

        [StringLength(200)]
        public string? Note { get; set; }
    }
}
