using AspCrm.Models;

namespace AspCrm.ViewModels
{
    public class ChatMessageVm
    {
        public int Id { get; set; }
        public int ConversationId { get; set; }
        public ChatSenderType SenderType { get; set; }
        public string SenderLabel { get; set; } = string.Empty;
        public string Content { get; set; } = string.Empty;
        public DateTime SentAt { get; set; }
        public bool IsOwn { get; set; }
    }
}
