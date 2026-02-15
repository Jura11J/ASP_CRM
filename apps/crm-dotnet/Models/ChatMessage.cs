using System.ComponentModel.DataAnnotations;

namespace AspCrm.Models
{
    public class ChatMessage
    {
        public int Id { get; set; }

        [Required]
        public int ConversationId { get; set; }
        public ChatConversation? Conversation { get; set; }

        [Required]
        public ChatSenderType SenderType { get; set; }

        [StringLength(450)]
        public string? SenderCrmUserId { get; set; }

        [Required, StringLength(2000)]
        public string Content { get; set; } = string.Empty;

        public DateTime SentAt { get; set; } = DateTime.UtcNow;

        public bool IsReadByCustomer { get; set; }
        public bool IsReadByCrm { get; set; }
    }
}
