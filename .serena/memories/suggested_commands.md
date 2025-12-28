Common commands (Windows PowerShell):
- List files: `Get-ChildItem` (alias `ls`), search text: `rg "pattern"`.
- Run UI tests from module: `cd ui-tests; mvn clean test` (uses src/test/resources/testng.xml via surefire).
- IntelliJ run: open `ui-tests/`, run `src/test/resources/testng.xml`.
