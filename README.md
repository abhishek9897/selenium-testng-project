# Selenium TestNG Project – SauceDemo

A small UI test automation project for the demo e-commerce site
[saucedemo.com](https://www.saucedemo.com/), built with **Java, Selenium WebDriver, TestNG and Maven**
using the **Page Object Model (POM)**.

## Tech stack

| Tool | Version |
|------|---------|
| Java | 11 or newer |
| Selenium WebDriver | 4.50.0 |
| TestNG | 7.12.0 |
| Maven | 3.6.3 or newer |
| Browser | Google Chrome (latest) |

## Project structure

```
selenium-testng-project/
├── pom.xml                      # Maven dependencies and plugins
├── testng.xml                   # Full suite - runs every test (3 classes in parallel)
└── src/test/
    ├── java/
    │   ├── base/BaseTest.java       # Opens Chrome before each test, closes it after
    │   ├── data/LoginDataProvider.java  # Test data for invalid login (TestNG DataProvider)
    │   ├── pages/                   # Page objects: locators + page actions
    │   │   ├── LoginPage.java
    │   │   ├── ProductsPage.java
    │   │   └── CartPage.java
    │   └── tests/                   # Test classes: test steps + assertions
    │       ├── LoginTest.java
    │       ├── ProductsTest.java
    │       └── CartTest.java
    └── resources/config.properties  # URL, credentials, headless flag
```

## Test scenarios

| # | Test | Class | Groups |
|---|------|-------|--------|
| 1 | Valid login opens the products page | LoginTest | smoke, regression |
| 2–5 | Invalid login shows the right error (wrong password, empty username, empty password, locked-out user) – data-driven | LoginTest | regression |
| 6 | User can log out | LoginTest | regression |
| 7 | Products page is displayed after login | ProductsTest | smoke, regression |
| 8 | Every product has a name and a price | ProductsTest | regression |
| 9 | Adding a product updates the cart badge (depends on test 7) | ProductsTest | smoke, regression |
| 10 | Cart shows the product that was added | CartTest | smoke, regression |

TestNG features used: `@DataProvider` (in a separate class), `groups` (`smoke`, `regression`),
`dependsOnMethods`, all Before/After annotations (Suite, Test, Class, Method),
and parallel execution (`parallel="classes"`, `thread-count="3"`).

See [PROJECT_GUIDE.md](PROJECT_GUIDE.md) for a full, beginner-friendly explanation of how everything works.

## How to run

```bash
# Run all tests (Chrome window opens)
mvn clean test

# Run without opening a browser window
mvn clean test -Dheadless=true

# Run only the smoke tests
mvn clean test -Dgroups=smoke

# Run a single class or a single test method
mvn test -Dtest=LoginTest
mvn test -Dtest=LoginTest#logoutTest
```

From IntelliJ IDEA or Eclipse: right-click `testng.xml` → **Run**.

## Test reports

TestNG generates its built-in reports automatically:

| How you ran the tests | Report folder |
|-----------------------|---------------|
| `mvn test` | `target/surefire-reports/` |
| IDE / TestNG directly | `test-output/` |

Open `index.html` (full interactive report) or `emailable-report.html` (one-page summary) in a browser.
`testng-results.xml` contains the same results in XML for tools such as CI servers.

## Test data

The site publicly lists its demo credentials on the login page:
`standard_user` / `secret_sauce`. They are stored in `src/test/resources/config.properties`.
