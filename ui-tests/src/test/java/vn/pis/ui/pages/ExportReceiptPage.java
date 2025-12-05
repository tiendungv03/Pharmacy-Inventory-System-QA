// ui-tests/src/main/java/vn/pis/ui/pages/ExportReceiptPage.java
package vn.pis.ui.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ExportReceiptPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private void log(String msg) {
        System.out.println("[PIS6][PAGE] " + msg);
    }

    // ---------- MENU / TITLE ----------
    private final By menuExportLink = By.xpath(
            "//a[contains(@href,'/export') and .//span[normalize-space()='Xuất kho']]"
    );
    private final By pageTitle = By.xpath("//h1[normalize-space()='Tạo phiếu xuất kho']");

    // ---------- THÔNG TIN CHUNG ----------
    private final By departmentButton = By.xpath("//label[contains(.,'Khoa/Phòng nhận')]/following-sibling::button");
    private final By departmentSpan   = By.xpath("//label[contains(.,'Khoa/Phòng nhận')]/following-sibling::button//span");
    private final By exportDateInput  = By.id("exportDate");
    private final By notesArea        = By.id("notes");

    // ---------- CHI TIẾT PHIẾU XUẤT ----------
    private final By addRowButton = By.xpath("//button[.//span[normalize-space()='Thêm dòng'] or normalize-space()='Thêm dòng']");

    private By rowTextInput(int rowIndex, String placeholder) {
        return By.xpath("(//table//tbody/tr)[" + rowIndex + "]//input[@placeholder=\"" + placeholder + "\"]");
    }

    private By rowNumberInputs(int rowIndex) {
        return By.xpath("(//table//tbody/tr)[" + rowIndex + "]//input[@type='number' and @placeholder='0']");
    }

    private By rowLineAmount(int rowIndex) {
        return By.xpath("(//table//tbody/tr)[" + rowIndex + "]//td[last()-1]//div");
    }

    private By rowDeleteButtonByIndex(int rowIndex) {
        return By.xpath(
                "(//table//tbody//tr[contains(@class,'border-b') and contains(@class,'border-border')])[" + rowIndex + "]" +
                        "//button[contains(@class,'inline-flex') and contains(@class,'items-center') and contains(@class,'gap-2')]"
        );
    }

    // ---------- TỔNG KẾT ----------
    private final By totalAmountLabel = By.xpath(
            "//span[@class='text-xl font-bold text-medical-blue' or contains(.,'₫')]"
    );

    // ---------- NÚT HOÀN THÀNH ----------
    private final By finishButton = By.xpath(
            "//button[.//span[normalize-space()='Hoàn thành phiếu xuất'] or normalize-space()='Hoàn thành phiếu xuất']"
    );

    // ---------- THÔNG BÁO ----------
//    private final By anyToast = By.cssSelector(
//            ".swal2-toast .swal2-title, .swal2-container .swal2-html-container," +
//                    ".toast-success, .alert-success, .alert.alert-success, [role='alert']"
//    );
    
 // Sửa để bắt tiêu đề trong Dialog
    private final By anyToast = By.xpath("//div[@role='dialog']//h2");

    //Nút Đóng khi hoàn thành phiếu nhập (Toast)
    private By btnCloseSuccess = By.xpath("//div[@role='dialog']//button[normalize-space()='Đóng']");
    
    
    public ExportReceiptPage(WebDriver d) {
        this.driver = d;
        this.wait = new WebDriverWait(d, Duration.ofSeconds(10));
    }

    // ========= NAV =========
    public void open() {
        log("Mở menu Xuất kho");
        WebElement link = wait.until(ExpectedConditions.visibilityOfElementLocated(menuExportLink));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", link);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(menuExportLink)).click();
        } catch (ElementClickInterceptedException e) {
            log("Click menu bằng JavaScript vì bị chặn");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        log("Đã mở màn Tạo phiếu xuất kho");
    }

    // ========= THÔNG TIN CHUNG =========
    public void selectDepartment(String departmentName) {
        log("Chọn Khoa/Phòng nhận: " + departmentName);
        selectFromDropdown(departmentButton, departmentSpan, departmentName);
    }

    public void setExportDate(String yyyyMMdd) {
        log("Set Ngày xuất: " + yyyyMMdd);
        WebElement date = wait.until(ExpectedConditions.visibilityOfElementLocated(exportDateInput));
        date.clear();
        date.sendKeys(yyyyMMdd);
    }

    public String getExportDate() {
        String value = wait.until(ExpectedConditions.visibilityOfElementLocated(exportDateInput))
                .getAttribute("value");
        log("Đọc Ngày xuất: " + value);
        return value;
    }
    
 // [THÊM MỚI] Dùng để lấy Element (kiểm tra xem có bị khóa/readonly không) - Test case 3(TC03)
    public WebElement getExportDateElement() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(exportDateInput));
    }

    public void setNotes(String notes) {
        log("Nhập Ghi chú: " + notes);
        WebElement area = wait.until(ExpectedConditions.visibilityOfElementLocated(notesArea));
        area.clear();
        area.sendKeys(notes);
    }

    // ========= DÒNG CHI TIẾT =========
    public void clickAddRow() {
        log("Nhấn Thêm dòng");
        wait.until(ExpectedConditions.elementToBeClickable(addRowButton)).click();
        log("Số dòng sau khi thêm: " + getRowCount());
    }

    public int getRowCount() {
        int count = driver.findElements(By.xpath("//table//tbody/tr")).size();
        log("Số dòng hiện tại trong bảng: " + count);
        return count;
    }

    public void setRowSku(int rowIndex, String sku) {
        log("Dòng " + rowIndex + " - Nhập Mã SKU: " + sku);
        WebElement ip = wait.until(ExpectedConditions.visibilityOfElementLocated(
                rowTextInput(rowIndex, "Mã SKU")));
        ip.clear();
        ip.sendKeys(sku);
    }

    // nhập tên thuốc + chọn từ gợi ý auto-complete
    public void setRowDrugNameAndChooseSuggestion(int rowIndex, String name) {
        log("Dòng " + rowIndex + " - Nhập Tên thuốc & chọn gợi ý: " + name);
        WebElement ip = wait.until(ExpectedConditions.visibilityOfElementLocated(
                rowTextInput(rowIndex, "Nhập sản phẩm...")));
        ip.clear();
        ip.sendKeys(name.substring(0, Math.min(3, name.length()))); // gõ vài ký tự đầu

        // dropdown gợi ý: mỗi item là 1 block, dòng 1 là tên thuốc
        By option = By.xpath(
                "(" +
                        "  //div[contains(@class,'cursor-pointer') or contains(@class,'hover:bg') or contains(@class,'border-b')]" +
                        "     [ .//div[normalize-space()='" + name + "'] ]" +
                        "  | //div[@role='option'][.//div[normalize-space()='" + name + "']]" +
                        "  | //li[@role='option'][.//div[normalize-space()='" + name + "']]" +
                        ")[1]"
        );

        try {
            WebElement opt = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(option));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", opt);
            opt.click();
            log("Đã chọn gợi ý: " + name);
        } catch (TimeoutException e) {
            log("Không tìm thấy option đúng tên, fallback ENTER");
            ip.sendKeys(Keys.ENTER);
        }
    }

    public void setRowQuantity(int rowIndex, String qty) {
        log("Dòng " + rowIndex + " - Nhập Số lượng: " + qty);
        By nums = rowNumberInputs(rowIndex);
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(nums, 0));
        List<WebElement> list = driver.findElements(nums);
        WebElement ip = list.get(0);
        ip.clear();
        ip.sendKeys(qty);
    }

    public void setRowPrice(int rowIndex, String price) {
        log("Dòng " + rowIndex + " - Nhập Đơn giá: " + price);
        By nums = rowNumberInputs(rowIndex);
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(nums, 0));
        List<WebElement> list = driver.findElements(nums);
        WebElement ip = list.size() > 1 ? list.get(1) : list.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", ip);
        ip.clear();
        ip.sendKeys(price);
    }

    public String getRowLineAmountText(int rowIndex) {
        String text = wait.until(ExpectedConditions.visibilityOfElementLocated(rowLineAmount(rowIndex)))
                .getText().trim();
        log("Dòng " + rowIndex + " - Đọc Thành tiền: " + text);
        return text;
    }

    public String getRowPriceText(int rowIndex) {
        By priceCell = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//td[5]");
        String text = wait.until(ExpectedConditions.visibilityOfElementLocated(priceCell))
                .getText().trim();
        log("Dòng " + rowIndex + " - Đơn giá hiển thị: " + text);
        return text;
    }

    public String getRowLotText(int rowIndex) {
        By lotCell = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//td[6]");
        String text = wait.until(ExpectedConditions.visibilityOfElementLocated(lotCell))
                .getText().trim();
        log("Dòng " + rowIndex + " - Số lô: " + text);
        return text;
    }

    public String getRowExpiryText(int rowIndex) {
        By expCell = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//td[7]");
        String text = wait.until(ExpectedConditions.visibilityOfElementLocated(expCell))
                .getText().trim();
        log("Dòng " + rowIndex + " - Hạn sử dụng: " + text);
        return text;
    }

    public void deleteRow(int rowIndex) {
        By rowsLocator = By.xpath("//table//tbody//tr[contains(@class,'border-b') and contains(@class,'border-border')]");
        int before = driver.findElements(rowsLocator).size();
        System.out.println("[PIS6][PAGE] Yêu cầu xoá dòng: " + rowIndex);
        System.out.println("[PIS6][PAGE] Số dòng trước khi xoá: " + before);

        WebElement btn = wait.until(
                ExpectedConditions.presenceOfElementLocated(rowDeleteButtonByIndex(rowIndex))
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
        } catch (Exception e) {
            System.out.println("[PIS6][PAGE] Click thường lỗi (" + e.getClass().getSimpleName() + "), dùng JS click nút xoá");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }

        wait.until(ExpectedConditions.numberOfElementsToBeLessThan(rowsLocator, before));
        int after = driver.findElements(rowsLocator).size();
        System.out.println("[PIS6][PAGE] Số dòng sau khi xoá: " + after);
    }

    // ========= TỔNG + HOÀN THÀNH =========
    public String getTotalAmountText() {
        String text = wait.until(ExpectedConditions.visibilityOfElementLocated(totalAmountLabel))
                .getText().trim();
        log("Đọc Tổng thành tiền: " + text);
        return text;
    }

//    public void clickFinish() {
//        log("Nhấn Hoàn thành phiếu xuất");
//        wait.until(ExpectedConditions.elementToBeClickable(finishButton)).click();
//    }
    
    // ==========NÚT LÀM MỚI============
    public void clickRefresh() {
        // Bạn cần thay đổi XPath bên dưới cho đúng với nút "Làm mới" trên web của bạn
        // Ví dụ: nút có chữ "Làm mới" hoặc icon reset
        
        // Gợi ý XPath 1: Theo text
        WebElement btn = driver.findElement(By.xpath("//button[contains(text(),'Làm mới')]"));
        
        // Gợi ý XPath 2: Nếu chỉ có icon, thường nằm cạnh nút Lưu
        // WebElement btn = driver.findElement(By.cssSelector("button.btn-refresh")); 

        wait.until(ExpectedConditions.elementToBeClickable(btn));
        btn.click();
    }
    
    
 // ========= TỔNG + HOÀN THÀNH =========
    public void clickFinish() {
        log("Nhấn Hoàn thành phiếu xuất");
        // Tìm element
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(finishButton));
        
        // Dùng JavascriptExecutor để click (mạnh hơn click thường)
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", btn);
    }

    // ========= THÔNG BÁO =========
    public String readFeedback() {
        // --- 1) Xử lý Alert ---
        try {
            // Giảm thời gian chờ alert xuống thấp (VD: 1-2s) để đỡ tốn thời gian nếu không có
            Alert a = new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.alertIsPresent());
            String t = a.getText();
            log("Alert hiển thị: " + t);
            a.accept();
            return t == null ? "" : t.trim();
        } catch (TimeoutException | NoAlertPresentException ignored) {
            // Không có alert thì bỏ qua, đi tiếp xuống toast
        }

        // --- 2) Xử lý Toast / Message ---
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            
            // a. Đợi toast hiện và lấy text
            WebElement toastElement = wait.until(ExpectedConditions.visibilityOfElementLocated(anyToast));
            String t = toastElement.getText();
            log("Toast / feedback hiển thị: " + t);

            // b. Xử lý click nút Đóng (Thêm mới)
            try {
                // Chờ nút Đóng có thể click được (tối đa 2s)
                WebElement btnClose = new WebDriverWait(driver, Duration.ofSeconds(2))
                        .until(ExpectedConditions.elementToBeClickable(btnCloseSuccess));
                btnClose.click();
                log("-> Đã click nút [Đóng]");
            } catch (Exception e) {
                log("-> Không click được nút Đóng (có thể do toast tự tắt hoặc không có nút): " + e.getMessage());
            }

            return t == null ? "" : t.trim();

        } catch (TimeoutException e) {
            log("Không tìm thấy bất kỳ toast / feedback nào");
            return "";
        }
    }
    
    
    //tôi thử bổ sung vào để làm tc6
    /**
     * Lấy WebElement của ô Số lượng tại dòng chỉ định.
     * Dùng để thao tác trực tiếp (clear, get attribute) trong Test Case.
     */
    public WebElement getQuantityInput(int rowIndex) {
        By nums = rowNumberInputs(rowIndex);
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(nums, 0));
        List<WebElement> list = driver.findElements(nums);
        // Theo logic của setRowQuantity, phần tử đầu tiên (index 0) là Số lượng
        return list.get(0);
    }

    /**
     * Lấy thông báo lỗi Native (bong bóng vàng của trình duyệt).
     * Ví dụ: "Please fill out this field" hoặc "Vui lòng điền vào trường này".
     */
    public String getNativeValidationMessage(WebElement element) {
        try {
            // ValidationMessage là thuộc tính có sẵn của HTML5 input
            String msg = element.getAttribute("validationMessage");
            return msg == null ? "" : msg.trim();
        } catch (Exception e) {
            return "";
        }
    }
    
 
    

    // ========= DROPDOWN HELPER =========
    private void selectFromDropdown(By buttonLocator, By spanLocator, String optionText) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(buttonLocator));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        btn.click();

        // 1) cố chọn đúng text nếu có
        if (optionText != null && !optionText.isBlank()) {
            By optionByText = By.xpath(
                    "//*[normalize-space()='" + optionText + "'][self::div or self::li or self::button]"
            );
            try {
                WebElement opt = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(optionByText));

                js.executeScript("arguments[0].scrollIntoView({block:'center'});", opt);
                try {
                    opt.click();
                } catch (ElementClickInterceptedException e) {
                    js.executeScript("arguments[0].click();", opt);
                }

                wait.until(ExpectedConditions.textToBePresentInElementLocated(spanLocator, optionText));
                return;
            } catch (TimeoutException ignored) {
            }
        }

        // 2) fallback: chọn option đầu tiên
        By firstOption = By.xpath(
                "(//div[@role='option'] | //div[@data-radix-select-item] | " +
                        "//div[contains(@class,'SelectItem')] | //li[@role='option'] | " +
                        "//button[@role='option'] | //button[contains(@class,'SelectItem')])[1]"
        );
        try {
            WebElement first = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(firstOption));

            js.executeScript("arguments[0].scrollIntoView({block:'center'});", first);
            String text = first.getText().trim();
            try {
                first.click();
            } catch (ElementClickInterceptedException e) {
                js.executeScript("arguments[0].click();", first);
            }

            if (!text.isEmpty()) {
                wait.until(ExpectedConditions.textToBePresentInElementLocated(spanLocator, text));
            }
        } catch (TimeoutException e) {
            btn.sendKeys(Keys.ARROW_DOWN);
            btn.sendKeys(Keys.ENTER);
        }
    }

    public String waitForAlertAndGetText(int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        String text = alert.getText();
        alert.accept();
        return text;
    }
}
