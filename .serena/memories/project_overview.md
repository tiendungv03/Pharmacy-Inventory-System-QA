Purpose: QA automation for Pharmacy Inventory System (PIS) UI using Selenium + TestNG. Tests are organized by PIS feature/module (e.g., PIS-2, PIS-9). Branch workflow in README: feature/* -> develop -> main (PR + squash, no direct push).

Structure (root):
- ui-tests/: Maven module for UI tests
  - src/test/java/vn/pis/ui/base: BaseTest
  - src/test/java/vn/pis/ui/pages: Page Objects
  - src/test/java/vn/pis/ui/tests: Test classes (PIS* naming)
  - src/test/java/vn/pis/ui/util: Config/TestEnv helpers
  - src/test/resources/config/common.properties: env settings
  - src/test/resources/testng.xml: TestNG suite entrypoint
- reports/: test reports (Surefire/Allure per README)
- .vscode/, .metadata/ etc.
