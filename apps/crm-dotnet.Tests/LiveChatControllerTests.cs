using AspCrm.Controllers;
using AspCrm.Models;
using AspCrm.ViewModels;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace AspCrm.Tests;

public class LiveChatControllerTests
{
    [Fact]
    public async Task Index_ReturnsEmptyViewModel_WhenNoConversations()
    {
        using var db = TestSupport.CreateDbContext();
        var userManager = TestSupport.CreateUserManagerMock();
        var controller = new LiveChatController(db, userManager.Object);
        TestSupport.AttachUser(controller, "crm1");

        var result = await controller.Index(null);

        var view = Assert.IsType<ViewResult>(result);
        var vm = Assert.IsType<ChatConversationPageVm>(view.Model);
        Assert.Empty(vm.Conversations);
    }

    [Fact]
    public async Task Index_SelectsConversation_AndMarksUnreadCustomerMessagesAsRead()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "Jan", LastName = "K", Email = "j@k.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        var conversation = new ChatConversation
        {
            CustomerId = customer.Id,
            LastMessageAt = DateTime.UtcNow,
            LastMessagePreview = "hello"
        };
        db.ChatConversations.Add(conversation);
        await db.SaveChangesAsync();

        db.ChatMessages.Add(new ChatMessage
        {
            ConversationId = conversation.Id,
            SenderType = ChatSenderType.Customer,
            Content = "message",
            IsReadByCrm = false,
            IsReadByCustomer = true,
            SentAt = DateTime.UtcNow
        });
        await db.SaveChangesAsync();

        var userManager = TestSupport.CreateUserManagerMock();
        var controller = new LiveChatController(db, userManager.Object);
        TestSupport.AttachUser(controller, "crm1");

        var result = await controller.Index(conversation.Id);

        var view = Assert.IsType<ViewResult>(result);
        var vm = Assert.IsType<ChatConversationPageVm>(view.Model);
        Assert.Equal(conversation.Id, vm.SelectedConversationId);
        Assert.Single(vm.Messages);

        var updatedMessage = await db.ChatMessages.FirstAsync();
        Assert.True(updatedMessage.IsReadByCrm);
    }

    [Fact]
    public async Task ConversationsSummary_ReturnsJsonList()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        db.ChatConversations.Add(new ChatConversation { CustomerId = customer.Id, LastMessageAt = DateTime.UtcNow, LastMessagePreview = "x" });
        await db.SaveChangesAsync();

        var userManager = TestSupport.CreateUserManagerMock();
        var controller = new LiveChatController(db, userManager.Object);

        var result = await controller.ConversationsSummary();

        var json = Assert.IsType<JsonResult>(result);
        var conversations = ReadAnonymousProperty<IList<ChatConversationListItemVm>>(json.Value!, "conversations");
        Assert.Single(conversations);
    }

    [Fact]
    public async Task ConversationMessages_ReturnsNotFound_WhenConversationMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var userManager = TestSupport.CreateUserManagerMock();
        var controller = new LiveChatController(db, userManager.Object);

        var result = await controller.ConversationMessages(123, null);

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task ConversationMessages_ReturnsMessages_AndMarksUnread()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();

        var conversation = new ChatConversation { CustomerId = customer.Id, LastMessageAt = DateTime.UtcNow };
        db.ChatConversations.Add(conversation);
        await db.SaveChangesAsync();

        db.ChatMessages.AddRange(
            new ChatMessage
            {
                ConversationId = conversation.Id,
                SenderType = ChatSenderType.Customer,
                Content = "first",
                IsReadByCrm = false,
                IsReadByCustomer = true,
                SentAt = DateTime.UtcNow.AddMinutes(-1)
            },
            new ChatMessage
            {
                ConversationId = conversation.Id,
                SenderType = ChatSenderType.Customer,
                Content = "second",
                IsReadByCrm = false,
                IsReadByCustomer = true,
                SentAt = DateTime.UtcNow
            });
        await db.SaveChangesAsync();

        var userManager = TestSupport.CreateUserManagerMock();
        var controller = new LiveChatController(db, userManager.Object);
        TestSupport.AttachUser(controller, "crm1");

        var result = await controller.ConversationMessages(conversation.Id, 0);

        var json = Assert.IsType<JsonResult>(result);
        Assert.Equal(conversation.Id, ReadAnonymousProperty<int>(json.Value!, "conversationId"));

        var messages = ReadAnonymousProperty<IList<ChatMessageVm>>(json.Value!, "messages");
        Assert.Equal(2, messages.Count);
        Assert.All(await db.ChatMessages.ToListAsync(), m => Assert.True(m.IsReadByCrm));
    }

    [Fact]
    public async Task SendMessage_ReturnsNotFound_WhenConversationMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var userManager = TestSupport.CreateUserManagerMock();
        var controller = new LiveChatController(db, userManager.Object);
        TestSupport.AttachUser(controller, "crm1");

        var result = await controller.SendMessage(1, "hello");

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task SendMessage_ReturnsBadRequest_WhenConversationClosed()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();
        var conversation = new ChatConversation { CustomerId = customer.Id, IsClosed = true, LastMessageAt = DateTime.UtcNow };
        db.ChatConversations.Add(conversation);
        await db.SaveChangesAsync();

        var userManager = TestSupport.CreateUserManagerMock();
        var controller = new LiveChatController(db, userManager.Object);
        TestSupport.AttachUser(controller, "crm1");

        var result = await controller.SendMessage(conversation.Id, "hello");

        Assert.IsType<BadRequestObjectResult>(result);
    }

    [Fact]
    public async Task SendMessage_ReturnsBadRequest_WhenEmptyContent()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();
        var conversation = new ChatConversation { CustomerId = customer.Id, LastMessageAt = DateTime.UtcNow };
        db.ChatConversations.Add(conversation);
        await db.SaveChangesAsync();

        var userManager = TestSupport.CreateUserManagerMock();
        var controller = new LiveChatController(db, userManager.Object);
        TestSupport.AttachUser(controller, "crm1");

        var result = await controller.SendMessage(conversation.Id, "   ");

        Assert.IsType<BadRequestObjectResult>(result);
    }

    [Fact]
    public async Task SendMessage_ReturnsUnauthorized_WhenUserIdUnavailable()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();
        var conversation = new ChatConversation { CustomerId = customer.Id, LastMessageAt = DateTime.UtcNow };
        db.ChatConversations.Add(conversation);
        await db.SaveChangesAsync();

        var userManager = TestSupport.CreateUserManagerMock();
        var controller = new LiveChatController(db, userManager.Object);
        TestSupport.AttachUser(controller, null);

        var result = await controller.SendMessage(conversation.Id, "hello");

        Assert.IsType<UnauthorizedResult>(result);
    }

    [Fact]
    public async Task SendMessage_PersistsMessage_AndReturnsJson()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();
        var conversation = new ChatConversation { CustomerId = customer.Id, LastMessageAt = DateTime.UtcNow };
        db.ChatConversations.Add(conversation);
        await db.SaveChangesAsync();

        var crmUser = new IdentityUser { Id = "crm1", UserName = "agent" };
        var userManager = TestSupport.CreateUserManagerMock(new[] { crmUser });

        var controller = new LiveChatController(db, userManager.Object);
        TestSupport.AttachUser(controller, crmUser.Id);

        var result = await controller.SendMessage(conversation.Id, "  hello world  ");

        var json = Assert.IsType<JsonResult>(result);
        Assert.NotNull(ReadAnonymousProperty<object>(json.Value!, "message"));

        var persisted = await db.ChatMessages.FirstAsync();
        Assert.Equal("hello world", persisted.Content);
        Assert.Equal(ChatSenderType.CrmUser, persisted.SenderType);
    }

    [Fact]
    public async Task ToggleClosed_ReturnsNotFound_WhenConversationMissing()
    {
        using var db = TestSupport.CreateDbContext();
        var userManager = TestSupport.CreateUserManagerMock();
        var controller = new LiveChatController(db, userManager.Object);

        var result = await controller.ToggleClosed(5);

        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task ToggleClosed_TogglesState_AndReturnsJson()
    {
        using var db = TestSupport.CreateDbContext();
        var customer = new Customer { FirstName = "A", LastName = "B", Email = "a@b.pl", Phone = "1" };
        db.Customers.Add(customer);
        await db.SaveChangesAsync();
        var conversation = new ChatConversation { CustomerId = customer.Id, LastMessageAt = DateTime.UtcNow, IsClosed = false };
        db.ChatConversations.Add(conversation);
        await db.SaveChangesAsync();

        var userManager = TestSupport.CreateUserManagerMock();
        var controller = new LiveChatController(db, userManager.Object);

        var result = await controller.ToggleClosed(conversation.Id);

        var json = Assert.IsType<JsonResult>(result);
        Assert.True(ReadAnonymousProperty<bool>(json.Value!, "isClosed"));
        var updated = await db.ChatConversations.FirstAsync();
        Assert.True(updated.IsClosed);
        Assert.NotNull(updated.ClosedAt);
    }

    private static T ReadAnonymousProperty<T>(object source, string propertyName)
    {
        var property = source.GetType().GetProperty(propertyName);
        Assert.NotNull(property);
        return (T)property!.GetValue(source)!;
    }
}
