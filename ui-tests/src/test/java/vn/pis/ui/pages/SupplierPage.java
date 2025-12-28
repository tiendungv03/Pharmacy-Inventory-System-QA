package vn.pis.ui.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;

import java.io.File;
import java.time.Duration;
import java.util.List;

/**
 * SupplierPage – Page Object cho màn Quản lý nhà cung cấp (FE-UI1)
 * Nhóm theo: Locators → Ctor → Header/UI → Table → Search → Actions → Pagination → Assertions
 */
public class SupplierPage {
    // =========================
    // Locators
    // =========================
    private final WebDriver driver;
    private final WebDriverWait wait;

    // --- MENU / NAVIGATION ---
    private final By menuSuppliers = By.xpath("//span[contains(text(),'Quản lý nhà cung cấp')]");

    // --- HEADER ---
    private final By headerTitle = By.xpath("//h1[contains(text(),'Quản lý nhà cung cấp')]");
    private final By headerSubtitle = By.xpath("//h1/following-sibling::*[contains(text(),'Quản lý thông tin')]");

    // --- TOOLBAR ---
    private final By btnAddSupplier = By.xpath("//button[contains(text(),'Thêm nhà cung cấp')]");
    private final By searchInput = By.xpath("//input[@placeholder='Tìm kiếm theo tên' or contains(@placeholder,'Tìm kiếm')]");
    private final By comboboxFilter = By.xpath("//select | //div[@role='combobox'] | //*[contains(@class,'select') or contains(@class,'combobox')]");

    // --- TABLE ---
    private final By table = By.xpath("//table[contains(@class,'w-full')]");
    private final By tableHeader = By.xpath("//thead/tr/th");
    private final By tableRows = By.xpath("//tbody/tr");

    // --- ACTION MENU ---
    private final By actionMenuButton = By.xpath("//button[@aria-haspopup='menu']");
    private final By actionMenuPopover = By.xpath("//*[@data-state='open' or contains(@class,'popover')]");

    // --- PAGINATION ---
    private final By paginationLabel = By.xpath("//span[contains(text(),'Trang')]");
    private final By btnPrevPage = By.xpath("//button[contains(@aria-label,'Trước') or contains(@aria-label,'Previous')] | //button//svg[contains(@class,'chevron-left')]/parent::button | //button[@data-testid='btn-prev']");
    private final By btnNextPage = By.xpath("//button[contains(@aria-label,'Sau') or contains(@aria-label,'Next')] | //button//svg[contains(@class,'chevron-right')]/parent::button | //button[@data-testid='btn-next']");

    // =========================
    // Constructor
    // =========================
    public SupplierPage(WebDriver d) {
        this.driver = d;
        this.wait = new WebDriverWait(d, Duration.ofSeconds(10));
    }

    // =========================
    // Navigation
    // =========================
    public void open() {
        try {
            WebElement menuElement = wait.until(ExpectedConditions.elementToBeClickable(menuSuppliers));
            // Scroll vào view trước khi click
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", menuElement);
            Thread.sleep(300);
            menuElement.click();
        } catch (Exception e) {
            // Fallback: dùng JavaScript click nếu click thường bị chặn
            try {
                WebElement menuElement = driver.findElement(menuSuppliers);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuElement);
            } catch (Exception ex) {
                throw new RuntimeException("Không thể mở menu Quản lý nhà cung cấp", ex);
            }
        }
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(headerTitle));
        wait.until(ExpectedConditions.presenceOfElementLocated(table));
    }

    // =========================
    // Header / UI Elements (FE-UI1-A01 to A10)
    // =========================
    
    /**
     * FE-UI1-A01: Render header
     */
    public boolean headerTitleExists() {
        return driver.findElements(headerTitle).size() > 0;
    }

    public String getHeaderTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(headerTitle)).getText().trim();
    }

    public String getHeaderSubtitle() {
        try {
            return driver.findElement(headerSubtitle).getText().trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    /**
     * FE-UI1-A02: Nút Thêm nhà cung cấp tồn tại & hoverable
     */
    public boolean addButtonExists() {
        return driver.findElements(btnAddSupplier).size() > 0;
    }

    public boolean isAddButtonEnabled() {
        WebElement btn = driver.findElement(btnAddSupplier);
        String disabled = btn.getAttribute("disabled");
        return disabled == null || disabled.isEmpty();
    }

    public void clickAddButton() {
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnAddSupplier));
            // Scroll into view and wait for overlay to settle
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
            Thread.sleep(300);
            
            try {
                btn.click();
            } catch (ElementClickInterceptedException e) {
                // Fallback: use JavaScript click
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot click 'Thêm nhà cung cấp' button: " + e.getMessage(), e);
        }
    }

    public void hoverAddButton() {
        WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(btnAddSupplier));
        new org.openqa.selenium.interactions.Actions(driver).moveToElement(btn).perform();
    }

    /**
     * FE-UI1-A03: Input Tìm kiếm có placeholder
     */
    public String getSearchPlaceholder() {
        return driver.findElement(searchInput).getAttribute("placeholder");
    }

    public void searchByText(String text) {
        WebElement search = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        search.clear();
        search.sendKeys(text);
        search.sendKeys(Keys.ENTER);
        
        // Chờ bảng refresh
        wait.until(ExpectedConditions.presenceOfElementLocated(table));
    }

    /**
     * FE-UI1-A04: Combobox Tất cả mặc định
     */
    public boolean comboboxExists() {
        try {
            // Thử tìm combobox trong vòng 2 giây
            new WebDriverWait(driver, Duration.ofSeconds(2)).until(
                ExpectedConditions.presenceOfElementLocated(comboboxFilter)
            );
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getComboboxText() {
        try {
            WebElement combo = driver.findElement(comboboxFilter);
            return combo.getText().trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    public String getComboboxAriaExpanded() {
        try {
            WebElement combo = driver.findElement(comboboxFilter);
            return combo.getAttribute("aria-expanded");
        } catch (NoSuchElementException e) {
            return "not-found";
        }
    }

    /**
     * FE-UI1-A05: Header bảng đúng 7 cột
     */
    public int getHeaderColumnCount() {
        List<WebElement> cols = driver.findElements(tableHeader);
        return cols.size();
    }

    public String getHeaderColumnText(int index) {
        // index từ 0 đến 6 (A05 yêu cầu 7 cột)
        By col = By.xpath("(//thead/tr/th)[" + (index + 1) + "]");
        return driver.findElement(col).getText().trim();
    }

    public List<String> getAllHeaderColumnTexts() {
        List<WebElement> cols = driver.findElements(tableHeader);
        List<String> texts = new java.util.ArrayList<>();
        for (WebElement col : cols) {
            texts.add(col.getText().trim());
        }
        return texts;
    }

    /**
     * FE-UI1-A06: Dòng SUP0005 hiển thị đủ thông tin
     */
    public boolean supplierRowExists(String code) {
        List<WebElement> rows = driver.findElements(
            By.xpath("//tbody/tr/td[contains(text(),'" + code + "')]/parent::tr")
        );
        return rows.size() > 0;
    }

    public WebElement getSupplierRow(String code) {
        return driver.findElement(
            By.xpath("//tbody/tr/td[contains(text(),'" + code + "')]/parent::tr")
        );
    }

    public String getSupplierRowText(String code) {
        WebElement row = getSupplierRow(code);
        return row.getText();
    }

    public boolean rowContainsText(String code, String... keywords) {
        String rowText = getSupplierRowText(code).toLowerCase();
        for (String kw : keywords) {
            if (!rowText.contains(kw.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    /**
     * FE-UI1-A07: Icon trong cột Liên hệ
     */
    public boolean phoneIconExists(String code) {
        WebElement row = getSupplierRow(code);
        List<WebElement> icons = row.findElements(
            By.xpath(".//svg[contains(@class,'lucide-phone')]")
        );
        return icons.size() > 0;
    }

    public boolean emailIconExists(String code) {
        WebElement row = getSupplierRow(code);
        List<WebElement> icons = row.findElements(
            By.xpath(".//svg[contains(@class,'lucide-mail')]")
        );
        return icons.size() > 0;
    }

    /**
     * FE-UI1-A08: Badge lớp đúng với "Đang hợp tác"
     */
    public boolean hasActiveBadge(String code) {
        WebElement row = getSupplierRow(code);
        List<WebElement> badges = row.findElements(
            By.xpath(".//span[contains(@class,'bg-calm-green') and contains(text(),'Đang hợp tác')]")
        );
        return badges.size() > 0;
    }

    public boolean hasActiveBadgeClass(String code) {
        WebElement row = getSupplierRow(code);
        try {
            WebElement badge = row.findElement(
                By.xpath(".//span[contains(text(),'Đang hợp tác')]")
            );
            String classList = badge.getAttribute("class");
            return classList != null && classList.contains("bg-calm-green");
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * FE-UI1-A09: Badge "Ngừng hợp tác"
     */
    public boolean hasInactiveBadge(String code) {
        WebElement row = getSupplierRow(code);
        List<WebElement> badges = row.findElements(
            By.xpath(".//span[contains(text(),'Ngừng hợp tác')]")
        );
        return badges.size() > 0;
    }

    public boolean hasInactiveBadgeClass(String code) {
        WebElement row = getSupplierRow(code);
        try {
            WebElement badge = row.findElement(
                By.xpath(".//span[contains(text(),'Ngừng hợp tác')]")
            );
            String classList = badge.getAttribute("class");
            return classList != null && classList.contains("bg-secondary");
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * FE-UI1-A10: Nút ⋮ có thể bấm
     */
    public void clickActionMenu() {
        List<WebElement> menus = driver.findElements(actionMenuButton);
        if (menus.size() > 0) {
            WebElement btn = menus.get(0);
            // Scroll into view để tránh bị overlay che
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            
            try {
                wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
            } catch (ElementClickInterceptedException e) {
                // Nếu bị overlay che, dùng JS click fallback
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            }
        }
    }

    public void clickActionMenuForRow(String code) {
        WebElement row = getSupplierRow(code);
        WebElement menuBtn = row.findElement(By.xpath(".//button[@aria-haspopup='menu']"));
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();
    }

    public boolean isActionMenuOpen() {
        List<WebElement> popovers = driver.findElements(actionMenuPopover);
        return popovers.size() > 0;
    }

    // =========================
    // Search (FE-UI1-A11, A12)
    // =========================

    /**
     * FE-UI1-A11: Tìm kiếm lọc theo mã
     */
    public int getVisibleRowCount() {
        return driver.findElements(tableRows).size();
    }

    public void searchBySupplierCode(String code) {
        searchByText(code);
    }

    /**
     * FE-UI1-A12: Tìm kiếm theo tên không phân biệt hoa thường
     */
    public void searchBySupplierName(String name) {
        searchByText(name);
    }

    public boolean searchResultContains(String text) {
        List<WebElement> rows = driver.findElements(tableRows);
        if (rows.isEmpty()) return false;
        
        String tableText = "";
        for (WebElement row : rows) {
            tableText += row.getText().toLowerCase();
        }
        return tableText.contains(text.toLowerCase());
    }

    // =========================
    // Pagination (FE-UI1-A13)
    // =========================

    /**
     * FE-UI1-A13: Pagination label & nút
     */
    public String getPaginationLabel() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(paginationLabel))
                    .getText().trim();
        } catch (TimeoutException e) {
            return "";
        }
    }

    public boolean isPrevButtonDisabled() {
        try {
            WebElement btn = driver.findElement(btnPrevPage);
            String disabled = btn.getAttribute("disabled");
            return disabled != null && !disabled.isEmpty();
        } catch (NoSuchElementException e) {
            return true; // Assume disabled if not found
        }
    }

    public boolean isNextButtonDisabled() {
        try {
            WebElement btn = driver.findElement(btnNextPage);
            String disabled = btn.getAttribute("disabled");
            return disabled != null && !disabled.isEmpty();
        } catch (NoSuchElementException e) {
            return true; // Assume disabled if not found
        }
    }

    public void clickPrevPage() {
        wait.until(ExpectedConditions.elementToBeClickable(btnPrevPage)).click();
    }

    public void clickNextPage() {
        wait.until(ExpectedConditions.elementToBeClickable(btnNextPage)).click();
    }

    // =========================
    // Accessibility (FE-UI1-A14)
    // =========================

    /**
     * FE-UI1-A14: Accessibility cơ bản
     */
    public String getHeaderRole() {
        return driver.findElement(headerTitle).getAttribute("role");
    }

    public String getComboboxRole() {
        try {
            WebElement combo = driver.findElement(comboboxFilter);
            String role = combo.getAttribute("role");
            return role != null ? role : "not-found";
        } catch (NoSuchElementException e) {
            return "not-found";
        }
    }

    // =========================
    // Visual (FE-UI1-A15)
    // =========================

    /**
     * FE-UI1-A15: Snapshot hàng đầu tiên (visual)
     */
    public void takeScreenshot(String name) {
        TakesScreenshot ts = (TakesScreenshot) driver;
        File screenshot = ts.getScreenshotAs(OutputType.FILE);
        try {
            org.apache.commons.io.FileUtils.copyFile(
                screenshot,
                new java.io.File("screenshots/" + name + ".png")
            );
        } catch (Exception e) {
            System.err.println("Screenshot failed: " + e.getMessage());
        }
    }

    public void takeFirstRowScreenshot() {
        try {
            WebElement firstRow = driver.findElement(By.xpath("(//tbody/tr)[1]"));
            File screenshot = ((TakesScreenshot) firstRow).getScreenshotAs(OutputType.FILE);
            org.apache.commons.io.FileUtils.copyFile(
                screenshot,
                new java.io.File("screenshots/supplier-row-1.png")
            );
        } catch (Exception e) {
            System.err.println("Row screenshot failed: " + e.getMessage());
        }
    }

    // =========================
    // Helpers & Assertions
    // =========================

    public void assertHeaderEquals(String expected) {
        Assert.assertEquals(getHeaderTitle(), expected);
    }

    public void assertHeaderColumnCount(int expected) {
        Assert.assertEquals(getHeaderColumnCount(), expected);
    }

    public void assertSupplierCodeExists(String code) {
        Assert.assertTrue(
            supplierRowExists(code),
            "Supplier code '" + code + "' not found in table"
        );
    }

    public void assertSearchResultCount(int expected) {
        Assert.assertEquals(
            getVisibleRowCount(),
            expected,
            "Expected " + expected + " rows but got " + getVisibleRowCount()
        );
    }

    public void waitForTableLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(table));
        wait.until(ExpectedConditions.presenceOfElementLocated(tableRows));
    }
}
