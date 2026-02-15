using AspCrm.Models;

namespace AspCrm.ViewModels
{
    public class ChatConversationPageVm
    {
        public IList<ChatConversationListItemVm> Conversations { get; set; } = new List<ChatConversationListItemVm>();
        public int? SelectedConversationId { get; set; }
        public ChatConversation? SelectedConversation { get; set; }
        public IList<ChatMessageVm> Messages { get; set; } = new List<ChatMessageVm>();
        public string CurrentUserId { get; set; } = string.Empty;
    }
}
