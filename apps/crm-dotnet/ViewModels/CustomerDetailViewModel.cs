using AspCrm.Models;

namespace AspCrm.ViewModels
{
    public class CustomerDetailViewModel
    {
        public Customer Customer { get; set; } = null!;
        public IEnumerable<CustomerNote> Notes { get; set; } = Enumerable.Empty<CustomerNote>();
        public IEnumerable<Order> Orders { get; set; } = Enumerable.Empty<Order>();
        public IEnumerable<Ticket> Tickets { get; set; } = Enumerable.Empty<Ticket>();

        public CustomerNote NewNote { get; set; } = new CustomerNote();
    }
}
