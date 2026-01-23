using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspCrm.Models
{
    public class Product
    {
        public int Id { get; set; }

        [Display(Name = "Nazwa")]
        [Required, StringLength(120)]
        public string Name { get; set; } = string.Empty;

        [Display(Name = "SKU")]
        [Required, StringLength(50)]
        public string Sku { get; set; } = string.Empty;

        [Display(Name = "Cena")]
        [Range(0, 100000), Column(TypeName = "decimal(18,2)")]
        public decimal Price { get; set; }

        [Display(Name = "Stan magazynowy")]
        [Range(0, int.MaxValue)]
        public int StockQuantity { get; set; }

        [Display(Name = "Aktywny")]
        public bool IsActive { get; set; } = true;

        [Display(Name = "Usunięty")]
        public bool IsDeleted { get; set; }

        [Display(Name = "Opis")]
        [StringLength(500)]
        public string? Description { get; set; }
    }
}
