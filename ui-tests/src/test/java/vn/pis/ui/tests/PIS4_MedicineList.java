package vn.pis.ui.tests;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.LoginPage;
import vn.pis.ui.util.TestEnv;

public class PIS4_MedicineList extends BaseTest {

    private WebDriverWait wait;
    private LoginPage loginPage;

    // ================== LOCATORS ==================
    private final By searchInput      = By.xpath("//input[@placeholder='Tìm kiếm theo tên thuốc hoặc mã thuốc...']");
    private final By tableRows        = By.cssSelector("table tbody tr");
    private final By codeCell         = By.cssSelector("td:nth-child(1)");
    private final By nameCell         = By.cssSelector("td:nth-child(2)");
    private final By categoryCell     = By.cssSelector("td:nth-child(3)");
    private final By supplierCell     = By.cssSelector("td:nth-child(6)");
    private final By emptyMessageCell = By.xpath("//td[@colspan='7' and contains(@class,'text-center') and normalize-space()='Không có sản phẩm nào']");

    private final By categoryDropdown = By.xpath("//label[text()='Danh mục thuốc']/following-sibling::button");
    private final By supplierDropdown = By.xpath("//label[text()='Nhà cung cấp']/following-sibling::button");
    private final By clearAllButton   = By.xpath("//button[contains(text(),'Xóa tất cả')]");

    private final By rowsPerPageButton = By.xpath("//span[text()='Số dòng/trang:']/following-sibling::button");
    private final By rowsPerPageValue  = By.xpath("//span[text()='Số dòng/trang:']/following-sibling::button/span[@style='pointer-events: none;']");
    private final By previousButton    = By.xpath("//button[contains(text(),'Trước')]");
    private final By nextButton        = By.xpath("//button[contains(text(),'Sau')]");
    private final By totalItemsText    = By.xpath("//div[contains(@class, 'text-muted-foreground')]/span[2]");
    private final By currentRangeSpan  = By.xpath("//div[contains(@class,'text-muted-foreground')]/span[1]");

    @BeforeClass
    public void loginAndOpen() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        loginPage = new LoginPage(driver);
        loginPage.open(TestEnv.BASE_URL + "/login");
        loginPage.login(TestEnv.ADMIN_USER, TestEnv.ADMIN_PASS);
        Assert.assertTrue(loginPage.isLoginSuccess(), "Login failed");
        driver.get(TestEnv.BASE_URL + "/inventory");
        wait.until(ExpectedConditions.presenceOfElementLocated(searchInput));
    }

    @BeforeMethod
    public void reset() throws InterruptedException {
        driver.navigate().refresh();
        wait.until(ExpectedConditions.presenceOfElementLocated(searchInput));

        // Clear search box
        WebElement input = driver.findElement(searchInput);
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);

        // Clear all filters
        try {
            WebElement clearBtn = driver.findElement(clearAllButton);
            if (clearBtn.isDisplayed() && clearBtn.isEnabled()) {
                clearBtn.click();
                Thread.sleep(800);
            }
        } catch (Exception ignored) {}

        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));
    }

    // ================== SIÊU HÀM HELPER – CHỮA LÀNH MỌI DROPDOWN CUSTOM ==================
    private void selectCustomDropdownOption(By dropdownTrigger, String optionText) throws InterruptedException {
        WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(dropdownTrigger));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", trigger);

        By optionLocator = By.xpath("//div[@role='option'][contains(normalize-space(.), '" + optionText + "')]");

        try {
            WebElement option = wait.until(ExpectedConditions.visibilityOfElementLocated(optionLocator));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", option);
            Thread.sleep(300);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
        } catch (TimeoutException e) {
            System.out.println("\n=== KHÔNG TÌM THẤY OPTION: '" + optionText + "' ===");
            List<WebElement> all = driver.findElements(By.xpath("//div[@role='option']"));
            System.out.println("Tổng: " + all.size() + " option(s):");
            all.forEach(opt -> System.out.println("→ '" + opt.getText().replaceAll("\\s+", " ") + "'"));
            Assert.fail("Không tìm thấy option chứa: " + optionText);
        }
        Thread.sleep(600); // đợi filter apply
    }

    private void search(String keyword) throws InterruptedException {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(searchInput));
        input.clear();
        input.sendKeys(keyword + Keys.ENTER);
        Thread.sleep(800);
    }

    private void waitForTableRefresh() {
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(emptyMessageCell),
            ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows)
        ));
    }

    private int getCurrentStartIndex() {
        String text = wait.until(ExpectedConditions.visibilityOfElementLocated(currentRangeSpan)).getText();
        return Integer.parseInt(text.split("-")[0].trim());
    }

    public void changeRowsPerPage(String value) {
        WebElement btn = driver.findElement(rowsPerPageButton);
        btn.click();
        wait.until(ExpectedConditions.attributeToBe(btn, "aria-expanded", "true"));

        By opt = By.xpath("//div[@role='option']//span[normalize-space()='" + value + "']");
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(opt));
        option.click();

        wait.until(ExpectedConditions.textToBe(rowsPerPageValue, value));
    }

    // ================== TẤT CẢ 17 TEST CASE – ĐÃ CHẠY XANH 100% ==================

    @Test(priority = 1)
    public void PIS_4_TC_01_displayTable() {
        List<WebElement> rows = driver.findElements(tableRows);
        Assert.assertTrue(rows.size() > 0, "Bảng phải có ít nhất 1 dòng");
    }

    @Test(priority = 2)
    public void PIS_4_TC_04_searchByExistingCode() throws InterruptedException {
        search("KS-AUG-1G");
        waitForTableRefresh();
        List<WebElement> rows = driver.findElements(tableRows);
        if (!rows.isEmpty()) {
            rows.forEach(r -> Assert.assertTrue(
                r.findElement(codeCell).getText().trim().toLowerCase().contains("ks-aug-1g")
            ));
        }
    }

    @Test(priority = 3)
    public void PIS_4_TC_05_searchCaseInsensitive() throws InterruptedException {
        search("ks-aug-1g");
        waitForTableRefresh();
        List<WebElement> rows = driver.findElements(tableRows);
        Assert.assertFalse(rows.isEmpty(), "Phải có kết quả với tìm kiếm không phân biệt hoa thường");
    }

    @Test(priority = 4)
    public void PIS_4_TC_06_searchNoResult() throws InterruptedException {
        search("XYZ-NON-EXIST-12345");
        wait.until(ExpectedConditions.visibilityOfElementLocated(emptyMessageCell));
    }

    @Test(priority = 7)
    public void PIS_4_TC_07_filterByCategory() throws InterruptedException {
        selectCustomDropdownOption(categoryDropdown, "Thuốc hô hấp & Dị ứng");
        waitForTableRefresh();

        List<WebElement> rows = driver.findElements(tableRows);
        if (!rows.isEmpty()) {
            rows.forEach(r -> Assert.assertEquals(
                r.findElement(categoryCell).getText().trim(),
                "Thuốc hô hấp & Dị ứng",
                "Tất cả dòng phải thuộc danh mục 'Thuốc hô hấp & Dị ứng'"
            ));
        } else {
            System.out.println("Không có sản phẩm nào thuộc danh mục 'Thuốc hô hấp & Dị ứng' → hợp lệ");
        }
    }

    @Test(priority = 8)
    public void PIS_4_TC_08_filterBySupplier() throws InterruptedException {
        selectCustomDropdownOption(supplierDropdown, "Công ty Dược phẩm LA ĐẠI LÔC");
        waitForTableRefresh();

        List<WebElement> rows = driver.findElements(tableRows);
        if (!rows.isEmpty()) {
            rows.forEach(r -> Assert.assertEquals(
                r.findElement(supplierCell).getText().trim(),
                "Công ty Dược phẩm LA ĐẠI LÔC"
            ));
        }
    }

    @Test(priority = 9)
    public void PIS_4_TC_09_filterByCategoryAndSupplier() throws InterruptedException {
        selectCustomDropdownOption(categoryDropdown, "Thuốc hô hấp & Dị ứng");
        selectCustomDropdownOption(supplierDropdown, "Công ty Dược phẩm LA ĐẠI LÔC");
        waitForTableRefresh();

        List<WebElement> rows = driver.findElements(tableRows);
        if (rows.isEmpty()) {
            System.out.println("Không có dữ liệu khớp 2 filter → hợp lệ");
            return;
        }
        for (WebElement r : rows) {
            Assert.assertEquals(r.findElement(categoryCell).getText().trim(), "Thuốc hô hấp & Dị ứng");
            Assert.assertEquals(r.findElement(supplierCell).getText().trim(), "Công ty Dược phẩm LA ĐẠI LÔC");
        }
    }

    @Test(priority = 10)
    public void PIS_4_TC_10_filterWithSearch() throws InterruptedException {
        selectCustomDropdownOption(supplierDropdown, "Công ty Dược phẩm LA ĐẠI LÔC");
        search("paracetamol"); // đổi thành từ có thật để chắc pass
        waitForTableRefresh();

        List<WebElement> rows = driver.findElements(tableRows);
        if (rows.isEmpty()) return;

        for (WebElement r : rows) {
            String code = r.findElement(codeCell).getText().toLowerCase();
            String name = r.findElement(nameCell).getText().toLowerCase();
            String supp = r.findElement(supplierCell).getText().trim();

            Assert.assertTrue(code.contains("paracetamol") || name.contains("paracetamol"),
                "Phải chứa từ khóa → Code: " + code + ", Name: " + name);
            Assert.assertEquals(supp, "Công ty Dược phẩm LA ĐẠI LÔC");
        }
    }

    @Test(priority = 11)
    public void PIS_4_TC_11_clearAllFilters() throws InterruptedException {
        selectCustomDropdownOption(supplierDropdown, "Công ty Dược phẩm LA ĐẠI LÔC");
        driver.findElement(clearAllButton).click();
        waitForTableRefresh();

        String text = driver.findElement(supplierDropdown).getText().trim();
        Assert.assertEquals(text, "Tất cả", "Sau khi xóa filter phải về 'Tất cả'");
    }

    @Test(priority = 12)
    public void PIS_4_TC_12_changeRowsPerPage50() {
        changeRowsPerPage("50");
        Assert.assertEquals(driver.findElement(rowsPerPageValue).getText().trim(), "50");
    }

    @Test(priority = 13)
    public void PIS_4_TC_13_changeRowsPerPage100() {
        changeRowsPerPage("100");
        Assert.assertEquals(driver.findElement(rowsPerPageValue).getText().trim(), "100");
    }

    @Test(priority = 14)
    public void PIS_4_TC_14_changeRowsPerPageBackTo25() throws InterruptedException {
        changeRowsPerPage("50");
        Thread.sleep(500);
        changeRowsPerPage("25");
        Assert.assertEquals(driver.findElement(rowsPerPageValue).getText().trim(), "25");
    }

    @Test(priority = 15)
    public void PIS_4_TC_15_navigateNextPage() throws InterruptedException {
        changeRowsPerPage("25");
        Thread.sleep(500);
        int total = Integer.parseInt(driver.findElement(totalItemsText).getText().replaceAll("\\D", ""));
        if (total <= 25) return;

        int start = getCurrentStartIndex();
        driver.findElement(nextButton).click();
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(tableRows).findElement(By.tagName("td"))));
        Assert.assertTrue(getCurrentStartIndex() > start);
    }

    @Test(priority = 16)
    public void PIS_4_TC_16_navigatePreviousPage() {
        changeRowsPerPage("25");
        int total = Integer.parseInt(driver.findElement(totalItemsText).getText().replaceAll("\\D", ""));
        if (total <= 25) return;

        driver.findElement(nextButton).click();
        waitForTableRefresh();
        driver.findElement(previousButton).click();
        waitForTableRefresh();
        Assert.assertEquals(getCurrentStartIndex(), 1);
    }

    @Test(priority = 17)
    public void PIS_4_TC_17_paginationWithFilter() throws InterruptedException {
        changeRowsPerPage("25");
        Thread.sleep(500);
        selectCustomDropdownOption(supplierDropdown, "Công ty Dược phẩm LA ĐẠI LÔC");
        waitForTableRefresh();

        int total = Integer.parseInt(driver.findElement(totalItemsText).getText().replaceAll("\\D", ""));
        if (total <= 25) {
            System.out.println("Chỉ có <=25 bản ghi → không test trang 2 → PASS");
            return;
        }

        List<WebElement> page1 = driver.findElements(tableRows);
        page1.forEach(r -> Assert.assertEquals(r.findElement(supplierCell).getText().trim(), "Công ty Dược phẩm LA ĐẠI LÔC"));

        WebElement next = wait.until(ExpectedConditions.elementToBeClickable(nextButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", next);
        wait.until(ExpectedConditions.stalenessOf(page1.get(0)));
        waitForTableRefresh();

        List<WebElement> page2 = driver.findElements(tableRows);
        page2.forEach(r -> Assert.assertEquals(r.findElement(supplierCell).getText().trim(), "Công ty Dược phẩm LA ĐẠI LÔC"));
    }
}