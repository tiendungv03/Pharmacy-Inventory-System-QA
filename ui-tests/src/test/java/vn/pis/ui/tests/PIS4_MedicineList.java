package vn.pis.ui.tests;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.LoginPage;
import vn.pis.ui.util.TestEnv;

public class PIS4_MedicineList extends BaseTest {

    private WebDriverWait wait;
    private LoginPage loginPage;

    // Locators
    private final By searchInput      = By.xpath("//input[@placeholder='Tìm kiếm theo tên thuốc hoặc mã thuốc...']");
    private final By tableRows        = By.cssSelector("table tbody tr");
    private final By codeCell         = By.cssSelector("td:nth-child(1)");
    private final By nameCell         = By.cssSelector("td:nth-child(2)");
    private final By categoryCell     = By.cssSelector("td:nth-child(3)");
    private final By supplierCell     = By.cssSelector("td:nth-child(6)");
    private final By emptyMessageCell = By.xpath("//td[@colspan='7' and contains(@class,'text-center') and normalize-space()='Không có sản phẩm nào']");
    
    // Filter locators
    private final By categoryDropdown = By.xpath("//label[text()='Danh mục thuốc']/following-sibling::button");
    private final By supplierDropdown = By.xpath("//label[text()='Nhà cung cấp']/following-sibling::button");
    private final By clearAllButton   = By.xpath("//button[contains(text(),'Xóa tất cả')]");
    
    // Pagination locators
    private final By rowsPerPageButton = By.xpath("//span[text()='Số dòng/trang:']/following-sibling::button");
    private final By rowsPerPageValue  = By.xpath("//span[text()='Số dòng/trang:']/following-sibling::button/span[@style='pointer-events: none;']");
    private final By previousButton    = By.xpath("//button[contains(text(),'Trước')]");
    private final By nextButton        = By.xpath("//button[contains(text(),'Sau')]");
    private final By pageNumberButton  = By.xpath("//button[contains(@class,'bg-medical-blue')]");
    private final By totalItemsText    = By.xpath("//div[contains(@class, 'text-muted-foreground')]/span[2]");

    @BeforeClass
    public void loginAndOpen() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        loginPage = new LoginPage(driver);
        loginPage.open(TestEnv.BASE_URL + "/login");
        loginPage.login(TestEnv.ADMIN_USER, TestEnv.ADMIN_PASS);
        Assert.assertTrue(loginPage.isLoginSuccess(), "Login failed");
        driver.get(TestEnv.BASE_URL + "/inventory");
        wait.until(ExpectedConditions.presenceOfElementLocated(searchInput));
    }

    @BeforeMethod
    public void reset() throws InterruptedException {
        driver.get(TestEnv.BASE_URL + "/inventory");
        wait.until(ExpectedConditions.presenceOfElementLocated(searchInput));
        
        // Clear search
        WebElement input = driver.findElement(searchInput);
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        
        // Clear all filters
        WebElement clearAll = driver.findElement(clearAllButton);
        if (clearAll.isEnabled()) {
            clearAll.click();
        }
        
        // Chờ để đảm bảo data load lại sau khi reset filters
        Thread.sleep(1000); 
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));
    }

    // ========== EXISTING TEST CASES ==========
    
    // PIS-4-TC-01: Hiển thị bảng danh sách thuốc
    @Test(priority = 1)
    public void PIS_4_TC_01_displayTable() {
        List<WebElement> rows = driver.findElements(tableRows);
        Assert.assertTrue(rows.size() > 0, "Danh sách thuốc phải hiển thị >= 1 dòng");
        String firstCode = rows.get(0).findElement(codeCell).getText().trim();
        Assert.assertFalse(firstCode.isEmpty(), "Ô Mã thuốc dòng đầu không được rỗng");
    }

    // PIS-4-TC-04: Search theo mã thuốc (tồn tại)
    @Test(priority = 2)
    public void PIS_4_TC_04_searchByExistingCode() throws InterruptedException {
        String code = "KS-AUG-1G";
        search(code);

        // Chờ cho kết quả search load xong
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(emptyMessageCell),
                ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows)
        ));

        List<WebElement> emptyCells = driver.findElements(emptyMessageCell);
        if (!emptyCells.isEmpty()) {
            Assert.assertEquals(
                    emptyCells.get(0).getText().trim(),
                    "Không có sản phẩm nào",
                    "Khi không tìm thấy dữ liệu phải hiển thị đúng thông báo 'Không có sản phẩm nào'"
            );
            return;
        }

        List<WebElement> rows = driver.findElements(tableRows);
        Assert.assertTrue(rows.size() > 0, "Phải có kết quả cho mã: " + code);

        String target = code.toLowerCase();
        for (WebElement r : rows) {
            String actual = r.findElement(codeCell).getText().trim().toLowerCase();
            Assert.assertTrue(actual.contains(target),
                    "Mỗi dòng phải chứa mã '" + code + "' (bất kể hoa/thường). Thấy: " + actual);
        }
    }

    // PIS-4-TC-05: Search không phân biệt hoa/thường
    @Test(priority = 3)
    public void PIS_4_TC_05_searchCaseInsensitive() throws InterruptedException {
        String inputLower = "ks-aug-1g";
        search(inputLower);
        
        // Chờ bảng dữ liệu load, tối thiểu 1 dòng
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(emptyMessageCell),
            ExpectedConditions.numberOfElementsToBeMoreThan(tableRows, 0)
        ));
        
        List<WebElement> emptyCells = driver.findElements(emptyMessageCell);
        if (!emptyCells.isEmpty()) {
            Assert.fail("Search không phân biệt hoa/thường: Phải có kết quả.");
        }

        List<WebElement> rows = driver.findElements(tableRows);
        Assert.assertTrue(rows.size() > 0, "Search không phân biệt hoa/thường: phải có kết quả");

        for (WebElement r : rows) {
            String actual = r.findElement(codeCell).getText().trim().toLowerCase();
            Assert.assertTrue(actual.contains(inputLower),
                    "Mỗi dòng phải match từ khóa (ignore-case). Thấy: " + actual);
        }
    }

    // PIS-4-TC-06: Search không có kết quả
    @Test(priority = 4)
    public void PIS_4_TC_06_searchNoResult() throws InterruptedException {
        String notExist = "MED-NOT-EXIST-XYZ-12345";
        search(notExist);
        
        WebElement emptyMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(), 'Không có sản phẩm nào')]")
        ));
        
        Assert.assertTrue(emptyMessage.isDisplayed(), 
            "Phải hiển thị thông báo 'Không có sản phẩm nào' khi search không có kết quả");
    }

    // ========== NEW TEST CASES - FILTERS ==========
    
    // PIS-4-TC-07: Lọc theo Danh mục thuốc
    @Test(priority = 7)
    public void PIS_4_TC_07_filterByCategory() throws InterruptedException {
        // Click dropdown danh mục
        WebElement categoryBtn = wait.until(ExpectedConditions.elementToBeClickable(categoryDropdown));
        categoryBtn.click();
        
        // Chọn một danh mục (ví dụ: option đầu tiên không phải "Tất cả")
        WebElement categoryOption = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//div[@role='option' and not(contains(text(),'Tất cả'))][1]")
        ));
        String selectedCategory = categoryOption.getText().trim();
        categoryOption.click();
        
        // Chờ kết quả load sau khi áp dụng filter
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(emptyMessageCell),
            ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows)
        ));
        
        // Kiểm tra nếu có kết quả
        List<WebElement> emptyCells = driver.findElements(emptyMessageCell);
        if (emptyCells.isEmpty()) {
            List<WebElement> rows = driver.findElements(tableRows);
            Assert.assertTrue(rows.size() > 0, "Phải có kết quả khi lọc theo danh mục");
            
            // Kiểm tra tất cả dòng đều thuộc danh mục đã chọn
            for (WebElement row : rows) {
                String category = row.findElement(categoryCell).getText().trim();
                Assert.assertEquals(category, selectedCategory, 
                    "Danh mục của dòng phải khớp với bộ lọc đã chọn");
            }
        } else {
            System.out.println("Không có sản phẩm nào thuộc danh mục đã chọn: " + selectedCategory);
        }
    }
    
    // PIS-4-TC-08: Lọc theo Nhà cung cấp
    @Test(priority = 8)
    public void PIS_4_TC_08_filterBySupplier() throws InterruptedException {
        String targetSupplier = "Công ty Dược phẩm LA ĐẠI LÔC";
        
        // Click dropdown nhà cung cấp
        WebElement supplierBtn = wait.until(ExpectedConditions.elementToBeClickable(supplierDropdown));
        supplierBtn.click();
        
        // Đợi danh sách options xuất hiện
        By supplierOptionLocator = By.xpath("//div[@role='option'][normalize-space(text())='" + targetSupplier + "']");
        
        WebElement supplierOption = null;
        try {
            supplierOption = wait.until(ExpectedConditions.elementToBeClickable(supplierOptionLocator));
        } catch (TimeoutException e) {
            Assert.fail("Không tìm thấy nhà cung cấp: " + targetSupplier);
        }
        
        supplierOption.click();
        
        // Chờ kết quả load sau khi áp dụng filter
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(emptyMessageCell),
            ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows)
        ));
        
        // Kiểm tra kết quả
        List<WebElement> emptyCells = driver.findElements(emptyMessageCell);
        if (emptyCells.isEmpty()) {
            List<WebElement> rows = driver.findElements(tableRows);
            Assert.assertTrue(rows.size() > 0, 
                "Phải có kết quả khi lọc theo nhà cung cấp: " + targetSupplier);
            
            // Kiểm tra tất cả dòng đều thuộc nhà cung cấp đã chọn
            for (WebElement row : rows) {
                String supplier = row.findElement(supplierCell).getText().trim();
                Assert.assertEquals(supplier, targetSupplier, 
                    "Nhà cung cấp của dòng phải là: " + targetSupplier + ", nhưng thấy: " + supplier);
            }
        } else {
            System.out.println("Không có sản phẩm nào từ nhà cung cấp: " + targetSupplier);
        }
    }
    
    // PIS-4-TC-09: Lọc kết hợp Danh mục + Nhà cung cấp
    @Test(priority = 9)
    public void PIS_4_TC_09_filterByCategoryAndSupplier() throws InterruptedException {
        String targetCategory = "Thuốc hô hấp & Dị ứng";
        String targetSupplier = "Công ty Dược phẩm LA ĐẠI LÔC";
        
        // 1. Chọn Danh mục
        WebElement categoryBtn = wait.until(ExpectedConditions.elementToBeClickable(categoryDropdown));
        categoryBtn.click();
        
        By categoryOptLocator = By.xpath("//div[@role='option'][normalize-space(text())='" + targetCategory + "']");
        WebElement categoryOption = null;
        try {
            categoryOption = wait.until(ExpectedConditions.elementToBeClickable(categoryOptLocator));
        } catch (TimeoutException e) {
            Assert.fail("Không tìm thấy danh mục: " + targetCategory);
        }
        categoryOption.click();
        
        // Chờ cho bộ lọc Danh mục áp dụng xong
        Thread.sleep(500); 
        
        // 2. Chọn Nhà cung cấp
        WebElement supplierBtn = wait.until(ExpectedConditions.elementToBeClickable(supplierDropdown));
        supplierBtn.click();
        
        By supplierOptLocator = By.xpath("//div[@role='option'][normalize-space(text())='" + targetSupplier + "']");
        WebElement supplierOption = null;
        try {
            supplierOption = wait.until(ExpectedConditions.elementToBeClickable(supplierOptLocator));
        } catch (TimeoutException e) {
            Assert.fail("Không tìm thấy nhà cung cấp: " + targetSupplier);
        }
        supplierOption.click();
        
        // Chờ kết quả load sau khi áp dụng filter thứ hai
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(emptyMessageCell),
            ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows)
        ));
        
        // Kiểm tra kết quả
        List<WebElement> emptyCells = driver.findElements(emptyMessageCell);
        if (emptyCells.isEmpty()) {
            List<WebElement> rows = driver.findElements(tableRows);
            Assert.assertTrue(rows.size() > 0, 
                "Phải có kết quả khi lọc theo cả danh mục và nhà cung cấp");
            
            for (WebElement row : rows) {
                String category = row.findElement(categoryCell).getText().trim();
                String supplier = row.findElement(supplierCell).getText().trim();
                
                Assert.assertEquals(category, targetCategory, 
                    "Danh mục phải là: " + targetCategory + ", nhưng thấy: " + category);
                Assert.assertEquals(supplier, targetSupplier, 
                    "Nhà cung cấp phải là: " + targetSupplier + ", nhưng thấy: " + supplier);
            }
        } else {
            System.out.println("Không có sản phẩm nào khớp với cả 2 bộ lọc");
        }
    }
    
    // PIS-4-TC-10: Áp dụng lọc + search
    @Test(priority = 10)
    public void PIS_4_TC_10_filterWithSearch() throws InterruptedException {
        String targetSupplier = "Công ty Dược phẩm LA ĐẠI LÔC";
        String keyword = "sku";
        
        // 1. Chọn Nhà cung cấp
        WebElement supplierBtn = wait.until(ExpectedConditions.elementToBeClickable(supplierDropdown));
        supplierBtn.click();
        
        By supplierOptLocator = By.xpath("//div[@role='option'][normalize-space(text())='" + targetSupplier + "']");
        WebElement supplierOption = null;
        try {
            supplierOption = wait.until(ExpectedConditions.elementToBeClickable(supplierOptLocator));
        } catch (TimeoutException e) {
            Assert.fail("Không tìm thấy nhà cung cấp: " + targetSupplier);
        }
        supplierOption.click();
        
        // Chờ cho bộ lọc Nhà cung cấp áp dụng xong
        Thread.sleep(500);
        
        // 2. Search keyword "sku"
        search(keyword);
        
        // Chờ kết quả load
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(emptyMessageCell),
            ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows)
        ));
        
        // Kiểm tra kết quả
        List<WebElement> emptyCells = driver.findElements(emptyMessageCell);
        if (emptyCells.isEmpty()) {
            List<WebElement> rows = driver.findElements(tableRows);
            Assert.assertTrue(rows.size() > 0, 
                "Phải có kết quả khi lọc theo nhà cung cấp và search");
            
            for (WebElement row : rows) {
                // Kiểm tra cả mã thuốc (codeCell) và tên thuốc (nameCell) chứa keyword
                String code = row.findElement(codeCell).getText().trim().toLowerCase();
                String name = row.findElement(nameCell).getText().trim().toLowerCase();
                String supplier = row.findElement(supplierCell).getText().trim();
                
                Assert.assertTrue(code.contains(keyword.toLowerCase()) || name.contains(keyword.toLowerCase()), 
                    "Mã thuốc/Tên thuốc phải chứa keyword: " + keyword + ", Code: " + code + ", Name: " + name);
                Assert.assertEquals(supplier, targetSupplier, 
                    "Nhà cung cấp phải là: " + targetSupplier + ", nhưng thấy: " + supplier);
            }
        } else {
            System.out.println("Không có sản phẩm nào khớp với bộ lọc và keyword search");
        }
    }
    
    // PIS-4-TC-11: Nút 'Xóa tất cả'
    @Test(priority = 11)
    public void PIS_4_TC_11_clearAllFilters() throws InterruptedException {
        String targetSupplier = "Công ty Dược phẩm LA ĐẠI LÔC";
        
        // 1. Áp dụng filter (Nhà cung cấp)
        WebElement supplierBtn = wait.until(ExpectedConditions.elementToBeClickable(supplierDropdown));
        supplierBtn.click();
        
        By supplierOptLocator = By.xpath("//div[@role='option'][normalize-space(text())='" + targetSupplier + "']");
        WebElement supplierOption = null;
        try {
            supplierOption = wait.until(ExpectedConditions.elementToBeClickable(supplierOptLocator));
        } catch (TimeoutException e) {
            Assert.fail("Không tìm thấy nhà cung cấp: " + targetSupplier);
        }
        supplierOption.click();
        
        // Chờ dữ liệu load sau khi filter
        Thread.sleep(1000);
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));
        
        // Đếm số kết quả sau khi filter
        int filteredCount = driver.findElements(tableRows).size();
        
        // Kiểm tra dropdown hiển thị nhà cung cấp đã chọn
        WebElement supplierBtnAfterFilter = driver.findElement(supplierDropdown);
        String displayedSupplier = supplierBtnAfterFilter.getText().trim();
        Assert.assertEquals(displayedSupplier, targetSupplier, 
            "Dropdown phải hiển thị nhà cung cấp đã chọn");
        
        // 2. Click "Xóa tất cả"
        WebElement clearAll = wait.until(ExpectedConditions.elementToBeClickable(clearAllButton));
        clearAll.click();
        
        // Chờ dữ liệu load lại sau khi clear filter
        Thread.sleep(1000);
        
        // 3. Kiểm tra dropdown đã reset về "Tất cả"
        WebElement supplierBtnAfterClear = driver.findElement(supplierDropdown);
        String displayedAfterClear = supplierBtnAfterClear.getText().trim();
        Assert.assertEquals(displayedAfterClear, "Tất cả", 
            "Sau khi xóa bộ lọc, dropdown phải hiển thị 'Tất cả'");
        
        // Đếm số kết quả sau khi clear
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));
        int allCount = driver.findElements(tableRows).size();
        
        // Sau khi clear phải có nhiều kết quả hơn hoặc bằng
        Assert.assertTrue(allCount >= filteredCount, 
            "Sau khi xóa bộ lọc, số kết quả phải tăng lên hoặc giữ nguyên.");
    }

    // ========== NEW TEST CASES - PAGINATION ==========
    
    // PIS-4-TC-12: Thay đổi số dòng/trang sang 50
    @Test(priority = 12)
    public void PIS_4_TC_12_changeRowsPerPage50() {
        // Kiểm tra giá trị ban đầu
        WebElement currentValue = wait.until(ExpectedConditions.visibilityOfElementLocated(rowsPerPageValue));
        Assert.assertEquals(currentValue.getText().trim(), "25", 
            "Giá trị mặc định phải là 25");
        
        // Đổi sang 50
        changeRowsPerPage("50");
        
        // Kiểm tra span đã đổi sang 50
        WebElement newValue = driver.findElement(rowsPerPageValue);
        Assert.assertEquals(newValue.getText().trim(), "50", 
            "Giá trị phải đổi thành 50");
        
        // Kiểm tra số dòng hiển thị
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));
        List<WebElement> rows = driver.findElements(tableRows);
        Assert.assertTrue(rows.size() <= 50, 
            "Số dòng hiển thị phải <= 50, nhưng thấy: " + rows.size());
    }

    // PIS-4-TC-13: Thay đổi số dòng/trang sang 100
    @Test(priority = 13)
    public void PIS_4_TC_13_changeRowsPerPage100() {
        changeRowsPerPage("100");
        
        // Kiểm tra span đã đổi sang 100
        WebElement newValue = driver.findElement(rowsPerPageValue);
        Assert.assertEquals(newValue.getText().trim(), "100", 
            "Giá trị phải đổi thành 100");
        
        // Kiểm tra số dòng hiển thị
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));
        List<WebElement> rows = driver.findElements(tableRows);
        Assert.assertTrue(rows.size() <= 100, 
            "Số dòng hiển thị phải <= 100, nhưng thấy: " + rows.size());
    }

    // PIS-4-TC-14: Thay đổi số dòng/trang về 25
    @Test(priority = 14)
    public void PIS_4_TC_14_changeRowsPerPageBackTo25() {
        // BƯỚC 1: CHUẨN BỊ (Đổi sang 50 trước để có cái mà quay về)
        changeRowsPerPage("50");
        wait.until(ExpectedConditions.textToBe(rowsPerPageValue, "50")); // Đợi chắc chắn nó đã là 50
        
        // BƯỚC 2: THỰC HIỆN (Đổi về 25)
        changeRowsPerPage("25");
        
        // BƯỚC 3: KIỂM TRA UI (Số trên nút)
        wait.until(ExpectedConditions.textToBe(rowsPerPageValue, "25"));
        
        WebElement newValue = driver.findElement(rowsPerPageValue);
        Assert.assertEquals(newValue.getText().trim(), "25", 
            "Giá trị trên nút phải đổi về 25");
        
        // BƯỚC 4: KIỂM TRA DỮ LIỆU (Số dòng trong bảng)
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));
        List<WebElement> rows = driver.findElements(tableRows);
        Assert.assertTrue(rows.size() <= 25, 
            "Số dòng hiển thị phải <= 25, nhưng thấy: " + rows.size());
    }
    
    // thêm locator
private final By currentRangeSpan = By.xpath("//div[contains(@class,'text-muted-foreground')]/span[1]");

// helper lấy số bắt đầu (1, 26, 51, ...)
private int getCurrentStartIndex() {
    WebElement span = wait.until(ExpectedConditions.visibilityOfElementLocated(currentRangeSpan));
    String range = span.getText().trim(); // vd: "1-25"
    String[] parts = range.split("-");
    return Integer.parseInt(parts[0].trim());
}

// PIS-4-TC-15: Điều hướng trang Sau
@Test(priority = 15)
public void PIS_4_TC_15_navigateNextPage() {
    // set 25 dòng/trang
    changeRowsPerPage("25");
    wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));

    // đảm bảo có >25 sp
    WebElement totalItemsElement = driver.findElement(totalItemsText);
    String totalText = totalItemsElement.getText().trim().replaceAll("[^0-9]", "");
    int total = Integer.parseInt(totalText);
    if (total <= 25) {
        System.out.println("Không đủ dữ liệu để kiểm tra phân trang (<= 25). Bỏ qua TC-15.");
        return;
    }

    int startBefore = getCurrentStartIndex(); // trang 1 => 1

    List<WebElement> beforeRows = driver.findElements(tableRows);
    WebElement nextBtn = wait.until(ExpectedConditions.elementToBeClickable(nextButton));
    nextBtn.click();

    // chờ bảng reload
    if (!beforeRows.isEmpty()) {
        wait.until(ExpectedConditions.stalenessOf(beforeRows.get(0)));
    }
    wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));

    int startAfter = getCurrentStartIndex(); // trang 2 => 26

    Assert.assertNotEquals(startAfter, startBefore,
            "Range bắt đầu phải thay đổi sau khi click Sau.");
    Assert.assertTrue(startAfter > startBefore,
            "Range mới phải lớn hơn range cũ. Trước: " + startBefore + ", sau: " + startAfter);
}

// PIS-4-TC-16: Điều hướng trang Trước
@Test(priority = 16)
public void PIS_4_TC_16_navigatePreviousPage() {
    changeRowsPerPage("25");
    wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));

    WebElement totalItemsElement = driver.findElement(totalItemsText);
    String totalText = totalItemsElement.getText().trim().replaceAll("[^0-9]", "");
    int total = Integer.parseInt(totalText);
    if (total <= 25) {
        System.out.println("Không đủ dữ liệu để kiểm tra điều hướng Trang Trước. Bỏ qua TC-16.");
        return;
    }

    // đang ở trang 1 (1-25)
    int startPage1 = getCurrentStartIndex();
    Assert.assertEquals(startPage1, 1, "Ban đầu phải ở trang 1 (range bắt đầu là 1).");

    // sang trang 2
    List<WebElement> beforeRows = driver.findElements(tableRows);
    WebElement nextBtn = wait.until(ExpectedConditions.elementToBeClickable(nextButton));
    nextBtn.click();

    if (!beforeRows.isEmpty()) {
        wait.until(ExpectedConditions.stalenessOf(beforeRows.get(0)));
    }
    wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));

    int startPage2 = getCurrentStartIndex();
    Assert.assertTrue(startPage2 > startPage1,
            "Sau khi click Sau phải sang range tiếp theo. Trang 2 start: " + startPage2);

    // click Trước về lại trang 1
    List<WebElement> beforeRows2 = driver.findElements(tableRows);
    WebElement prevBtn = wait.until(ExpectedConditions.elementToBeClickable(previousButton));
    prevBtn.click();

    if (!beforeRows2.isEmpty()) {
        wait.until(ExpectedConditions.stalenessOf(beforeRows2.get(0)));
    }
    wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));

    int startBack = getCurrentStartIndex();
    Assert.assertNotEquals(startBack, startPage2, "Range sau khi click Trước phải khác range trang 2.");
    Assert.assertEquals(startBack, 1, "Sau khi click Trước phải quay về range bắt đầu từ 1 (trang 1).");
}
    
    // PIS-4-TC-17: Phân trang kết hợp bộ lọc
@Test(priority = 17)
public void PIS_4_TC_17_paginationWithFilter() throws InterruptedException {
    String targetSupplier = "Công ty Dược phẩm LA ĐẠI LÔC"; 

    changeRowsPerPage("25");

    // Đợi một chút cho UI ổn định sau khi đổi số dòng (quan trọng)
    Thread.sleep(1500); 
    
    // --- BẮT ĐẦU CHỌN NHÀ CUNG CẤP ---
    WebElement supplierBtn = wait.until(ExpectedConditions.elementToBeClickable(supplierDropdown));
    supplierBtn.click();

    // 1. Đợi ít nhất 1 option bất kỳ xuất hiện (đảm bảo dropdown đã mở)
    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@role='option']")));

    // 2. Định nghĩa Locator chính xác
    By supplierOptionLocator = By.xpath("//div[@role='option'][normalize-space(.)='" + targetSupplier + "']");

    WebElement supplierOption = null;
    try {
        // Thử tìm bình thường
        supplierOption = wait.until(ExpectedConditions.presenceOfElementLocated(supplierOptionLocator));
        
        // --- FIX QUAN TRỌNG: Scroll tới element nếu nó bị che khuất ---
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", supplierOption);
        Thread.sleep(500); // Đợi scroll xong

        // Kiểm tra lại xem click được chưa
        wait.until(ExpectedConditions.elementToBeClickable(supplierOption)).click();

    } catch (TimeoutException e) {
        // --- DEBUG LOGIC: Nếu lỗi, in ra 5 option đầu tiên để kiểm tra ---
        System.err.println("❌ Không tìm thấy Supplier: " + targetSupplier);
        System.out.println("⚠️ Các Supplier đang hiện có:");
        List<WebElement> options = driver.findElements(By.xpath("//div[@role='option']"));
        for (int i = 0; i < Math.min(options.size(), 5); i++) {
            System.out.println(" - " + options.get(i).getText());
        }
        Assert.fail("TC-17: Không tìm thấy option '" + targetSupplier + "'. Kiểm tra lại Log phía trên.");
    }
    // --- KẾT THÚC CHỌN NHÀ CUNG CẤP ---


    // --- LOGIC KIỂM TRA PHÂN TRANG (GIỮ NGUYÊN) ---
    Thread.sleep(1000); 
    wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));

    // 1. Trang 1 + filter
    List<WebElement> page1Rows = driver.findElements(tableRows);
    Assert.assertFalse(page1Rows.isEmpty(), "Trang 1: Không có dữ liệu sau khi filter!");
    
    for (WebElement row : page1Rows) {
        String supplier = row.findElement(supplierCell).getText().trim(); 
        Assert.assertEquals(supplier, targetSupplier, "Trang 1: Nhà cung cấp sai lệch!");
    }

    // 2. Check total items
    WebElement totalItemsElement = driver.findElement(totalItemsText);
    String totalText = totalItemsElement.getText().trim().replaceAll("[^0-9]", "");
    int total = Integer.parseInt(totalText);
    
    if (total <= 25) {
        System.out.println("ℹ️ TC-17: Tổng số bản ghi là " + total + " (<= 25). Không đủ để test trang 2. PASS.");
        return;
    }

    // 3. Next page
    int startBefore = getCurrentStartIndex(); 
    List<WebElement> beforeRows = driver.findElements(tableRows);
    
    WebElement nextBtn = wait.until(ExpectedConditions.elementToBeClickable(nextButton));
    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextBtn); // Click bằng JS cho chắc chắn

    if (!beforeRows.isEmpty()) {
        wait.until(ExpectedConditions.stalenessOf(beforeRows.get(0)));
    }
    wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));

    // 4. Verify
    int startAfter = getCurrentStartIndex();
    Assert.assertTrue(startAfter > startBefore, "Phân trang thất bại. Range không đổi.");

    List<WebElement> page2Rows = driver.findElements(tableRows);
    for (WebElement row : page2Rows) {
        String supplier = row.findElement(supplierCell).getText().trim(); 
        Assert.assertEquals(supplier, targetSupplier, "Trang 2: Nhà cung cấp sai lệch!");
    }
}

    // ========== HELPERS ==========
    
    /**
     * Helper function to input a keyword into the search box and press Enter.
     * @param keyword The string to search for.
     */
    private void search(String keyword) throws InterruptedException {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(searchInput));
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(keyword);
        // Sau khi nhập, nhấn Enter để trigger search
        input.sendKeys(Keys.ENTER); 
        // Dùng sleep ngắn để đợi AJAX request được gửi đi
        Thread.sleep(500);
    }

    /**
     * Helper function to change the number of rows displayed per page.
     * @param valueToSelect The number of rows to select (e.g., "25", "50", "100").
     */
    public void changeRowsPerPage(String valueToSelect) {
        // 1. Locator cho nút Dropdown 
        WebElement button = driver.findElement(rowsPerPageButton);
        
        // 2. Click Mở Dropdown
        button.click();

        // 3. Đợi Dropdown mở hẳn ra
        wait.until(ExpectedConditions.attributeToBe(button, "aria-expanded", "true"));
        
        // 4. Locator cho tùy chọn
        By optionLocator = By.xpath("//div[@role='option']//span[text()='" + valueToSelect + "']");
        
        try {
            // 5. Chờ cho Tùy chọn có thể Click được và click
            WebElement option = wait.until(ExpectedConditions.elementToBeClickable(optionLocator));
            option.click();
        } catch (TimeoutException e) {
            System.err.println("Không tìm thấy tùy chọn " + valueToSelect + " sau khi mở dropdown.");
            throw e; // Ném lại lỗi để test case fail
        }

        // 6. Chờ Dropdown đóng lại
        wait.until(ExpectedConditions.attributeToBe(button, "aria-expanded", "false"));
        
        // 7. Chờ cho data load lại sau khi thay đổi số dòng/trang
        wait.until(ExpectedConditions.textToBe(rowsPerPageValue, valueToSelect));
    }
}