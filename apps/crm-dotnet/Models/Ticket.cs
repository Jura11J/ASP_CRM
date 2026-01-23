using System.ComponentModel.DataAnnotations;

namespace AspCrm.Models
{
    public class Ticket
    {
        public int Id { get; set; }

        [Display(Name = "Klient")]
        [Required]
        public int CustomerId { get; set; }
        public Customer? Customer { get; set; }

        [Display(Name = "Tytuł")]
        [Required, StringLength(150)]
        public string Title { get; set; } = string.Empty;

        [Display(Name = "Opis")]
        [Required, StringLength(1000)]
        public string Description { get; set; } = string.Empty;

        [Display(Name = "Status")]
        public TicketStatus Status { get; set; } = TicketStatus.Open;
        [Display(Name = "Priorytet")]
        public TicketPriority Priority { get; set; } = TicketPriority.Medium;

        [Display(Name = "Data utworzenia")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        public ICollection<TicketComment> Comments { get; set; } = new List<TicketComment>();
    }
}
