package tests;

import base.BaseTest;
import data.LoginDataProvider;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.ProductsPage;

public class LoginTest extends BaseTest {

    @Test(description = "User can log in with valid credentials", groups = {"smoke", "regression"})
    public void validLoginTest() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(config.getProperty("username"), config.getProperty("password"));

        ProductsPage productsPage = new ProductsPage(driver);
        Assert.assertEquals(productsPage.getPageTitle(), "Products");
        Assert.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "URL should contain inventory.html after login, but was: " + driver.getCurrentUrl());
    }

    // Runs once for every row in LoginDataProvider.invalidLoginData()
    @Test(description = "Login fails with invalid credentials",
            dataProvider = "invalidLoginData", dataProviderClass = LoginDataProvider.class,
            groups = {"regression"})
    public void invalidLoginTest(String username, String password, String expectedError) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, password);

        Assert.assertEquals(loginPage.getErrorMessage(), expectedError);
    }

    @Test(description = "User can log out and returns to the login page", groups = {"regression"})
    public void logoutTest() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(config.getProperty("username"), config.getProperty("password"));

        ProductsPage productsPage = new ProductsPage(driver);
        productsPage.logout();

        Assert.assertTrue(loginPage.isLoginButtonDisplayed(), "Login button should be visible after logout");
        Assert.assertFalse(driver.getCurrentUrl().contains("inventory.html"),
                "User should not be on the products page after logout");
    }
}
