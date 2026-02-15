using AspCrm.Data;
using AspCrm.Models;
using AspCrm.ViewModels;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace AspCrm.Controllers
{
    [Authorize]
    public class LiveChatController : Controller
    {
        private readonly AppDbContext _context;
        private readonly UserManager<IdentityUser> _userManager;

        public LiveChatController(AppDbContext context, UserManager<IdentityUser> userManager)
        {
            _context = context;
            _userManager = userManager;
        }

        public async Task<IActionResult> Index(int? conversationId)
        {
            var conversations = await _context.ChatConversations
                .Include(c => c.Customer)
                .OrderByDescending(c => c.LastMessageAt)
                .ToListAsync();

            var vm = new ChatConversationPageVm
            {
                Conversations = await BuildConversationListAsync(conversations),
                CurrentUserId = GetCurrentUserId() ?? string.Empty
            };

            if (conversations.Count == 0)
            {
                return View(vm);
            }

            var selectedId = conversations.Any(c => c.Id == conversationId)
                ? conversationId
                : conversations.First().Id;

            if (!selectedId.HasValue)
            {
                return View(vm);
            }

            var selectedConversation = await _context.ChatConversations
                .Include(c => c.Customer)
                .Include(c => c.Messages)
                .FirstOrDefaultAsync(c => c.Id == selectedId.Value);

            if (selectedConversation == null)
            {
                return View(vm);
            }

            await MarkConversationAsReadAsync(selectedConversation);

            vm.SelectedConversationId = selectedConversation.Id;
            vm.SelectedConversation = selectedConversation;
            vm.Messages = await MapMessagesAsync(
                selectedConversation.Messages.OrderBy(m => m.Id).ToList(),
                selectedConversation.Customer!,
                vm.CurrentUserId);

            vm.Conversations = await BuildConversationListAsync(conversations, selectedConversation.Id);
            return View(vm);
        }

        [HttpGet]
        public async Task<IActionResult> ConversationsSummary()
        {
            var conversations = await _context.ChatConversations
                .Include(c => c.Customer)
                .OrderByDescending(c => c.LastMessageAt)
                .ToListAsync();

            var list = await BuildConversationListAsync(conversations);
            return Json(new { conversations = list });
        }

        [HttpGet]
        public async Task<IActionResult> ConversationMessages(int id, int? afterId)
        {
            var conversation = await _context.ChatConversations
                .Include(c => c.Customer)
                .FirstOrDefaultAsync(c => c.Id == id);

            if (conversation?.Customer == null)
            {
                return NotFound();
            }

            var currentUserId = GetCurrentUserId() ?? string.Empty;

            var query = _context.ChatMessages
                .Where(m => m.ConversationId == id);

            if (afterId.HasValue)
            {
                query = query.Where(m => m.Id > afterId.Value);
            }

            var messages = await query
                .OrderBy(m => m.Id)
                .ToListAsync();

            var unreadCustomerMessages = await _context.ChatMessages
                .Where(m => m.ConversationId == id && m.SenderType == ChatSenderType.Customer && !m.IsReadByCrm)
                .ToListAsync();

            if (unreadCustomerMessages.Count > 0)
            {
                foreach (var unread in unreadCustomerMessages)
                {
                    unread.IsReadByCrm = true;
                }

                await _context.SaveChangesAsync();
            }

            var mapped = await MapMessagesAsync(messages, conversation.Customer, currentUserId);
            return Json(new
            {
                conversationId = id,
                isClosed = conversation.IsClosed,
                messages = mapped
            });
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> SendMessage(int id, [FromForm] string content)
        {
            var conversation = await _context.ChatConversations
                .Include(c => c.Customer)
                .FirstOrDefaultAsync(c => c.Id == id);

            if (conversation?.Customer == null)
            {
                return NotFound();
            }

            if (conversation.IsClosed)
            {
                return BadRequest(new { error = "Konwersacja jest zamknięta." });
            }

            var trimmed = (content ?? string.Empty).Trim();
            if (string.IsNullOrWhiteSpace(trimmed))
            {
                return BadRequest(new { error = "Wiadomość nie może być pusta." });
            }

            var currentUserId = GetCurrentUserId();
            if (string.IsNullOrWhiteSpace(currentUserId))
            {
                return Unauthorized();
            }

            var message = new ChatMessage
            {
                ConversationId = id,
                SenderType = ChatSenderType.CrmUser,
                SenderCrmUserId = currentUserId,
                Content = trimmed,
                SentAt = DateTime.UtcNow,
                IsReadByCrm = true,
                IsReadByCustomer = false
            };

            _context.ChatMessages.Add(message);

            conversation.LastMessageAt = message.SentAt;
            conversation.LastMessagePreview = BuildPreview(trimmed);

            await _context.SaveChangesAsync();

            var mapped = (await MapMessagesAsync(new List<ChatMessage> { message }, conversation.Customer, currentUserId)).First();
            return Json(new { message = mapped });
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> ToggleClosed(int id)
        {
            var conversation = await _context.ChatConversations.FirstOrDefaultAsync(c => c.Id == id);
            if (conversation == null)
            {
                return NotFound();
            }

            conversation.IsClosed = !conversation.IsClosed;
            conversation.ClosedAt = conversation.IsClosed ? DateTime.UtcNow : null;

            await _context.SaveChangesAsync();
            return Json(new { isClosed = conversation.IsClosed, closedAt = conversation.ClosedAt });
        }

        private async Task MarkConversationAsReadAsync(ChatConversation conversation)
        {
            var unreadCustomerMessages = conversation.Messages
                .Where(m => m.SenderType == ChatSenderType.Customer && !m.IsReadByCrm)
                .ToList();

            if (unreadCustomerMessages.Count == 0)
            {
                return;
            }

            foreach (var unread in unreadCustomerMessages)
            {
                unread.IsReadByCrm = true;
            }

            await _context.SaveChangesAsync();
        }

        private async Task<IList<ChatConversationListItemVm>> BuildConversationListAsync(List<ChatConversation> conversations, int? selectedId = null)
        {
            if (conversations.Count == 0)
            {
                return new List<ChatConversationListItemVm>();
            }

            var conversationIds = conversations.Select(c => c.Id).ToList();

            var unreadCounts = await _context.ChatMessages
                .Where(m => conversationIds.Contains(m.ConversationId) && m.SenderType == ChatSenderType.Customer && !m.IsReadByCrm)
                .GroupBy(m => m.ConversationId)
                .Select(g => new { ConversationId = g.Key, Count = g.Count() })
                .ToDictionaryAsync(x => x.ConversationId, x => x.Count);

            var lastMessageMeta = await GetLastMessageMetaAsync(conversationIds);

            return conversations
                .OrderByDescending(c => selectedId.HasValue && c.Id == selectedId.Value)
                .ThenByDescending(c => c.LastMessageAt)
                .Select(c =>
                {
                    var hasMeta = lastMessageMeta.TryGetValue(c.Id, out var meta);
                    var preview = !string.IsNullOrWhiteSpace(c.LastMessagePreview)
                        ? c.LastMessagePreview
                        : hasMeta ? meta.Preview : null;

                    return new ChatConversationListItemVm
                    {
                        ConversationId = c.Id,
                        CustomerId = c.CustomerId,
                        CustomerName = c.Customer?.FullName ?? "Klient",
                        CustomerEmail = c.Customer?.Email ?? string.Empty,
                        LastMessageAt = hasMeta ? meta.SentAt : c.LastMessageAt,
                        LastMessagePreview = preview,
                        UnreadCount = unreadCounts.TryGetValue(c.Id, out var count) ? count : 0,
                        IsClosed = c.IsClosed
                    };
                })
                .ToList();
        }

        private async Task<Dictionary<int, (string Preview, DateTime SentAt)>> GetLastMessageMetaAsync(List<int> conversationIds)
        {
            var lastIds = await _context.ChatMessages
                .Where(m => conversationIds.Contains(m.ConversationId))
                .GroupBy(m => m.ConversationId)
                .Select(g => new { ConversationId = g.Key, LastId = g.Max(m => m.Id) })
                .ToListAsync();

            if (lastIds.Count == 0)
            {
                return new Dictionary<int, (string Preview, DateTime SentAt)>();
            }

            var idLookup = lastIds.ToDictionary(x => x.ConversationId, x => x.LastId);
            var lastMessageIds = idLookup.Values.ToList();

            var lastMessages = await _context.ChatMessages
                .Where(m => lastMessageIds.Contains(m.Id))
                .ToListAsync();

            return lastMessages.ToDictionary(
                m => m.ConversationId,
                m => (BuildPreview(m.Content), m.SentAt));
        }

        private async Task<IList<ChatMessageVm>> MapMessagesAsync(List<ChatMessage> messages, Customer customer, string currentUserId)
        {
            var crmIds = messages
                .Where(m => m.SenderType == ChatSenderType.CrmUser && !string.IsNullOrWhiteSpace(m.SenderCrmUserId))
                .Select(m => m.SenderCrmUserId!)
                .Distinct()
                .ToList();

            var crmUsers = crmIds.Count == 0
                ? new Dictionary<string, IdentityUser>()
                : await _userManager.Users
                    .Where(u => crmIds.Contains(u.Id))
                    .ToDictionaryAsync(u => u.Id, u => u);

            return messages.Select(m =>
            {
                var senderLabel = m.SenderType == ChatSenderType.Customer
                    ? customer.FullName
                    : crmUsers.TryGetValue(m.SenderCrmUserId ?? string.Empty, out var user)
                        ? (user.UserName ?? user.Email ?? "CRM")
                        : "CRM";

                return new ChatMessageVm
                {
                    Id = m.Id,
                    ConversationId = m.ConversationId,
                    SenderType = m.SenderType,
                    SenderLabel = senderLabel,
                    Content = m.Content,
                    SentAt = m.SentAt,
                    IsOwn = m.SenderType == ChatSenderType.CrmUser && string.Equals(m.SenderCrmUserId, currentUserId, StringComparison.Ordinal)
                };
            }).ToList();
        }

        private string? GetCurrentUserId()
        {
            return _userManager.GetUserId(User) ?? User.FindFirstValue(ClaimTypes.NameIdentifier);
        }

        private static string BuildPreview(string content)
        {
            const int maxLength = 160;
            if (content.Length <= maxLength)
            {
                return content;
            }

            return content[..maxLength] + "…";
        }
    }
}
