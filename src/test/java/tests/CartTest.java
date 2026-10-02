package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.LoginPage;
import pages.ProductsPage;

import java.util.List;

public class CartTest extends BaseTest {

    @Test(description = "Cart shows the product that was added", groups = {"smoke", "regression"})
    public void cartShowsCorrectProductTest() {
        String productName = "Sauce Labs Backpack";

        new LoginPage(driver).login(config.getProperty("username"), config.getProperty("password"));

        ProductsPage productsPage = new ProductsPage(driver);
        productsPage.addProductToCart(productName);
        productsPage.openCart();

        CartPage cartPage = new CartPage(driver);
        Assert.assertEquals(cartPage.getPageTitle(), "Your Cart");

        List<String> itemsInCart = cartPage.getCartItemNames();
        Assert.assertEquals(itemsInCart.size(), 1, "Cart should contain exactly one item");
        Assert.assertEquals(itemsInCart.get(0), productName);
    }
}
