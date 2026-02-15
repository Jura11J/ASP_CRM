using System.ComponentModel.DataAnnotations;

namespace AspCrm.Models
{
    public class Customer
    {
        public int Id { get; set; }

        [Display(Name = "Imię")]
        [Required, StringLength(80)]
        public string FirstName { get; set; } = string.Empty;

        [Display(Name = "Nazwisko")]
        [Required, StringLength(80)]
        public string LastName { get; set; } = string.Empty;

        [Display(Name = "E-mail")]
        [Required, EmailAddress, StringLength(120)]
        public string Email { get; set; } = string.Empty;

        [Display(Name = "Telefon")]
        [Required, Phone, StringLength(40)]
        public string Phone { get; set; } = string.Empty;

        [Display(Name = "Adres")]
        [StringLength(160)]
        public string? AddressLine1 { get; set; }

        [Display(Name = "Miasto")]
        [StringLength(80)]
        public string? City { get; set; }

        [Display(Name = "Preferowany kontakt"), StringLength(50)]
        public string? PreferredContactMethod { get; set; }

        [Display(Name = "Zgoda marketingowa")]
        public bool MarketingConsent { get; set; }

        [Display(Name = "Aktywny")]
        public bool IsActive { get; set; } = true;

        [Display(Name = "Usunięty")]
        public bool IsDeleted { get; set; }

        [Display(Name = "Data utworzenia")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        public ICollection<Order> Orders { get; set; } = new List<Order>();
        public ICollection<CustomerNote> Notes { get; set; } = new List<CustomerNote>();
        public ICollection<Ticket> Tickets { get; set; } = new List<Ticket>();
        public ICollection<ChatConversation> ChatConversations { get; set; } = new List<ChatConversation>();

        public string FullName => $"{FirstName} {LastName}";
    }
}
