package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ProductsPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // Locators
    private By pageTitle = By.className("title");
    private By productItems = By.className("inventory_item");
    private By productNames = By.className("inventory_item_name");
    private By productPrices = By.className("inventory_item_price");
    private By cartBadge = By.className("shopping_cart_badge");
    private By cartLink = By.className("shopping_cart_link");
    private By menuButton = By.id("react-burger-menu-btn");
    private By logoutLink = By.id("logout_sidebar_link");

    public ProductsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public String getPageTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle)).getText();
    }

    public int getProductCount() {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(productItems)).size();
    }

    public List<String> getProductNames() {
        List<String> names = new ArrayList<>();
        for (WebElement element : driver.findElements(productNames)) {
            names.add(element.getText());
        }
        return names;
    }

    public List<String> getProductPrices() {
        List<String> prices = new ArrayList<>();
        for (WebElement element : driver.findElements(productPrices)) {
            prices.add(element.getText());
        }
        return prices;
    }

    // Finds the "Add to cart" button inside the product card that has this name
    public void addProductToCart(String productName) {
        By addButton = By.xpath("//div[text()='" + productName + "']"
                + "/ancestor::div[@class='inventory_item']//button");
        wait.until(ExpectedConditions.elementToBeClickable(addButton)).click();
    }

    public String getCartBadgeCount() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(cartBadge)).getText();
    }

    public void openCart() {
        wait.until(ExpectedConditions.elementToBeClickable(cartLink)).click();
    }

    public void logout() {
        wait.until(ExpectedConditions.elementToBeClickable(menuButton)).click();
        // The side menu slides in, so wait until the link is clickable
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
    }
}
