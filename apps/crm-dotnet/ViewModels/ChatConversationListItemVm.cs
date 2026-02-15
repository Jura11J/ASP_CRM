namespace AspCrm.ViewModels
{
    public class ChatConversationListItemVm
    {
        public int ConversationId { get; set; }
        public int CustomerId { get; set; }
        public string CustomerName { get; set; } = string.Empty;
        public string CustomerEmail { get; set; } = string.Empty;
        public DateTime LastMessageAt { get; set; }
        public string? LastMessagePreview { get; set; }
        public int UnreadCount { get; set; }
        public bool IsClosed { get; set; }
    }
}
