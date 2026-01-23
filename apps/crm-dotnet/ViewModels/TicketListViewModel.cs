using AspCrm.Models;
using Microsoft.AspNetCore.Mvc.Rendering;

namespace AspCrm.ViewModels
{
    public class TicketListViewModel
    {
        public TicketStatus? Status { get; set; }
        public TicketPriority? Priority { get; set; }
        public int? CustomerId { get; set; }

        public IEnumerable<SelectListItem> Customers { get; set; } = Enumerable.Empty<SelectListItem>();
        public IEnumerable<Ticket> Tickets { get; set; } = Enumerable.Empty<Ticket>();
    }
}
