package listeners;

import base.BaseTest;
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

// Registered in testng.xml. TestNG calls these methods automatically.
public class TestListener implements ISuiteListener, ITestListener {

    // ---------- Suite level (ISuiteListener) ----------

    @Override
    public void onStart(ISuite suite) {
        System.out.println("===== SUITE STARTED : " + suite.getName() + " =====");
    }

    @Override
    public void onFinish(ISuite suite) {
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
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("PASSED  : " + result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("FAILED  : " + result.getName() + " - " + result.getThrowable().getMessage());
        takeScreenshot(result);
    }

    // A test that is going to be retried is also reported as skipped
    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("SKIPPED : " + result.getName());
    }

    private void takeScreenshot(ITestResult result) {
        // The test class object that just failed, e.g. LoginTest (all test classes extend BaseTest)
        BaseTest testClass = (BaseTest) result.getInstance();
        WebDriver driver = testClass.getDriver();

        if (driver == null) {
            System.out.println("No browser open - screenshot not taken");
            return;
        }

        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File destination = new File("screenshots/" + result.getName() + "_" + System.currentTimeMillis() + ".png");

        try {
            destination.getParentFile().mkdirs();
            Files.copy(screenshot.toPath(), destination.toPath());
            System.out.println("Screenshot saved: " + destination.getPath());
        } catch (IOException e) {
            System.out.println("Could not save screenshot: " + e.getMessage());
        }
    }
}
