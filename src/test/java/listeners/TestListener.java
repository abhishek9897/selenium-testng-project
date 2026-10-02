package listeners;

import base.BaseTest;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;

// Registered in testng.xml. TestNG calls these methods automatically.
public class TestListener implements ISuiteListener, ITestListener {

    private static ExtentReports extent;

    // Tests run in parallel, so each thread keeps its own current ExtentTest
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    // ---------- Suite level (ISuiteListener) ----------

    @Override
    public void onStart(ISuite suite) {
        System.out.println("===== SUITE STARTED : " + suite.getName() + " =====");

        ExtentSparkReporter spark = new ExtentSparkReporter("reports/ExtentReport.html");
        spark.config().setDocumentTitle("SauceDemo Test Report");
        spark.config().setReportName(suite.getName());

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Application", "https://www.saucedemo.com/");
        extent.setSystemInfo("Browser", "Chrome");
        extent.setSystemInfo("Java", System.getProperty("java.version"));
    }

    @Override
    public void onFinish(ISuite suite) {
        // Writes everything into the HTML file - without flush() the report stays empty
        extent.flush();
        System.out.println("Extent Report saved: reports/ExtentReport.html");
        System.out.println("===== SUITE FINISHED: " + suite.getName() + " =====");
    }

    // ---------- <test> tag level (ITestListener) ----------

    @Override
    public void onStart(ITestContext context) {
        System.out.println("TEST STARTED : " + context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("TEST FINISHED: " + context.getName()
                + " | Passed: " + context.getPassedTests().size()
                + " | Failed: " + context.getFailedTests().size()
                + " | Skipped: " + context.getSkippedTests().size());
    }

    // ---------- Each @Test method (ITestListener) ----------

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("STARTED : " + result.getName());

        // For DataProvider tests, add the data to the name, e.g. invalidLoginTest [standard_user, wrong_password, ...]
        String testName = result.getName();
        if (result.getParameters().length > 0) {
            testName = testName + " " + Arrays.toString(result.getParameters());
        }

        ExtentTest test = extent.createTest(testName, result.getMethod().getDescription());
        test.assignCategory(result.getMethod().getGroups());  // smoke / regression
        extentTest.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("PASSED  : " + result.getName());
        extentTest.get().pass("Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("FAILED  : " + result.getName() + " - " + result.getThrowable().getMessage());
        extentTest.get().fail(result.getThrowable());

        String screenshot = takeScreenshot(result);
        if (screenshot != null) {
            extentTest.get().fail("Screenshot at failure",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(screenshot).build());
        }
    }

    // A test that is going to be retried is also reported as skipped
    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("SKIPPED : " + result.getName());

        if (result.wasRetried()) {
            extentTest.get().skip("Failed, will be retried: " + result.getThrowable().getMessage());
        } else {
            extentTest.get().skip("Test skipped");
        }
    }

    // Saves a PNG in screenshots/ and returns the image as Base64 text for the Extent Report
    private String takeScreenshot(ITestResult result) {
        // The test class object that just failed, e.g. LoginTest (all test classes extend BaseTest)
        BaseTest testClass = (BaseTest) result.getInstance();
        WebDriver driver = testClass.getDriver();

        if (driver == null) {
            System.out.println("No browser open - screenshot not taken");
            return null;
        }

        String base64 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        File destination = new File("screenshots/" + result.getName() + "_" + System.currentTimeMillis() + ".png");

        try {
            destination.getParentFile().mkdirs();
            Files.write(destination.toPath(), Base64.getDecoder().decode(base64));
            System.out.println("Screenshot saved: " + destination.getPath());
        } catch (IOException e) {
            System.out.println("Could not save screenshot: " + e.getMessage());
        }
        return base64;
    }
}
