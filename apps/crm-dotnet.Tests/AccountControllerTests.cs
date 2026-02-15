using AspCrm.Controllers;
using AspCrm.ViewModels;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Moq;

namespace AspCrm.Tests;

public class AccountControllerTests
{
    [Fact]
    public void Login_Get_ReturnsViewWithModel()
    {
        var userManager = TestSupport.CreateUserManagerMock();
        var signInManager = TestSupport.CreateSignInManagerMock(userManager.Object);
        var controller = new AccountController(signInManager.Object, userManager.Object);

        var result = controller.Login("/orders");

        var view = Assert.IsType<ViewResult>(result);
        var model = Assert.IsType<LoginViewModel>(view.Model);
        Assert.Equal("/orders", model.ReturnUrl);
    }

    [Fact]
    public async Task Login_Post_InvalidModel_ReturnsView()
    {
        var userManager = TestSupport.CreateUserManagerMock();
        var signInManager = TestSupport.CreateSignInManagerMock(userManager.Object);
        var controller = new AccountController(signInManager.Object, userManager.Object);
        controller.ModelState.AddModelError("Email", "required");

        var result = await controller.Login(new LoginViewModel());

        Assert.IsType<ViewResult>(result);
    }

    [Fact]
    public async Task Login_Post_UserMissing_ReturnsViewWithError()
    {
        var userManager = TestSupport.CreateUserManagerMock();
        userManager.Setup(m => m.FindByEmailAsync("missing@demo.pl")).ReturnsAsync((IdentityUser?)null);
        var signInManager = TestSupport.CreateSignInManagerMock(userManager.Object);
        var controller = new AccountController(signInManager.Object, userManager.Object);

        var result = await controller.Login(new LoginViewModel { Email = "missing@demo.pl", Password = "pass" });

        Assert.IsType<ViewResult>(result);
        Assert.False(controller.ModelState.IsValid);
    }

    [Fact]
    public async Task Login_Post_SuccessWithLocalReturnUrl_RedirectsToUrl()
    {
        var user = new IdentityUser { Id = "u1", Email = "user@demo.pl" };
        var userManager = TestSupport.CreateUserManagerMock(new[] { user });
        userManager.Setup(m => m.FindByEmailAsync(user.Email!)).ReturnsAsync(user);

        var signInManager = TestSupport.CreateSignInManagerMock(userManager.Object);
        signInManager
            .Setup(m => m.PasswordSignInAsync(user, "pass", true, false))
            .ReturnsAsync(Microsoft.AspNetCore.Identity.SignInResult.Success);

        var controller = new AccountController(signInManager.Object, userManager.Object);
        var url = new Mock<IUrlHelper>();
        url.Setup(u => u.IsLocalUrl("/orders")).Returns(true);
        controller.Url = url.Object;

        var result = await controller.Login(new LoginViewModel
        {
            Email = user.Email!,
            Password = "pass",
            RememberMe = true,
            ReturnUrl = "/orders"
        });

        var redirect = Assert.IsType<RedirectResult>(result);
        Assert.Equal("/orders", redirect.Url);
    }

    [Fact]
    public async Task Login_Post_SuccessWithoutReturnUrl_RedirectsHome()
    {
        var user = new IdentityUser { Id = "u1", Email = "user@demo.pl" };
        var userManager = TestSupport.CreateUserManagerMock(new[] { user });
        userManager.Setup(m => m.FindByEmailAsync(user.Email!)).ReturnsAsync(user);

        var signInManager = TestSupport.CreateSignInManagerMock(userManager.Object);
        signInManager
            .Setup(m => m.PasswordSignInAsync(user, "pass", false, false))
            .ReturnsAsync(Microsoft.AspNetCore.Identity.SignInResult.Success);

        var controller = new AccountController(signInManager.Object, userManager.Object)
        {
            Url = Mock.Of<IUrlHelper>()
        };

        var result = await controller.Login(new LoginViewModel
        {
            Email = user.Email!,
            Password = "pass",
            RememberMe = false
        });

        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal("Index", redirect.ActionName);
        Assert.Equal("Home", redirect.ControllerName);
    }

    [Fact]
    public async Task Login_Post_FailedSignIn_ReturnsViewWithError()
    {
        var user = new IdentityUser { Id = "u1", Email = "user@demo.pl" };
        var userManager = TestSupport.CreateUserManagerMock(new[] { user });
        userManager.Setup(m => m.FindByEmailAsync(user.Email!)).ReturnsAsync(user);

        var signInManager = TestSupport.CreateSignInManagerMock(userManager.Object);
        signInManager
            .Setup(m => m.PasswordSignInAsync(user, "bad", false, false))
            .ReturnsAsync(Microsoft.AspNetCore.Identity.SignInResult.Failed);

        var controller = new AccountController(signInManager.Object, userManager.Object)
        {
            Url = Mock.Of<IUrlHelper>()
        };

        var result = await controller.Login(new LoginViewModel
        {
            Email = user.Email!,
            Password = "bad"
        });

        Assert.IsType<ViewResult>(result);
        Assert.False(controller.ModelState.IsValid);
    }

    [Fact]
    public async Task Logout_Post_SignsOutAndRedirectsToLogin()
    {
        var userManager = TestSupport.CreateUserManagerMock();
        var signInManager = TestSupport.CreateSignInManagerMock(userManager.Object);
        signInManager.Setup(m => m.SignOutAsync()).Returns(Task.CompletedTask);

        var controller = new AccountController(signInManager.Object, userManager.Object);

        var result = await controller.Logout();

        signInManager.Verify(m => m.SignOutAsync(), Times.Once);
        var redirect = Assert.IsType<RedirectToActionResult>(result);
        Assert.Equal("Login", redirect.ActionName);
    }
}
