package vn.pis.ui.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.List;

public class ExportPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // =========================
    // 1. LOCATORS
    // =========================
    private final By pageTitle      = By.xpath("//h1[contains(text(),'Tạo phiếu xuất kho')]");
    
    // Inputs
    private final By deptDropdown   = By.xpath("//button[@role='combobox']");
    private final By dateInput      = By.id("exportDate");
    private final By notesInput     = By.id("notes");
    
    // [CẬP NHẬT] Dropdown Option (Thêm locator tìm theo class 'cursor-pointer' dựa trên HTML bạn gửi)
    private final By dropdownOption = By.xpath(
        "//div[@role='option'] | " + 
        "//div[contains(@class,'cursor-pointer')] | " + // <-- Dòng này khớp với HTML của thuốc
        "//li[@role='option']"
    );

    // Table Actions
    private final By btnAddLine     = By.xpath("//button[normalize-space()='Thêm dòng']");
    private final By tableRows      = By.cssSelector("tbody tr");
    private final By btnSave        = By.xpath("//button[normalize-space()='Hoàn thành phiếu xuất']");
    private final By totalAmount    = By.xpath("//*[contains(text(),'Tổng thành tiền')]/following-sibling::*");
    
    // Modal
    private final By successModal   = By.xpath("//div[@role='dialog'][contains(., 'Tạo Phiếu Xuất Kho Thành Công')]");
    private final By btnModalClose  = By.xpath("//div[@role='dialog']//button[normalize-space()='Đóng']");

    // =========================
    // 2. CONSTRUCTOR
    // =========================
    public ExportPage(WebDriver d) {
        this.driver = d;
        this.wait = new WebDriverWait(d, Duration.ofSeconds(15));
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/export");
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(pageTitle));
        } catch (TimeoutException e) {
            driver.navigate().refresh();
        }
    }

    public String getTitle() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(pageTitle)).getText();
    }

    // =========================
    // 3. HEADER ACTIONS
    // =========================
    public void selectDepartment(String deptName) {
        for (int i = 0; i < 3; i++) {
            try {
                WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(deptDropdown));
                btn.click();
                // Chờ options hiện ra (dùng locator mới cập nhật)
                List<WebElement> opts = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(dropdownOption));
                if (!opts.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opts.get(0));
                }
                break; 
            } catch (StaleElementReferenceException e) {
                try { Thread.sleep(500); } catch (Exception ex) {}
            }
        }
    }
    
    public String getSelectedDepartment() {
        return driver.findElement(deptDropdown).getText();
    }

    public void setDate(String date) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(dateInput));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '" + date + "';", el);
        ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('input'));", el);
    }
    
    public String getDateValue() { return driver.findElement(dateInput).getAttribute("value"); }

    public void setNotes(String notes) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(notesInput));
        el.clear();
        el.sendKeys(notes);
    }
    
    public String getNotesValue() { return driver.findElement(notesInput).getAttribute("value"); }

    // =========================
    // 4. TABLE ACTIONS
    // =========================
    public void clickAddLine() {
        wait.until(ExpectedConditions.elementToBeClickable(btnAddLine)).click();
        try { Thread.sleep(200); } catch (Exception e) {}
    }

    public int getRowCount() {
        return driver.findElements(tableRows).size();
    }

    // [QUAN TRỌNG] Hàm nhập thuốc đã cập nhật Locator
    public void fillMedicine(int rowIndex, String name, boolean selectSuggestion) {
        for (int i = 0; i < 3; i++) {
            try {
                WebElement row = driver.findElement(By.xpath("(//tbody/tr)[" + (rowIndex + 1) + "]"));
                WebElement input = row.findElement(By.xpath(".//input[@placeholder='Nhập sản phẩm...']"));
                
                input.click();
                input.clear();
                input.sendKeys(name);
                
                if (selectSuggestion) {
                    // [FIX LOCATOR] Dựa vào HTML bạn gửi: div có class 'absolute' chứa các item 'cursor-pointer'
                    By suggestionItem = By.xpath(
                        "//div[contains(@class, 'absolute')]//div[contains(@class, 'cursor-pointer')][contains(., '" + name + "')]"
                    );

                    try {
                        WebElement itemToClick = new WebDriverWait(driver, Duration.ofSeconds(5))
                                .until(ExpectedConditions.visibilityOfElementLocated(suggestionItem));
                        
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", itemToClick);
                    } catch (TimeoutException e) {
                        throw new RuntimeException("❌ LỖI: Không tìm thấy thuốc '" + name + "' trong danh sách gợi ý (Sai locator hoặc thuốc không có)!");
                    }
                
                    waitForLineCalculation(rowIndex);
                }
                break; 
            } catch (StaleElementReferenceException e) {
                try { Thread.sleep(500); } catch (Exception ex) {}
            }
        }
    }

    public void fillQuantity(int rowIndex, String qty) {
        for (int i = 0; i < 3; i++) {
            try {
                WebElement row = driver.findElement(By.xpath("(//tbody/tr)[" + (rowIndex + 1) + "]"));
                WebElement input = row.findElement(By.cssSelector("input[type='number']"));
                input.clear();
                input.sendKeys(qty);
                input.sendKeys(Keys.TAB); 
                break;
            } catch (StaleElementReferenceException e) {
                try { Thread.sleep(500); } catch (Exception ex) {}
            }
        }
    }

    public void deleteLine(int rowIndex) {
        WebElement row = driver.findElement(By.xpath("(//tbody/tr)[" + (rowIndex + 1) + "]"));
        WebElement btnDel = row.findElement(By.xpath(".//button[last()]")); 
        btnDel.click();
    }

    public String getCellText(int rowIndex, int colIndex) {
        try {
            WebElement row = driver.findElement(By.xpath("(//tbody/tr)[" + (rowIndex + 1) + "]"));
            return row.findElement(By.xpath("./td[" + colIndex + "]")).getText().trim();
        } catch (Exception e) { return ""; }
    }

    public String getGrandTotal() {
        return driver.findElement(totalAmount).getText().trim();
    }

    public void waitForLineCalculation(int rowIndex) {
        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(5));
            w.until(d -> {
                String total = getCellText(rowIndex, 8); 
                return !total.equals("0 ₫") && !total.equals("-") && !total.isEmpty();
            });
        } catch (TimeoutException e) {
            System.out.println("⚠️ Hết giờ chờ tính tiền dòng " + rowIndex);
        }
    }

    // =========================
    // 5. SUBMIT
    // =========================
    public void clickSave() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(btnSave));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public boolean isSuccessModalDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successModal));
            return true;
        } catch (TimeoutException e) { return false; }
    }

    public void closeSuccessModal() {
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnModalClose));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(successModal));
        } catch (Exception e) {}
    }

    public boolean isErrorMessageDisplayed(String content) {
        try {
            By locator = By.xpath("//*[contains(text(),'" + content + "')]");
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) { return false; }
    }
    
    public boolean isSaveButtonEnabled() {
        return driver.findElement(btnSave).isEnabled();
    }
}