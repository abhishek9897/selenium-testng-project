package base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

// alwaysRun = true on every setup/teardown method makes them run even when
// tests are filtered by groups (for example in testng-smoke.xml)
public class BaseTest {

    // Each test class gets its own BaseTest object, so each class has its own driver.
    // This is why parallel="classes" in testng.xml is safe.
    protected WebDriver driver;

    // Shared by all test classes and loaded only once per suite
    protected static Properties config;

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() throws IOException {
        config = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("config.properties not found in src/test/resources");
            }
            config.load(input);
        }
        log("Before Suite : config.properties loaded");
    }

    @BeforeTest(alwaysRun = true)
    public void beforeTest(ITestContext context) {
        log("Before Test  : <test name=\"" + context.getName() + "\"> starting");
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        log("Before Class : " + getClass().getSimpleName());
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method) {
        log("Before Method: " + method.getName() + " - opening Chrome");

        ChromeOptions options = new ChromeOptions();

        // Turn off Chrome's password manager so its "save password" / "password breach"
        // pop-ups do not appear on top of the page after login.
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);

        // A -Dheadless=true value on the command line overrides config.properties
        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless", config.getProperty("headless")));
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1920,1080");

        // Selenium Manager (built into Selenium 4.6+) finds or downloads the matching ChromeDriver
        driver = new ChromeDriver(options);
        driver.get(config.getProperty("baseUrl"));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(Method method) {
        if (driver != null) {
            driver.quit();
        }
        log("After Method : " + method.getName() + " - Chrome closed");
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        log("After Class  : " + getClass().getSimpleName());
    }

    @AfterTest(alwaysRun = true)
    public void afterTest(ITestContext context) {
        log("After Test   : <test name=\"" + context.getName() + "\"> finished");
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        log("After Suite  : all tests finished");
    }

    // Prints the thread name too, so you can see tests running in parallel
    private void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }
}
