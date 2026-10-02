package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.ProductsPage;

import java.util.List;

public class ProductsTest extends BaseTest {

    @Test(description = "Products page is displayed after successful login", groups = {"smoke", "regression"})
    public void productsPageIsDisplayedTest() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(config.getProperty("username"), config.getProperty("password"));

        ProductsPage productsPage = new ProductsPage(driver);
        Assert.assertEquals(productsPage.getPageTitle(), "Products");
        Assert.assertTrue(productsPage.getProductCount() > 0, "At least one product should be displayed");
    }

    @Test(description = "Every product shows a name and a price", groups = {"regression"})
    public void productsHaveNamesAndPricesTest() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(config.getProperty("username"), config.getProperty("password"));

        ProductsPage productsPage = new ProductsPage(driver);
        int productCount = productsPage.getProductCount();
        List<String> names = productsPage.getProductNames();
        List<String> prices = productsPage.getProductPrices();

        Assert.assertEquals(names.size(), productCount, "Each product should have a name");
        Assert.assertEquals(prices.size(), productCount, "Each product should have a price");

        for (String name : names) {
            Assert.assertFalse(name.trim().isEmpty(), "Product name should not be empty");
        }
        for (String price : prices) {
            Assert.assertTrue(price.startsWith("$"), "Price should start with $, but was: " + price);
        }
    }

    // If the products page does not even load, this test is skipped instead of failing
    @Test(description = "User can add a product to the cart", groups = {"smoke", "regression"},
            dependsOnMethods = "productsPageIsDisplayedTest")
    public void addProductToCartTest() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(config.getProperty("username"), config.getProperty("password"));

        ProductsPage productsPage = new ProductsPage(driver);
        productsPage.addProductToCart("Sauce Labs Backpack");

        Assert.assertEquals(productsPage.getCartBadgeCount(), "1");
    }
}
