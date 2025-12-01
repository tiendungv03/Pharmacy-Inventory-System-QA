//package vn.pis.ui.pages;
//
//import org.openqa.selenium.*;
//import org.openqa.selenium.support.ui.*;
//import java.time.Duration;
//import java.util.List;
//
//public class ExportReceipt {
//    private final WebDriver driver;
//    private final WebDriverWait wait;
//
//    // =========================
//    // 1. LOCATORS
//    // =========================
//    private final By pageTitle      = By.xpath("//h1[contains(text(),'Tạo phiếu xuất kho')]");
//    
//    // Inputs
//    private final By deptDropdown   = By.xpath("//button[@role='combobox']");
//    private final By dateInput      = By.id("exportDate");
//    private final By notesInput     = By.id("notes");
//    private final By dropdownOption = By.xpath("//div[@role='option'] | //div[contains(@class,'select-item')] | //li[@role='option']");
//
//    // Table Actions
//    private final By btnAddLine     = By.xpath("//button[normalize-space()='Thêm dòng']");
//    private final By tableRows      = By.cssSelector("tbody tr");
//    private final By btnSave        = By.xpath("//button[normalize-space()='Hoàn thành phiếu xuất']");
//    private final By totalAmount    = By.xpath("//*[contains(text(),'Tổng thành tiền')]/following-sibling::*");
//    
//    // [NEW] MODAL THÀNH CÔNG (Dựa theo ảnh)
//    private final By successModal   = By.xpath("//div[@role='dialog'][contains(., 'Tạo Phiếu Xuất Kho Thành Công')]");
//    private final By btnModalClose  = By.xpath("//div[@role='dialog']//button[normalize-space()='Đóng']");
//    private final By btnCreateNew = By.xpath("//button[contains(text(),'Tạo Phiếu Mới')]");
//    private final By btnRefresh = By.xpath("//button[contains(text(),'Làm mới')]");
//
//    // =========================	
//    // 2. CONSTRUCTOR & NAV
//    // =========================
//    public ExportReceipt(WebDriver d) {
//        this.driver = d;
//        this.wait = new WebDriverWait(d, Duration.ofSeconds(10));
//    }
//
//    public void open(String baseUrl) {
//        driver.get(baseUrl + "/export");
//        wait.until(ExpectedConditions.presenceOfElementLocated(pageTitle));
//    }
//
//    public String getTitle() {
//        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle)).getText();
//    }
//
//    // =========================
//    // 3. HEADER ACTIONS
//    // =========================
//    public void selectDepartment(String deptName) {
//        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(deptDropdown));
//        btn.click();
//        List<WebElement> opts = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(dropdownOption));
//        if (!opts.isEmpty()) opts.get(0).click();
//    }
//    
//    public String getSelectedDepartment() {
//        return driver.findElement(deptDropdown).getText();
//    }
//
//    
//    
//    public void setDate(String date) {
//        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(dateInput));
//        // Dùng JS để tránh lỗi format ngày khi dùng sendKeys
//        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '" + date + "';", el);
//        ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('input'));", el);
//    }
//    
//    public String getDateValue() { return driver.findElement(dateInput).getAttribute("value"); }
//
//    
//    
//    public void setNotes(String notes) {
//        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(notesInput));
//        el.clear();
//        el.sendKeys(notes);
//    }
//    
//    public String getNotesValue() { return driver.findElement(notesInput).getAttribute("value"); }
//
//    // =========================
//    // 4. TABLE ACTIONS (Bền bỉ - Fix lỗi Stale)
//    // =========================
//    public void clickAddLine() {
//        wait.until(ExpectedConditions.elementToBeClickable(btnAddLine)).click();
//        try { Thread.sleep(300); } catch (Exception e) {} 
//    }
//
//    public int getRowCount() {
//        return driver.findElements(tableRows).size();
//    }
//
//    public void fillMedicine(int rowIndex, String name, boolean selectSuggestion) {
//        // Retry 3 lần nếu gặp StaleElement
//        for (int i = 0; i < 3; i++) {
//            try {
//                // 1. Tìm dòng và ô input
//                WebElement row = driver.findElement(By.xpath("(//tbody/tr)[" + (rowIndex + 1) + "]"));
//                WebElement input = row.findElement(By.xpath(".//input[@placeholder='Nhập sản phẩm...']"));
//                
//                // 2. Thao tác nhập liệu
//                input.click();
//                input.clear();
//                input.sendKeys(name);
//                
//                // 3. Xử lý chọn gợi ý và chờ dữ liệu load
//                if (selectSuggestion) {
//                    try { Thread.sleep(800); } catch (Exception e) {} 
//                    input.sendKeys(Keys.ENTER);
//                
//                    // --- ĐOẠN MỚI THÊM (Đã sửa lại cho đúng cú pháp) ---
//                    // Đợi cho đến khi cột "Đơn vị" (giả sử là cột 3) có dữ liệu khác dấu "-"
//                    WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(5));
//                    w.until(d -> {
//                        // Gọi hàm getCellText để lấy giá trị mới nhất
//                        String unit = getCellText(rowIndex, 3); 
//                        // Điều kiện: Không phải dấu gạch ngang VÀ không được rỗng
//                        return !unit.equals("-") && !unit.isEmpty();
//                    });
//                }
//                
//                // quan trọng: Nếu chạy đến đây mà không lỗi thì thoát vòng lặp ngay
//                break; 
//
//            } catch (StaleElementReferenceException e) {
//                // Nếu lỗi StaleElement thì chờ 0.5s rồi lặp lại (retry)
//                try { Thread.sleep(500); } catch (Exception ex) {}
//            } catch (Exception e) {
//                // Bắt các lỗi khác (ví dụ Timeout khi chờ Đơn vị)
//                System.out.println("Lỗi khi điền thuốc: " + e.getMessage());
//                break; // Gặp lỗi lạ thì dừng luôn
//            }
//        }
//    }
//    
//            
//        
//    
//
//    public void fillQuantity(int rowIndex, String qty) {
//        for (int i = 0; i < 3; i++) {
//            try {
//                WebElement row = driver.findElement(By.xpath("(//tbody/tr)[" + (rowIndex + 1) + "]"));
//                WebElement input = row.findElement(By.cssSelector("input[type='number']"));
//                input.clear();
//                input.sendKeys(qty);
//                break;
//            } catch (StaleElementReferenceException e) {
//                try { Thread.sleep(500); } catch (Exception ex) {}
//            }
//        }
//    }
//
//    public void deleteLine(int rowIndex) {
//        WebElement row = driver.findElement(By.xpath("(//tbody/tr)[" + (rowIndex + 1) + "]"));
//        WebElement btnDel = row.findElement(By.xpath(".//button[last()]")); // Nút xóa thường ở cuối
//        btnDel.click();
//    }
//
//    public String getCellText(int rowIndex, int colIndex) {
//        WebElement row = driver.findElement(By.xpath("(//tbody/tr)[" + (rowIndex + 1) + "]"));
//        return row.findElement(By.xpath("./td[" + colIndex + "]")).getText().trim();
//    }
//
//    public String getGrandTotal() {
//        return driver.findElement(totalAmount).getText().trim();
//    }
//
//    // =========================
//    // 5. SUBMIT & VALIDATION (MODAL)
//    // =========================
//    public void clickSave() {
//        // Dùng JS Click để không bị chặn bởi overlay
//        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(btnSave));
//        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
//    }
//
//    // Kiểm tra Modal thành công hiện ra
//    public boolean isSuccessModalDisplayed() {
//        try {
//            wait.until(ExpectedConditions.visibilityOfElementLocated(successModal));
//            return true;
//        } catch (TimeoutException e) { return false; }
//    }
//
//    // Đóng modal để dọn dẹp
//    public void closeSuccessModal() {
//        try {
//            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnModalClose));
//            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
//            wait.until(ExpectedConditions.invisibilityOfElementLocated(successModal));
//        } catch (Exception e) {
//            System.out.println("⚠️ Không đóng được modal: " + e.getMessage());
//        }
//    }
//
//    public boolean isErrorMessageDisplayed(String content) {
//        try {
//            By locator = By.xpath("//*[contains(text(),'" + content + "')]");
//            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
//            return true;
//        } catch (TimeoutException e) { return false; }
//    }
//    
//    public boolean isSaveButtonEnabled() {
//        return driver.findElement(btnSave).isEnabled();
//    }
//}



//
//package vn.pis.ui.pages;
//
//import org.openqa.selenium.*;
//import org.openqa.selenium.support.ui.*;
//import java.time.Duration;
//import java.util.List;
//
//public class ExportReceipt {
//    private final WebDriver driver;
//    private final WebDriverWait wait;
//
//    // =========================
//    // 1. LOCATORS
//    // =========================
//    private final By pageTitle      = By.xpath("//h1[contains(text(),'Tạo phiếu xuất kho')]");
//    
//    // Inputs
//    private final By deptDropdown   = By.xpath("//button[@role='combobox']");
//    private final By dateInput      = By.id("exportDate");
//    private final By notesInput     = By.id("notes");
//    // Locator tìm option chính xác hơn
//    private final By dropdownOption = By.xpath("//div[@role='option'] | //div[contains(@class,'select-item')] | //li[@role='option']");
//
//    // Table Actions
//    private final By btnAddLine     = By.xpath("//button[normalize-space()='Thêm dòng']");
//    private final By tableRows      = By.cssSelector("tbody tr");
//    private final By btnSave        = By.xpath("//button[normalize-space()='Hoàn thành phiếu xuất']");
//    private final By totalAmount    = By.xpath("//*[contains(text(),'Tổng thành tiền')]/following-sibling::*");
//    
//    // MODAL
//    private final By successModal   = By.xpath("//div[@role='dialog'][contains(., 'Tạo Phiếu Xuất Kho Thành Công')]");
//    private final By btnModalClose  = By.xpath("//div[@role='dialog']//button[normalize-space()='Đóng']");
//
//    // =========================    
//    // 2. CONSTRUCTOR & NAV
//    // =========================
//    public ExportReceipt(WebDriver d) {
//        this.driver = d;
//        // [FIX] Tăng timeout mặc định lên 15s để tránh mạng lag
//        this.wait = new WebDriverWait(d, Duration.ofSeconds(15));
//    }
//
//    public void open(String baseUrl) {
//        driver.get(baseUrl + "/export");
//        wait.until(ExpectedConditions.presenceOfElementLocated(pageTitle));
//    }
//
//    public String getTitle() {
//        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle)).getText();
//    }
//
//    // =========================
//    // 3. HEADER ACTIONS
//    // =========================
//    
//    // [FIX TC02] Thêm cơ chế Retry để trị lỗi StaleElementReferenceException
//    public void selectDepartment(String deptName) {
//        for (int i = 0; i < 3; i++) {
//            try {
//                WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(deptDropdown));
//                btn.click();
//                
//                // Chờ option hiện ra
//                List<WebElement> opts = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(dropdownOption));
//                
//                if (!opts.isEmpty()) {
//                    opts.get(0).click();
//                }
//                break; // Thành công thì thoát
//            } catch (StaleElementReferenceException e) {
//                // Nếu lỗi thì chờ xíu rồi thử lại
//                try { Thread.sleep(500); } catch (Exception ex) {}
//            }
//        }
//    }
//    
//    public String getSelectedDepartment() {
//        return driver.findElement(deptDropdown).getText();
//    }
//
//    public void setDate(String date) {
//        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(dateInput));
//        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '" + date + "';", el);
//        ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('input'));", el);
//    }
//    
//    public String getDateValue() { return driver.findElement(dateInput).getAttribute("value"); }
//
//    public void setNotes(String notes) {
//        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(notesInput));
//        el.clear();
//        el.sendKeys(notes);
//    }
//    
//    public String getNotesValue() { return driver.findElement(notesInput).getAttribute("value"); }
//
//    // =========================
//    // 4. TABLE ACTIONS
//    // =========================
//    public void clickAddLine() {
//        wait.until(ExpectedConditions.elementToBeClickable(btnAddLine)).click();
//        try { Thread.sleep(300); } catch (Exception e) {} 
//    }
//
//    public int getRowCount() {
//        return driver.findElements(tableRows).size();
//    }
//
//    public void fillMedicine(int rowIndex, String name, boolean selectSuggestion) {
//        for (int i = 0; i < 3; i++) {
//            try {
//                WebElement row = driver.findElement(By.xpath("(//tbody/tr)[" + (rowIndex + 1) + "]"));
//                WebElement input = row.findElement(By.xpath(".//input[@placeholder='Nhập sản phẩm...']"));
//                
//                input.click();
//                input.clear();
//                input.sendKeys(name);
//                
//                if (selectSuggestion) {
//                    try { Thread.sleep(1000); } catch (Exception e) {} // Tăng chờ gợi ý
//                    input.sendKeys(Keys.ENTER);
//                
//                    // [FIX TC06] Tăng thời gian chờ load giá lên 15 giây
//                    WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(15));
//                    longWait.until(d -> {
//                        String unit = getCellText(rowIndex, 3); 
//                        return !unit.equals("-") && !unit.isEmpty();
//                    });
//                }
//                break; 
//            } catch (StaleElementReferenceException e) {
//                try { Thread.sleep(500); } catch (Exception ex) {}
//            } catch (Exception e) {
//                System.out.println("⚠️ Lỗi fillMedicine (có thể do mạng chậm): " + e.getMessage());
//                // Không break để nó retry tiếp
//            }
//        }
//    }
//
//    public void fillQuantity(int rowIndex, String qty) {
//        for (int i = 0; i < 3; i++) {
//            try {
//                WebElement row = driver.findElement(By.xpath("(//tbody/tr)[" + (rowIndex + 1) + "]"));
//                WebElement input = row.findElement(By.cssSelector("input[type='number']"));
//                input.clear();
//                input.sendKeys(qty);
//                
//                input.sendKeys(Keys.TAB); 
//                ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", input);
//                ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", input);
//                try { Thread.sleep(500); } catch (Exception ex) {}
//
//                break;
//            } catch (StaleElementReferenceException e) {
//                try { Thread.sleep(500); } catch (Exception ex) {}
//            }
//        }
//    }
//
//    public void deleteLine(int rowIndex) {
//        WebElement row = driver.findElement(By.xpath("(//tbody/tr)[" + (rowIndex + 1) + "]"));
//        WebElement btnDel = row.findElement(By.xpath(".//button[last()]")); 
//        btnDel.click();
//    }
//
//    public String getCellText(int rowIndex, int colIndex) {
//        try {
//            WebElement row = driver.findElement(By.xpath("(//tbody/tr)[" + (rowIndex + 1) + "]"));
//            return row.findElement(By.xpath("./td[" + colIndex + "]")).getText().trim();
//        } catch (Exception e) {
//            return ""; 
//        }
//    }
//
//    public String getGrandTotal() {
//        return driver.findElement(totalAmount).getText().trim();
//    }
//
//    // =========================
//    // 5. SUBMIT & VALIDATION
//    // =========================
//    public void clickSave() {
//        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(btnSave));
//        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
//    }
//
//    public boolean isSuccessModalDisplayed() {
//        try {
//            wait.until(ExpectedConditions.visibilityOfElementLocated(successModal));
//            return true;
//        } catch (TimeoutException e) { return false; }
//    }
//
//    public void closeSuccessModal() {
//        try {
//            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnModalClose));
//            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
//            wait.until(ExpectedConditions.invisibilityOfElementLocated(successModal));
//        } catch (Exception e) {}
//    }
//
//    public boolean isErrorMessageDisplayed(String content) {
//        try {
//            By locator = By.xpath("//*[contains(text(),'" + content + "')]");
//            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
//            return true;
//        } catch (TimeoutException e) { return false; }
//    }
//    
//    public boolean isSaveButtonEnabled() {
//        return driver.findElement(btnSave).isEnabled();
//    }
//}

package vn.pis.ui.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.List;

public class ExportReceipt {
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
    private final By dropdownOption = By.xpath("//div[@role='option'] | //div[contains(@class,'select-item')] | //li[@role='option']");

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
    public ExportReceipt(WebDriver d) {
        this.driver = d;
        this.wait = new WebDriverWait(d, Duration.ofSeconds(10));
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
                List<WebElement> opts = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(dropdownOption));
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

 // =========================================================================
    // HÀM NHẬP THUỐC (CHẠY 1 LẦN DUY NHẤT - KHÔNG RETRY)
    // =========================================================================
    public void fillMedicine(int rowIndex, String name, boolean selectSuggestion) {
        // 1. Tìm ô input (Nếu trang web nháy lúc này -> Lỗi StaleElement -> Test Fail ngay lập tức)
        WebElement row = driver.findElement(By.xpath("(//tbody/tr)[" + (rowIndex + 1) + "]"));
        WebElement input = row.findElement(By.xpath(".//input[@placeholder='Nhập sản phẩm...']"));
        
        // 2. Nhập tên thuốc
        input.click();
        input.clear();
        input.sendKeys(name);
        
        if (selectSuggestion) {
            // Locator tìm dòng gợi ý
            By suggestionItem = By.xpath(
                "//div[@role='option'][contains(., '" + name + "')] | " +
                "//li[contains(@class,'select-item')][contains(., '" + name + "')]"
            );

            try {
                // Chờ tối đa 5 giây cho list gợi ý hiện ra
                WebElement itemToClick = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(suggestionItem));
                
                // Click chọn
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", itemToClick);
                
            } catch (TimeoutException e) {
                // Báo lỗi rõ ràng nếu không thấy gợi ý
                throw new RuntimeException("❌ LỖI: Gõ '" + name + "' nhưng không thấy menu gợi ý (hoặc API lỗi)!");
            }
        
            // 3. Chờ Web load giá tiền/đơn vị
            try {
                WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(3));
                w.until(d -> {
                    String unit = getCellText(rowIndex, 3); 
                    return !unit.equals("-") && !unit.isEmpty();
                });
            } catch (TimeoutException ex) {
                System.out.println("⚠️ Cảnh báo: Giá chưa load kịp sau khi chọn.");
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

    // [QUAN TRỌNG] Hàm chờ tính tiền
    public void waitForLineCalculation(int rowIndex) {
        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(5));
            w.until(d -> {
                String total = getCellText(rowIndex, 8); // Cột 8: Thành tiền
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