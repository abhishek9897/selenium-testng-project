# Selenium TestNG Project – SauceDemo

A small UI test automation project for the demo e-commerce site
[saucedemo.com](https://www.saucedemo.com/), built with **Java, Selenium WebDriver, TestNG and Maven**
using the **Page Object Model (POM)**.

## Table of contents

1. [About the project](#1-about-the-project)
2. [Tech stack and prerequisites](#2-tech-stack-and-prerequisites)
3. [Project structure](#3-project-structure)
4. [Test scenarios](#4-test-scenarios)
5. [How to run](#5-how-to-run)
6. [Test reports](#6-test-reports)
7. [How it works – the big picture](#7-how-it-works--the-big-picture)
8. [Code explained](#8-code-explained)
9. [Execution order and parallel run](#9-execution-order-and-parallel-run)
10. [One test, step by step](#10-one-test-step-by-step)
11. [Glossary](#11-glossary)
12. [Possible improvements](#12-possible-improvements)

---

## 1. About the project

Instead of a person clicking through the shop by hand, this project uses **Java code that controls Chrome**.
It logs in, adds a product to the cart, opens the cart, logs out… and after each action it
**checks** that the website behaved correctly.

> Think of it like a robot tester:
> **Selenium** = the robot's hands (clicks, types, reads the screen)
> **TestNG** = the robot's checklist (which tests to run, pass/fail, reports)
> **Maven** = the robot's toolbox (downloads libraries, compiles and runs everything)

### What the project achieves

| Goal | How |
|------|-----|
| Check the main user flows automatically | 10 test runs: login, invalid logins, logout, products, add to cart, cart |
| Catch problems fast | One command (`mvn clean test`) runs everything in ~10 seconds |
| Easy to maintain | Page Object Model – if a button changes, fix it in **one** place |
| Reliable, not flaky | Explicit waits instead of `Thread.sleep()` |
| Run only critical tests when needed | `smoke` and `regression` groups |
| Test many inputs without copy-paste | `@DataProvider` – one test, 4 data rows |
| Finish faster | 3 test classes run in parallel |
| Clear results | TestNG built-in HTML and XML reports |

---

## 2. Tech stack and prerequisites

| Tool | Version |
|------|---------|
| Java (JDK) | 11 or newer |
| Maven | 3.6.3 or newer |
| Google Chrome | Latest |
| Selenium WebDriver | 4.50.0 (downloaded by Maven) |
| TestNG | 7.12.0 (downloaded by Maven) |

- **No ChromeDriver setup needed.** Selenium Manager (built into Selenium 4.6+) finds or downloads the matching driver automatically.
- **Internet access** is needed to reach saucedemo.com and to download Maven dependencies.
- **Test data:** the site publicly lists its demo credentials on its login page:
  `standard_user` / `secret_sauce` (and `locked_out_user` for the locked-out case).

---

## 3. Project structure

```
selenium-testng-project/
├── pom.xml                              Maven: libraries + how to run tests
├── testng.xml                           TestNG suite – all test classes, run in parallel
├── README.md
└── src/test/
    ├── java/
    │   ├── base/
    │   │   └── BaseTest.java            Opens Chrome before each test, closes it after
    │   ├── data/
    │   │   └── LoginDataProvider.java   Test data for invalid logins
    │   ├── listeners/
    │   │   ├── TestListener.java        Logs test events, screenshot on failure
    │   │   ├── RetryAnalyzer.java       Re-runs a failed test once
    │   │   └── RetryTransformer.java    Adds RetryAnalyzer to every test
    │   ├── pages/                       Page objects: locators + page actions
    │   │   ├── LoginPage.java
    │   │   ├── ProductsPage.java
    │   │   └── CartPage.java
    │   └── tests/                       Test classes: test steps + assertions
    │       ├── LoginTest.java
    │       ├── ProductsTest.java
    │       └── CartTest.java
    └── resources/
        └── config.properties            URL, username, password, headless flag

screenshots/                             Created when a test fails (not committed to Git)
```

A simple rule for the 5 packages:

| Package | Answers the question |
|---------|----------------------|
| `base`  | "How do I **start and stop** the browser?" |
| `pages` | "**Where** is the element and **how** do I use it?" |
| `tests` | "**What** am I testing and **what** should happen?" |
| `data`  | "**Which inputs** should I try?" |
| `listeners` | "What should happen **automatically** when a test starts, passes or fails?" |

---

## 4. Test scenarios

| # | Test method | Class | What it checks | Groups |
|---|-------------|-------|----------------|--------|
| 1 | `validLoginTest` | LoginTest | Valid login opens the "Products" page | smoke, regression |
| 2–5 | `invalidLoginTest` (×4, data-driven) | LoginTest | Wrong password, empty username, empty password, locked-out user each show the correct error | regression |
| 6 | `logoutTest` | LoginTest | Logout returns to the login page | regression |
| 7 | `productsPageIsDisplayedTest` | ProductsTest | "Products" title and products are shown | smoke, regression |
| 8 | `productsHaveNamesAndPricesTest` | ProductsTest | Every product has a name and a `$` price | regression |
| 9 | `addProductToCartTest` | ProductsTest | Adding a product makes the cart badge show "1" (depends on test 7) | smoke, regression |
| 10 | `cartShowsCorrectProductTest` | CartTest | The cart contains exactly the added product | smoke, regression |

**TestNG features used:** `@DataProvider` (in a separate class), `groups`, `dependsOnMethods`,
all Before/After annotations (Suite, Test, Class, Method), parallel execution,
listeners (`ITestListener`, `ISuiteListener`) with a screenshot on failure, and automatic retry
(`IRetryAnalyzer` + `IAnnotationTransformer`).

---

## 5. How to run

### From the terminal

```bash
mvn clean test                          # run everything
mvn clean test -Dgroups=smoke           # only the 4 smoke tests
mvn clean test -Dgroups=regression      # all tests in the regression group
mvn test -Dtest=LoginTest               # one class
mvn test -Dtest=LoginTest#logoutTest    # one test method
```

> **Note:** `-Dtest=...` makes Maven ignore `testng.xml`, so the **listeners are not active** in that run
> (no screenshots, no retry). Use `mvn clean test` or `-Dgroups=...` when you need them.

### From IntelliJ IDEA / Eclipse / VS Code

Right-click `testng.xml` → **Run**, or click the ▶ icon next to any `@Test` method.

### Watching a test slowly

Tests are fast on purpose. To see each step, set a breakpoint in the test, choose **Debug Test**,
and press **F10** line by line while watching Chrome. Don't add `Thread.sleep()` for this.

---

## 6. Test reports

TestNG creates its built-in reports automatically – no extra library is used.

| How you ran the tests | Report folder |
|-----------------------|---------------|
| `mvn test` | `target/surefire-reports/` |
| IDE / TestNG directly | `test-output/` |

Open these files in a browser:

| File | Use it for |
|------|-----------|
| `emailable-report.html` | **One-page summary** – passed / failed / skipped, times, errors |
| `index.html` | **Detailed report** – click each test, see DataProvider parameters and stack traces |
| `testng-results.xml` | Same results in XML, for CI tools like Jenkins |

The other files in the folder (`.css`, `.js`, `.png`) are only support files for `index.html`.

| Status | Meaning |
|--------|---------|
| **Passed** | All assertions were true |
| **Failed** | An assertion was false or an exception happened – read the message and stack trace |
| **Skipped** | Did not run – e.g. the method it depends on failed, **or** the attempt failed and is being retried |

**Screenshots:** when a test fails, a screenshot of the browser at that moment is saved in
`screenshots/<testName>_<timestamp>.png` in the project folder.

**Stack trace tip:** read from the top and find the first line that mentions **your** package
(`tests.` or `pages.`) – that's the line in your code where it broke.

---

## 7. How it works – the big picture

```
                    mvn clean test
                          │
                          ▼
   ┌──────────────────────────────────────────┐
   │ MAVEN (pom.xml)                          │
   │  • downloads Selenium + TestNG           │
   │  • compiles the Java code                │
   │  • Surefire plugin starts TestNG         │
   └──────────────────────────────────────────┘
                          │
                          ▼
   ┌──────────────────────────────────────────┐
   │ TESTNG (testng.xml)                      │
   │  • runs LoginTest, ProductsTest,         │
   │    CartTest – 3 classes in parallel      │
   └──────────────────────────────────────────┘
                          │
                          ▼
   ┌──────────────────────────────────────────┐
   │ BaseTest  @BeforeSuite (once)            │
   │  • read config.properties                │
   │ @BeforeTest / @BeforeClass (log only)    │
   │ @BeforeMethod (before every test)        │
   │  • open Chrome, go to saucedemo.com      │
   └──────────────────────────────────────────┘
                          │
                          ▼
   ┌──────────────────────────────────────────┐
   │ TEST CLASS  (e.g. CartTest)              │
   │  • says WHAT to do and WHAT to expect    │
   │        │ calls                           │
   │        ▼                                 │
   │  PAGE CLASSES (LoginPage, ProductsPage…) │
   │  • know WHERE elements are (locators)    │
   │  • know HOW to click/type (actions)      │
   │        │ uses                            │
   │        ▼                                 │
   │  SELENIUM WebDriver ──► Chrome browser   │
   │                                          │
   │  • Assert: actual == expected ?          │
   └──────────────────────────────────────────┘
                          │
                          ▼
   ┌──────────────────────────────────────────┐
   │ BaseTest  @AfterMethod                   │
   │  • driver.quit() – close Chrome          │
   │ @AfterClass / @AfterTest / @AfterSuite   │
   └──────────────────────────────────────────┘
                          │
                          ▼
   Reports in target/surefire-reports/
```

Every test gets a **brand new Chrome** and closes it at the end, so tests never affect each other.

---

## 8. Code explained

### 8.1 `config.properties` – settings

```properties
baseUrl=https://www.saucedemo.com/
username=standard_user
password=secret_sauce
headless=false
```

If the URL or password changes, edit **one line here** instead of every test.
`headless=false` shows the Chrome window; change it to `true` to run Chrome invisibly (no window).

### 8.2 `BaseTest.java` – open and close the browser

Every test class **extends** `BaseTest`, so every test automatically gets:

- `driver` – the Chrome browser to control
- `config` – the values from `config.properties`

It uses all 8 TestNG setup/teardown annotations:

| Annotation | When it runs | What it does here |
|------------|--------------|-------------------|
| `@BeforeSuite` | **Once**, before everything | Loads `config.properties` into a `static` field shared by all classes |
| `@BeforeTest` | Once per `<test>` tag in the XML | Prints which `<test>` is starting |
| `@BeforeClass` | Once per test class | Prints the class name |
| `@BeforeMethod` | **Before each** `@Test` | Opens Chrome (guest mode, maximized, headless if set) → opens saucedemo.com |
| `@AfterMethod` | **After each** `@Test` (even if it failed) | `driver.quit()` – closes Chrome |
| `@AfterClass` | Once per test class | Prints the class name |
| `@AfterTest` | Once per `<test>` tag | Prints which `<test>` finished |
| `@AfterSuite` | **Once**, after everything | Prints "all tests finished" |

Only `@BeforeSuite`, `@BeforeMethod` and `@AfterMethod` do real work. The others print a log line
(with the thread name) so you can **see the execution order** in the console – see [section 9](#9-execution-order-and-parallel-run).

Details worth knowing:

- **`--guest`** – Chrome starts in guest mode (no saved passwords), so its "change your password"
  pop-up does not appear after logging in with the demo password.
- **`alwaysRun = true`** on all 8 methods – setup methods have no group, so without this, running
  `-Dgroups=smoke` would skip setup, Chrome would never open, and every test would crash.
- **`ITestContext`** and **`Method`** parameters – TestNG fills these in automatically; they are used only to
  print the `<test>` name and the test method name.

### 8.3 Page classes – Page Object Model (POM)

One Java class per screen. Each class contains:

1. **Locators** – how to find elements (`By.id("login-button")`)
2. **Actions** – methods that do something on the page (`login()`, `addProductToCart()`)

Tests **never** contain locators – they only call page methods.

**`LoginPage.java`**

| Locator | Element |
|---------|---------|
| `By.id("user-name")` | Username box |
| `By.id("password")` | Password box |
| `By.id("login-button")` | Login button |
| `By.cssSelector("[data-test='error']")` | Error message |

| Method | What it does |
|--------|--------------|
| `enterUsername()`, `enterPassword()`, `clickLogin()` | One small step each |
| `login(username, password)` | All three steps together |
| `getErrorMessage()` | Waits for the error to appear and returns its text |
| `isLoginButtonDisplayed()` | Proves we are back on the login page after logout |

**`ProductsPage.java`**

| Method | What it does |
|--------|--------------|
| `getPageTitle()` | Returns "Products" |
| `getProductCount()` | Number of product cards shown |
| `getProductNames()` / `getProductPrices()` | Lists of all names / prices |
| `addProductToCart("Sauce Labs Backpack")` | Clicks "Add to cart" for **that** product |
| `getCartBadgeCount()` | The number on the cart icon |
| `openCart()` | Clicks the cart icon |
| `logout()` | Opens the ☰ menu and clicks Logout |

`addProductToCart` uses a **dynamic XPath**:

```
//div[text()='Sauce Labs Backpack']/ancestor::div[@class='inventory_item']//button
```

In plain words: *"find the product name, go up to its product card, then find the button inside that card."*
The same method works for **any** product name.

**`CartPage.java`**

| Method | What it does |
|--------|--------------|
| `getPageTitle()` | Returns "Your Cart" |
| `getCartItemNames()` | List of product names in the cart |

### 8.4 Explicit waits – why tests don't randomly fail

Websites take time to load. If Selenium looks for an element **before** it appears, it throws
`NoSuchElementException`.

- **Bad fix:** `Thread.sleep(3000)` – always waits 3 seconds, even if the page loaded in 0.2 s, and still fails if it needs 4 s.
- **Good fix:** an explicit wait:

```java
wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.elementToBeClickable(menuButton)).click();
```

*"Check repeatedly, up to 10 seconds, and continue **the moment** the button is clickable."*

| Condition used | Meaning |
|----------------|---------|
| `visibilityOfElementLocated` | Element is present **and** visible |
| `visibilityOfAllElementsLocatedBy` | All matching elements are visible |
| `elementToBeClickable` | Visible **and** enabled – safe to click |

> **Real example from this project:** `logoutTest` first used `driver.findElement(menuButton).click()`
> right after login. It failed in about half of the runs with `NoSuchElementException` because the
> products page had not finished loading. Switching to `elementToBeClickable` fixed it.

### 8.5 Test classes – the checks

Each test follows **Arrange → Act → Assert**:

```java
LoginPage loginPage = new LoginPage(driver);                    // Arrange
loginPage.login("standard_user", "wrong_password");             // Act
Assert.assertEquals(loginPage.getErrorMessage(), "Epic sadface: ...");  // Assert
```

If an assertion fails, the test stops there and is marked **FAILED** in the report.

### 8.6 `LoginDataProvider.java` – data-driven testing

One test method, **4 rows of data**:

| username | password | expected error |
|----------|----------|----------------|
| standard_user | wrong_password | Username and password do not match any user in this service |
| *(empty)* | secret_sauce | Username is required |
| standard_user | *(empty)* | Password is required |
| locked_out_user | secret_sauce | Sorry, this user has been locked out. |

```java
@Test(dataProvider = "invalidLoginData", dataProviderClass = LoginDataProvider.class)
public void invalidLoginTest(String username, String password, String expectedError)
```

- `dataProvider` – the **name** of the data
- `dataProviderClass` – the **class** where the data lives
- The data method must be **`static`** because it is in a different class
- Each row's values are passed into the test's parameters, in order

### 8.7 Groups and `dependsOnMethods`

**Groups** are labels on tests:

- `smoke` – the 4 most important tests (login → products → add to cart → cart). A quick health check.
- `regression` – every test. The full check.

There is only **one** suite file (`testng.xml`). A group is chosen on the command line with
`-Dgroups=smoke`; Maven's Surefire plugin still runs `testng.xml` but only the tests with that label.
Group names are case-sensitive (`Smoke` would run nothing).

> Alternative: a TestNG XML file can filter with `<groups><run><include name="smoke"/></run></groups>`,
> but that needs a separate XML file per group, so `-Dgroups` is used instead.

**`dependsOnMethods`** in `ProductsTest`:

```java
@Test(dependsOnMethods = "productsPageIsDisplayedTest")
public void addProductToCartTest()
```

*"Only run me if `productsPageIsDisplayedTest` passed."* If the products page is broken, adding to cart
makes no sense, so TestNG marks the test **SKIPPED** instead of reporting a second, confusing failure.

### 8.8 `testng.xml` – the suite

```xml
<suite name="SauceDemo Test Suite" parallel="classes" thread-count="3">

    <listeners>
        <listener class-name="listeners.TestListener"/>
        <listener class-name="listeners.RetryTransformer"/>
    </listeners>

    <test name="SauceDemo UI Tests">
        <classes>
            <class name="tests.LoginTest"/>
            <class name="tests.ProductsTest"/>
            <class name="tests.CartTest"/>
        </classes>
    </test>
</suite>
```

Structure: **suite** → **test** → **classes** → `@Test` **methods**. The Before/After annotations match these levels:

| XML level | Annotations |
|-----------|-------------|
| `<suite>` | `@BeforeSuite` / `@AfterSuite` |
| `<test>` | `@BeforeTest` / `@AfterTest` |
| `<class>` | `@BeforeClass` / `@AfterClass` |
| each `@Test` method | `@BeforeMethod` / `@AfterMethod` |

### 8.9 `pom.xml` – Maven

| Part | Meaning |
|------|---------|
| `selenium-java` dependency | Selenium WebDriver library |
| `testng` dependency (scope `test`) | TestNG, used only for tests |
| `maven-compiler-plugin` | Compiles the Java code (as Java 11) |
| `maven-surefire-plugin` | Runs TestNG with `testng.xml` when you type `mvn test` |

### 8.10 Listeners – screenshot on failure and retry

A **listener** is a class that TestNG calls **automatically** when something happens
(suite starts, test passes, test fails…). Tests don't call it – it is registered once in `testng.xml`.

**`TestListener.java`** implements two TestNG interfaces:

| Method | Interface | When TestNG calls it | What it does here |
|--------|-----------|----------------------|-------------------|
| `onStart(ISuite)` | `ISuiteListener` | Suite starts | Prints "SUITE STARTED" |
| `onFinish(ISuite)` | `ISuiteListener` | Suite ends | Prints "SUITE FINISHED" |
| `onStart(ITestContext)` | `ITestListener` | A `<test>` tag starts | Prints "TEST STARTED" |
| `onFinish(ITestContext)` | `ITestListener` | A `<test>` tag ends | Prints passed / failed / skipped counts |
| `onTestStart` | `ITestListener` | Before each `@Test` | Prints "STARTED" |
| `onTestSuccess` | `ITestListener` | A test passed | Prints "PASSED" |
| `onTestFailure` | `ITestListener` | A test failed | Prints "FAILED" + error, **takes a screenshot** |
| `onTestSkipped` | `ITestListener` | A test was skipped or is being retried | Prints "SKIPPED" |

**How the screenshot works:**

```java
BaseTest testClass = (BaseTest) result.getInstance();   // the test object that failed
WebDriver driver = testClass.getDriver();               // its browser
File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
Files.copy(screenshot.toPath(), destination.toPath());  // save into screenshots/
```

The listener lives in another package, so it can't read the `protected driver` field directly –
that's why `BaseTest` has a small public `getDriver()` method. `onTestFailure` runs **before**
`@AfterMethod` closes the browser, so the screenshot shows the page at the moment of failure.

**Retry – two small classes:**

| Class | Interface | Job |
|-------|-----------|-----|
| `RetryAnalyzer` | `IRetryAnalyzer` | `retry()` returns `true` → TestNG runs the failed test again. Max **1** retry. |
| `RetryTransformer` | `IAnnotationTransformer` | Adds `RetryAnalyzer` to **every** `@Test` while TestNG reads the annotations |

Without the transformer, every test would need `@Test(retryAnalyzer = RetryAnalyzer.class)`.
An `IAnnotationTransformer` must be registered in `testng.xml` (it can't be added with `@Listeners`),
because it has to run *before* TestNG reads the `@Test` annotations.

**What a failure looks like** (real run, with a test broken on purpose):

```
STARTED : cartShowsCorrectProductTest
RETRYING: cartShowsCorrectProductTest (retry 1 of 1)
SKIPPED : cartShowsCorrectProductTest        ← 1st attempt, retried
STARTED : cartShowsCorrectProductTest
FAILED  : cartShowsCorrectProductTest - expected [WRONG TITLE] but found [Your Cart]
Screenshot saved: screenshots/cartShowsCorrectProductTest_1790967895195.png
TEST FINISHED: SauceDemo UI Tests | Passed: 9 | Failed: 1 | Skipped: 1
```

The first attempt is reported as **SKIPPED**, the final attempt as **FAILED**, and only the final
failure gets a screenshot. If the retry had passed, the test would count as **PASSED**.

> Retry is a safety net for **flaky** tests (slow network, timing). It doesn't fix a real bug –
> a real bug fails both times, like above.

---

## 9. Execution order and parallel run

Console output from a real run (shortened):

```
[main]          Before Suite : config.properties loaded
[main]          Before Test  : <test name="SauceDemo UI Tests"> starting
[...Tests-1]    Before Class : LoginTest        ┐
[...Tests-3]    Before Class : CartTest         ├ 3 classes start at the same time
[...Tests-2]    Before Class : ProductsTest     ┘
[...Tests-3]    Before Method: cartShowsCorrectProductTest - opening Chrome
[...Tests-2]    Before Method: productsPageIsDisplayedTest - opening Chrome
[...Tests-1]    Before Method: invalidLoginTest - opening Chrome
   ... each thread runs its own class's tests one by one ...
[...Tests-3]    After Class  : CartTest
[...Tests-2]    After Class  : ProductsTest
[...Tests-1]    After Class  : LoginTest
[main]          After Test   : <test name="SauceDemo UI Tests"> finished
[main]          After Suite  : all tests finished
```

Order to remember: **Suite → Test → Class → Method → @Test → Method → Class → Test → Suite**
(like opening and closing nested boxes).

**Parallel settings** (on the `<suite>` tag):

| Setting | Meaning |
|---------|---------|
| `parallel="classes"` | Each test class runs in its own thread, at the same time |
| `thread-count="3"` | At most 3 threads → at most 3 Chrome windows at once |

The full run went from about **16 s** to about **10 s**.

**Why `classes` and not `methods`?** TestNG creates **one object per test class**, and `driver` is a
normal field in that object.

- `parallel="classes"` → one class = one thread = one object = its own `driver`. **Safe.**
- `parallel="methods"` → two methods of the same class would share the **same** `driver` field,
  and one test would close the other's browser. **Not safe** without `ThreadLocal<WebDriver>`.

Other `parallel` values: `tests` (each `<test>` tag in parallel) and `instances`.

**Why is `config` `static` but `driver` is not?** `config` is read-only and the same for everyone,
so one shared copy is fine. `driver` must be different for every running test.

---

## 10. One test, step by step

`cartShowsCorrectProductTest`:

```
0. (Done once already: @BeforeSuite loaded config, @BeforeTest/@BeforeClass printed logs)
1. TestNG finds @Test on cartShowsCorrectProductTest
2. @BeforeMethod setUp()         → Chrome opens → saucedemo.com login page
3. LoginPage.login(...)          → waits for username box, types, types password, clicks Login
4. ProductsPage.addProductToCart("Sauce Labs Backpack")
                                 → waits until that product's button is clickable, clicks it
5. ProductsPage.openCart()       → waits for the cart icon, clicks it
6. CartPage checks
     Assert title == "Your Cart"                  ✔
     Assert number of items == 1                  ✔
     Assert first item == "Sauce Labs Backpack"   ✔
7. @AfterMethod tearDown()       → driver.quit(), Chrome closes
8. TestNG records PASS and moves on
```

If step 6 failed, TestNG would record **FAIL** with the message and stack trace – step 7 still runs.

---

## 11. Glossary

| Term | Meaning |
|------|---------|
| WebDriver | Selenium's interface for controlling a browser |
| Locator (`By`) | A way to find an element: id, className, cssSelector, xpath |
| Page Object | A class representing one screen: locators + actions |
| Explicit wait | Wait for a specific condition, up to a timeout |
| Assertion | A check – if false, the test fails |
| Annotation | `@Something` – tells TestNG what a method is for |
| Suite | A set of tests defined in a TestNG XML file |
| Smoke test | Small, quick set of the most critical tests |
| Regression test | Full set, run to make sure nothing that worked before is broken |
| Headless | Browser running without a visible window |
| Flaky test | Sometimes passes, sometimes fails, with no code change |
| Thread | One "worker" running code; parallel = several threads at once |
| ThreadLocal | A Java variable with a separate value for each thread |
| Listener | A class TestNG calls automatically on events (test start, pass, fail…) |
| Retry analyzer | Decides whether a failed test should run again |
| Annotation transformer | Changes `@Test` annotations at runtime, before the tests run |

---

## 12. Possible improvements

1. Add a test for an empty username **and** empty password to `LoginDataProvider`.
2. Add `removeProductFromCart(String name)` to `CartPage` and a test for it.
3. Add a `sortBy(String option)` method to `ProductsPage` and test "Price (low to high)".
4. Move the 10-second wait timeout into `config.properties`.
5. Support `parallel="methods"` using `ThreadLocal<WebDriver>`.
