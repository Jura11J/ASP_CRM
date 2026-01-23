using System.ComponentModel.DataAnnotations;
using System.Reflection;

namespace AspCrm.Models
{
    public static class EnumExtensions
    {
        // Zwraca podpis z atrybutu Display lub nazwę enuma.
        public static string GetDisplayName(this Enum value)
        {
            var member = value.GetType().GetMember(value.ToString()).FirstOrDefault();
            var display = member?.GetCustomAttribute<DisplayAttribute>();
            return display?.Name ?? value.ToString();
        }
    }
}
