using System.ComponentModel.DataAnnotations;

namespace AspCrm.Models
{
    public class ChatConversation
    {
        public int Id { get; set; }

        [Required]
        public int CustomerId { get; set; }
        public Customer? Customer { get; set; }

        [Display(Name = "Utworzono")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [Display(Name = "Ostatnia wiadomość")]
        public DateTime LastMessageAt { get; set; } = DateTime.UtcNow;

        [StringLength(200)]
        public string? LastMessagePreview { get; set; }

        [Display(Name = "Zamknięta")]
        public bool IsClosed { get; set; }

        [Display(Name = "Zamknięto")]
        public DateTime? ClosedAt { get; set; }

        public ICollection<ChatMessage> Messages { get; set; } = new List<ChatMessage>();
    }
}
