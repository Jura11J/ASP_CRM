namespace AspCrm.Models
{
    public enum OrderStatus
    {
        [System.ComponentModel.DataAnnotations.Display(Name = "Nowe")]
        New = 0,
        [System.ComponentModel.DataAnnotations.Display(Name = "Opłacone")]
        Paid = 1,
        [System.ComponentModel.DataAnnotations.Display(Name = "Wysłane")]
        Shipped = 2,
        [System.ComponentModel.DataAnnotations.Display(Name = "Zrealizowane")]
        Completed = 3,
        [System.ComponentModel.DataAnnotations.Display(Name = "Anulowane")]
        Cancelled = 4
    }

    public enum TicketStatus
    {
        [System.ComponentModel.DataAnnotations.Display(Name = "Otwarte")]
        Open = 0,
        [System.ComponentModel.DataAnnotations.Display(Name = "W trakcie")]
        InProgress = 1,
        [System.ComponentModel.DataAnnotations.Display(Name = "Zamknięte")]
        Closed = 2
    }

    public enum TicketPriority
    {
        [System.ComponentModel.DataAnnotations.Display(Name = "Niski")]
        Low = 0,
        [System.ComponentModel.DataAnnotations.Display(Name = "Średni")]
        Medium = 1,
        [System.ComponentModel.DataAnnotations.Display(Name = "Wysoki")]
        High = 2
    }
}
