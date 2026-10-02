# Project Guide – How This Project Works (Easy Explanation)

This guide explains the project in plain words: **what** it does, **why** each file exists,
and **how** everything connects when you run a test.

---

## 1. What is this project?

We are pretending to be a QA engineer testing an online shop: **https://www.saucedemo.com/**.

Instead of a human clicking through the site every day, we wrote **Java code that controls Chrome**.
The code logs in, adds a product to the cart, opens the cart, logs out… and after each action it
**checks** whether the website behaved correctly.

> Think of it like a robot tester:
> **Selenium** = the robot's hands (clicks, types, reads the screen)
> **TestNG** = the robot's checklist (which tests to run, did each pass or fail, report)
> **Maven** = the robot's toolbox (downloads Selenium/TestNG, compiles and runs everything)

---

## 2. What are we achieving?

| Goal | How the project does it |
|------|-------------------------|
| Check the most important user flows automatically | 10 test runs: login, invalid logins, logout, products, add to cart, cart |
| Catch bugs fast | One command (`mvn clean test`) runs everything in ~15 seconds |
| Keep code easy to maintain | Page Object Model – if a button changes, fix it in **one** place |
| Make tests reliable (not flaky) | Explicit waits instead of `Thread.sleep()` |
| Run only important tests when needed | `smoke` and `regression` groups |
| Finish faster | 3 test classes run in parallel (`parallel="classes"`) |
| Test many inputs without copy-paste | `@DataProvider` – one test, 4 data rows |
| See results clearly | TestNG built-in HTML and XML reports |

---

## 3. The big picture

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
   │  • "run LoginTest, ProductsTest,         │
   │     CartTest" – 3 classes in parallel    │
   └──────────────────────────────────────────┘
                          │   for EVERY test method:
                          ▼
   ┌──────────────────────────────────────────┐
   │ BaseTest  @BeforeSuite (once)            │
   │  • read config.properties                │
   │ @BeforeTest / @BeforeClass (log only)    │
   │ @BeforeMethod (every test)               │
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

**Important:** every test gets a **brand new Chrome** and closes it at the end.
So tests never affect each other.

---

## 4. Folder tour

```
selenium-testng-project/
├── pom.xml                  Maven: libraries + how to run tests
├── testng.xml               Full suite (all tests)
├── README.md                Short GitHub front page
├── PROJECT_GUIDE.md         This file
└── src/test/
    ├── java/
    │   ├── base/
    │   │   └── BaseTest.java          Open & close Chrome
    │   ├── data/
    │   │   └── LoginDataProvider.java Test data for invalid logins
    │   ├── pages/
    │   │   ├── LoginPage.java         Login screen
    │   │   ├── ProductsPage.java      Products screen (+ menu, cart icon)
    │   │   └── CartPage.java          Cart screen
    │   └── tests/
    │       ├── LoginTest.java         Login / logout tests
    │       ├── ProductsTest.java      Product list + add to cart tests
    │       └── CartTest.java          Cart contents test
    └── resources/
        └── config.properties          URL, username, password, headless
```

A simple rule to remember the 4 packages:

| Package | Answers the question |
|---------|----------------------|
| `base`  | "How do I **start and stop** the browser?" |
| `pages` | "**Where** is the button and **how** do I use it?" |
| `tests` | "**What** am I testing and **what** should happen?" |
| `data`  | "**Which inputs** should I try?" |

---

## 5. Each file in easy words

### 5.1 `config.properties` – the settings file

```properties
baseUrl=https://www.saucedemo.com/
username=standard_user
password=secret_sauce
headless=false
```

**Why?** If the URL or password changes, you edit **one line here** instead of every test.
`headless=false` means you see the Chrome window. `true` means Chrome runs invisibly.

---

### 5.2 `BaseTest.java` – the "open and close browser" class

Every test class **extends** `BaseTest`, so every test automatically gets:

- `driver` – the Chrome browser you control
- `config` – the values from `config.properties`

What it does – it uses **all 8** TestNG setup/teardown annotations:

| Annotation | When it runs | What it does in this project |
|------------|--------------|------------------------------|
| `@BeforeSuite` | **Once**, before everything | Loads `config.properties` into a `static` field shared by all classes |
| `@BeforeTest` | Once per `<test>` tag in the XML | Prints which `<test>` is starting |
| `@BeforeClass` | Once per test class | Prints the class name |
| `@BeforeMethod` | **Before each** `@Test` | Sets Chrome options → opens Chrome → opens saucedemo.com |
| `@AfterMethod` | **After each** `@Test` (even if it failed) | `driver.quit()` – closes Chrome |
| `@AfterClass` | Once per test class | Prints the class name |
| `@AfterTest` | Once per `<test>` tag | Prints which `<test>` finished |
| `@AfterSuite` | **Once**, after everything | Prints "all tests finished" |

Honest note: only `@BeforeSuite`, `@BeforeMethod` and `@AfterMethod` do **real work** here.
The others just print a log line so you can **see the order** in the console. In a bigger project,
`@BeforeClass` might log in once for a whole class, or `@BeforeSuite` might prepare test data in a database.

The log lines start with the **thread name**, e.g. `[TestNG-test-SauceDemo UI Tests-2]`,
so you can see which tests run in parallel (see section 5.10).

Small details worth knowing:

- **Password-manager settings** – new Chrome shows a "your password was found in a data breach"
  pop-up after logging in with `secret_sauce`. We turn that off so it does not cover the page.
- **`-Dheadless=true`** – lets you switch to invisible mode from the command line without editing the file.
- **No ChromeDriver download needed** – Selenium 4.6+ has *Selenium Manager*, which finds the right driver automatically.
- **`alwaysRun = true`** (on all 8 methods) – without it, when you run only the `smoke` group, TestNG would skip setup
  (because setup has no group) when filtering with `-Dgroups=smoke`, Chrome would never open, and every test would crash.
- **`ITestContext`** and **`Method`** parameters – TestNG fills these in automatically. We use them only to print
  the `<test>` name and the test method name.

---

### 5.3 Page classes – the Page Object Model (POM)

**Idea:** one Java class per screen of the website. Each class contains:

1. **Locators** – how to find elements (`By.id("login-button")`)
2. **Actions** – methods that do something on the page (`login()`, `addProductToCart()`)

Tests **never** contain locators. They only call page methods.

#### `LoginPage.java`

| Locator | Element |
|---------|---------|
| `By.id("user-name")` | Username box |
| `By.id("password")` | Password box |
| `By.id("login-button")` | Login button |
| `By.cssSelector("[data-test='error']")` | Red error message |

| Method | What it does |
|--------|--------------|
| `enterUsername(...)`, `enterPassword(...)`, `clickLogin()` | One small step each |
| `login(username, password)` | All three steps together |
| `getErrorMessage()` | Waits for the error to appear, returns its text |
| `isLoginButtonDisplayed()` | Used to prove we are back on the login page after logout |

#### `ProductsPage.java`

| Method | What it does |
|--------|--------------|
| `getPageTitle()` | Returns "Products" |
| `getProductCount()` | How many product cards are shown |
| `getProductNames()` / `getProductPrices()` | Lists of all names / prices |
| `addProductToCart("Sauce Labs Backpack")` | Clicks "Add to cart" for **that** product |
| `getCartBadgeCount()` | The little number on the cart icon |
| `openCart()` | Click the cart icon |
| `logout()` | Open the ☰ menu → click Logout |

The interesting one is `addProductToCart`. It builds a **dynamic XPath**:

```
//div[text()='Sauce Labs Backpack']/ancestor::div[@class='inventory_item']//button
```

In English: *"find the text 'Sauce Labs Backpack', go up to its product card, then find the button inside that card."*
So the same method works for **any** product name.

#### `CartPage.java`

| Method | What it does |
|--------|--------------|
| `getPageTitle()` | Returns "Your Cart" |
| `getCartItemNames()` | List of product names in the cart |

---

### 5.4 Explicit waits – why tests don't randomly fail

Websites take time to load. If Selenium looks for a button **before** it appears, you get
`NoSuchElementException`.

**Bad fix:** `Thread.sleep(3000)` – always waits 3 seconds, even when the page loaded in 0.2 seconds,
and still fails if the page needs 4 seconds.

**Good fix:** an explicit wait:

```java
wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.elementToBeClickable(menuButton)).click();
```

It means: *"check again and again, up to 10 seconds, and continue **the moment** the button is clickable."*

| Condition we use | Meaning |
|------------------|---------|
| `visibilityOfElementLocated` | Element is on the page **and** visible |
| `visibilityOfAllElementsLocatedBy` | All matching elements are visible |
| `elementToBeClickable` | Visible **and** enabled – safe to click |

> **Real story from this project:** `logoutTest` first used `driver.findElement(menuButton).click()`
> right after login. It failed in about **half** of the runs with `NoSuchElementException`, because the products
> page had not finished loading. Changing it to `elementToBeClickable` fixed it – 6 runs in a row passed.
> That is a perfect example to tell in an interview.

---

### 5.5 Test classes – the actual checks

Each test follows the same pattern, often called **Arrange → Act → Assert**:

```java
// Arrange: create the page object
LoginPage loginPage = new LoginPage(driver);
// Act: do something
loginPage.login("standard_user", "wrong_password");
// Assert: check the result
Assert.assertEquals(loginPage.getErrorMessage(), "Epic sadface: ...");
```

If an `Assert` fails, the test stops there and is marked **FAILED** in the report.

#### All tests

| Test | Class | What it proves | Groups |
|------|-------|----------------|--------|
| `validLoginTest` | LoginTest | Correct login → "Products" page, URL has `inventory.html` | smoke, regression |
| `invalidLoginTest` ×4 | LoginTest | Wrong inputs show the correct error message | regression |
| `logoutTest` | LoginTest | Logout returns to the login page | regression |
| `productsPageIsDisplayedTest` | ProductsTest | "Products" title and at least one product shown | smoke, regression |
| `productsHaveNamesAndPricesTest` | ProductsTest | Every product has a non-empty name and a `$` price | regression |
| `addProductToCartTest` | ProductsTest | Adding a backpack makes the cart badge show "1" | smoke, regression |
| `cartShowsCorrectProductTest` | CartTest | Cart page contains exactly the backpack | smoke, regression |

**Total: 10 test runs** (the invalid login test runs 4 times).

---

### 5.6 `LoginDataProvider.java` – data-driven testing

Instead of writing 4 nearly identical tests, we write **one** test and give it **4 rows of data**:

| username | password | expected error |
|----------|----------|----------------|
| standard_user | wrong_password | Username and password do not match any user in this service |
| *(empty)* | secret_sauce | Username is required |
| standard_user | *(empty)* | Password is required |
| locked_out_user | secret_sauce | Sorry, this user has been locked out. |

How the test connects to it:

```java
@Test(dataProvider = "invalidLoginData", dataProviderClass = LoginDataProvider.class)
public void invalidLoginTest(String username, String password, String expectedError)
```

- `dataProvider` = the **name** of the data
- `dataProviderClass` = the **class** where the data lives
- The data method must be **`static`** because it is in a different class
- Each row's values are passed into the test's parameters, in order

---

### 5.7 Groups, smoke suite and `dependsOnMethods`

**Groups** are labels on tests:

- `smoke` – the 4 most important tests (login → products → add to cart → cart). Quick health check.
- `regression` – every test. Full check.

We keep **only one** XML file (`testng.xml`). To run just one group, pass the group name on the command line:

```bash
mvn clean test -Dgroups=smoke         # only the 4 smoke tests
mvn clean test -Dgroups=regression    # all 10 (every test is in regression)
mvn clean test                        # no filter – everything in testng.xml
```

`-Dgroups` is handled by Maven's Surefire plugin: it still runs `testng.xml`, but only the tests with that group label.
The name must match exactly – group names are case-sensitive (`Smoke` would run nothing).

> Another way you should know for interviews: inside a TestNG XML file you can write
> `<groups><run><include name="smoke"/></run></groups>`. That also filters by group, but it would
> need a second XML file (one for smoke, one for everything), so we use `-Dgroups` instead.

**`dependsOnMethods`** – in `ProductsTest`:

```java
@Test(dependsOnMethods = "productsPageIsDisplayedTest")
public void addProductToCartTest()
```

Meaning: *"only run me if `productsPageIsDisplayedTest` passed."*
If the products page is broken, adding to cart makes no sense, so TestNG marks it **SKIPPED**
instead of showing a second, confusing failure. (We tested this: 1 failed + 1 skipped.)

---

### 5.8 `testng.xml` – the list of tests to run

```xml
<suite name="SauceDemo Test Suite" parallel="classes" thread-count="3">
    <test name="SauceDemo UI Tests">
        <classes>
            <class name="tests.LoginTest"/>
            <class name="tests.ProductsTest"/>
            <class name="tests.CartTest"/>
        </classes>
    </test>
</suite>
```

Structure: **suite** → contains **test(s)** → contains **classes** → which contain `@Test` **methods**.

This structure is exactly what the Before/After annotations match:

| XML level | Annotations |
|-----------|-------------|
| `<suite>` | `@BeforeSuite` / `@AfterSuite` |
| `<test>` | `@BeforeTest` / `@AfterTest` |
| `<class>` | `@BeforeClass` / `@AfterClass` |
| each `@Test` method | `@BeforeMethod` / `@AfterMethod` |

---

### 5.9 `pom.xml` – Maven's instructions

| Part | Easy meaning |
|------|--------------|
| `selenium-java` dependency | "Download Selenium for me" |
| `testng` dependency (scope `test`) | "Download TestNG, only for tests" |
| `maven-compiler-plugin` | "Compile my Java code (as Java 11)" |
| `maven-surefire-plugin` | "When I type `mvn test`, run TestNG with this suite file" |
| `<suiteXmlFile>testng.xml</suiteXmlFile>` | The one suite file Surefire runs |

---

### 5.10 Execution order and parallel run

**Order** (real console output from a run, shortened):

```
[main]                  Before Suite : config.properties loaded
[main]                  Before Test  : <test name="SauceDemo UI Tests"> starting
[...Tests-1]            Before Class : LoginTest        ┐
[...Tests-3]            Before Class : CartTest         ├ 3 classes start at the SAME time
[...Tests-2]            Before Class : ProductsTest     ┘
[...Tests-3]            Before Method: cartShowsCorrectProductTest - opening Chrome
[...Tests-2]            Before Method: productsPageIsDisplayedTest - opening Chrome
[...Tests-1]            Before Method: invalidLoginTest - opening Chrome
   ... each thread runs its own class's tests one by one ...
[...Tests-3]            After Class  : CartTest
[...Tests-2]            After Class  : ProductsTest
[...Tests-1]            After Class  : LoginTest
[main]                  After Test   : <test name="SauceDemo UI Tests"> finished
[main]                  After Suite  : all tests finished
```

Easy way to remember: **Suite → Test → Class → Method → (your @Test) → Method → Class → Test → Suite**
(like opening and closing nested boxes).

**Parallel** – set on the `<suite>` tag:

| Setting | Meaning |
|---------|---------|
| `parallel="classes"` | Each test class runs in its own thread, at the same time |
| `thread-count="3"` | Maximum 3 threads → maximum 3 Chrome windows at once |

Result: the full run went from about **16 s** to about **10 s**.

**Why `classes` and not `methods`?**
TestNG creates **one object per test class**, and `driver` is a normal field in that object.

- `parallel="classes"` → one class = one thread = one object = its own `driver`. **Safe.**
- `parallel="methods"` → two methods of the **same class** would run at the same time and
  share the **same** `driver` field – one test would close the other test's browser. **Not safe.**
  To use it you'd need `ThreadLocal<WebDriver>` (each thread gets its own driver) – a good interview
  talking point, but more than this project needs.

Other `parallel` values you should know: `tests` (each `<test>` tag in parallel) and `instances`.

**Why is `config` `static` but `driver` is not?**
`config` is read-only and the same for everyone, so one shared copy is fine.
`driver` must be **different for every running test**, so it can't be shared.

---

## 6. One test, step by step: `cartShowsCorrectProductTest`

```
0. (Already done once: @BeforeSuite loaded config, @BeforeTest and @BeforeClass printed logs)
1. TestNG sees @Test on cartShowsCorrectProductTest
2. BaseTest.setUp()  [@BeforeMethod] → Chrome opens → saucedemo.com login page
3. new LoginPage(driver).login("standard_user", "secret_sauce")
       → waits for username box, types, types password, clicks Login
4. new ProductsPage(driver)
   addProductToCart("Sauce Labs Backpack")
       → waits until that product's button is clickable, clicks it
5. openCart()               → waits for cart icon, clicks it
6. new CartPage(driver)
   Assert title == "Your Cart"                          ✔
   Assert number of items == 1                          ✔
   Assert first item == "Sauce Labs Backpack"           ✔
7. BaseTest.tearDown() [@AfterMethod] → driver.quit(), Chrome closes
8. TestNG records PASS and moves to the next test
```

If step 6 failed, TestNG would record **FAIL** with the message and stack trace, and step 7 still runs.

---

## 7. Reports – where to see results

After `mvn clean test`, open these in a browser:

| File | Use it for |
|------|-----------|
| `target/surefire-reports/emailable-report.html` | **One-page summary** – passed / failed / skipped, times, errors |
| `target/surefire-reports/index.html` | **Detailed** – click each test, see parameters (DataProvider rows), stack traces |
| `target/surefire-reports/testng-results.xml` | Same results in XML, for tools like Jenkins |

If you run from IntelliJ / Eclipse instead, the same reports appear in a `test-output/` folder.

| Status | Meaning |
|--------|---------|
| **Passed** | All asserts were true |
| **Failed** | An assert was false or an exception happened – read the message and stack trace |
| **Skipped** | Didn't run – e.g. the method it `dependsOn` failed |

**Stack trace tip:** read from the top, then find the first line that mentions **your** package
(`tests.` or `pages.`) – that's the line in your code where it broke.

---

## 8. Command cheat sheet

```bash
mvn clean test                                   # run everything, Chrome visible
mvn clean test -Dheadless=true                   # run everything, no window
mvn clean test -Dgroups=smoke                    # only smoke tests
mvn test -Dtest=LoginTest                        # one class
mvn test -Dtest=LoginTest#logoutTest             # one method
```

To **watch** a test slowly: set a breakpoint in the IDE, choose **Debug Test**, and press F10
line by line while watching Chrome. (Don't add `Thread.sleep()` for this.)

---

## 9. Mini glossary

| Word | Meaning |
|------|---------|
| WebDriver | Selenium's interface to control a browser |
| Locator (`By`) | A way to find an element: id, className, cssSelector, xpath |
| Page Object | A class representing one screen: locators + actions |
| Explicit wait | Wait for a specific condition, up to a timeout |
| Assertion | A check: if false, the test fails |
| Annotation | `@Something` – tells TestNG what a method is |
| Suite | A group of tests defined in a TestNG XML file |
| Smoke test | Small, quick set of the most critical tests |
| Regression test | Full set, run to make sure nothing old broke |
| Headless | Browser runs without a visible window |
| Flaky test | Sometimes passes, sometimes fails, with no code change |
| Thread | One "worker" that runs code; parallel = several threads at once |
| ThreadLocal | A Java variable that has a separate value for each thread |

---

## 10. Practice exercises (to make it your own)

1. Add a 5th row to `LoginDataProvider` (e.g. both fields empty). What error does the site show?
2. Add `removeProductFromCart(String name)` to `CartPage` and write a test for it.
3. Make a test fail on purpose (change `"Products"` to `"Product"`), run it, and read the failure in `index.html`. Then undo it.
4. Add a `sortBy(String option)` method to `ProductsPage` and test "Price (low to high)".
5. Move the `10` second timeout into `config.properties`.
6. Change `thread-count` to `1` and compare the run time. Then try `parallel="methods"` and watch tests break – explain why.

If you can do these and explain sections 3, 5.3, 5.4 and 5.10 out loud, you are well prepared to talk about this project in an interview.
