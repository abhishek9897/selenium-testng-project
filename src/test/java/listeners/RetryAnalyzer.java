package listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

// Runs a failed test again, up to MAX_RETRY extra times
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final int MAX_RETRY = 1;
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            System.out.println("RETRYING: " + result.getName() + " (retry " + retryCount + " of " + MAX_RETRY + ")");
            return true;
        }
        return false;
    }
}
