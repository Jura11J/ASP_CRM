using System.ComponentModel.DataAnnotations;

namespace AspCrm.ViewModels
{
    public class LoginViewModel
    {
        [Required(ErrorMessage = "Email jest wymagany")]
        [EmailAddress(ErrorMessage = "Niepoprawny adres email")]
        public string Email { get; set; } = string.Empty;

        [Required(ErrorMessage = "Hasło jest wymagane")]
        [DataType(DataType.Password)]
        public string Password { get; set; } = string.Empty;

        [Display(Name = "Zapamiętaj mnie")]
        public bool RememberMe { get; set; }

        public string? ReturnUrl { get; set; }
    }
}
