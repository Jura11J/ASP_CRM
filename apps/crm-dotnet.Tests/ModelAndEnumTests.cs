using AspCrm.Models;

namespace AspCrm.Tests;

public class ModelAndEnumTests
{
    [Fact]
    public void EnumExtensions_ReturnsDisplayName_WhenAttributeExists()
    {
        var name = OrderStatus.New.GetDisplayName();

        Assert.Equal("Nowe", name);
    }

    [Fact]
    public void EnumExtensions_ReturnsEnumName_WhenNoDisplayAttribute()
    {
        var name = ChatSenderType.Customer.GetDisplayName();

        Assert.Equal(nameof(ChatSenderType.Customer), name);
    }

    [Fact]
    public void ErrorViewModel_ShowRequestId_WorksAsExpected()
    {
        var model = new ErrorViewModel { RequestId = "abc" };

        Assert.True(model.ShowRequestId);
        model.RequestId = null;
        Assert.False(model.ShowRequestId);
    }

    [Fact]
    public void Customer_FullName_CombinesNames()
    {
        var customer = new Customer { FirstName = "Jan", LastName = "Nowak" };

        Assert.Equal("Jan Nowak", customer.FullName);
    }
}
