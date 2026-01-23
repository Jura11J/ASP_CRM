using System.ComponentModel.DataAnnotations;

namespace AspCrm.Models
{
    public class CustomerNote
    {
        public int Id { get; set; }

        [Required]
        public int CustomerId { get; set; }
        public Customer? Customer { get; set; }

        [Required, StringLength(800)]
        public string Content { get; set; } = string.Empty;

        [Required, StringLength(80)]
        public string Author { get; set; } = "System";

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }
}
