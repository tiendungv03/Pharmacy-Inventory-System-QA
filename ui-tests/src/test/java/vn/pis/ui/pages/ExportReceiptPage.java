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

    public ExportReceiptPage(WebDriver d) {
        this.driver = d;
        this.wait = new WebDriverWait(d, Duration.ofSeconds(10));
    }

    // =========================================================
    // 1) COMMON HELPERS
    // =========================================================
    private boolean exists(By by) {
        return driver.findElements(by).size() > 0;
    }

    private WebElement waitVisible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private String safeText(By by) {
        try { return waitVisible(by).getText().trim(); } catch (Exception e) { return ""; }
    }

    // =========================================================
    // 2) LOCATORS - MENU / TITLE
    // =========================================================
    private final By menuExportLink = By.xpath(
            "//a[contains(@href,'/export') and .//span[normalize-space()='Xuất kho']]"
    );
    private final By pageTitle = By.xpath("//h1[normalize-space()='Tạo phiếu xuất kho']");

    // =========================================================
    // 3) LOCATORS - THÔNG TIN CHUNG
    // =========================================================
    private final By departmentButton = By.xpath("//label[contains(.,'Khoa/Phòng nhận')]/following-sibling::button");
    private final By departmentSpan   = By.xpath("//label[contains(.,'Khoa/Phòng nhận')]/following-sibling::button//span");
    private final By exportDateInput  = By.id("exportDate");
    private final By notesArea        = By.id("notes");

    // section titles (UI checks)
    private final By generalInfoTitle = By.xpath("//*[normalize-space()='Thông tin chung']");
    private final By detailTableTitle = By.xpath("//*[normalize-space()='Chi tiết phiếu xuất']");

    // required stars
    private final By departmentLabelHasStar =
            By.xpath("//label[contains(normalize-space(.),'Khoa/Phòng nhận') and contains(normalize-space(.),'*')]");
    private final By drugHeaderHasStar =
            By.xpath("//table//thead//th[contains(normalize-space(.),'Tên thuốc') and contains(normalize-space(.),'*')]");
    private final By qtyHeaderHasStar =
            By.xpath("//table//thead//th[contains(normalize-space(.),'Số lượng') and contains(normalize-space(.),'*')]");

    // =========================================================
    // 4) LOCATORS - CHI TIẾT PHIẾU (TABLE)
    // =========================================================
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

    private By thHeader(String text) {
        return By.xpath("//table//thead//th[normalize-space()='" + text + "']");
    }

    // suggestions
    private final By anySuggestionItem = By.xpath(
            "(//div[@role='option'] | //li[@role='option'] | " +
            "//div[contains(@class,'cursor-pointer')] | //div[contains(@class,'border-b')])"
    );

    // =========================================================
    // 5) LOCATORS - TỔNG KẾT / BUTTONS / WARNING
    // =========================================================
    private final By totalAmountLabel = By.xpath(
            "//span[@class='text-xl font-bold text-medical-blue' or contains(.,'₫')]"
    );

    private final By finishButton = By.xpath(
            "//button[.//span[normalize-space()='Hoàn thành phiếu xuất'] or normalize-space()='Hoàn thành phiếu xuất']"
    );

    private final By refreshButton =
            By.xpath("//button[normalize-space()='Làm mới' or .//span[normalize-space()='Làm mới'] or contains(text(),'Làm mới')]");

    private final By warningBanner = By.xpath("//div[@role='alert']");

    // =========================================================
    // 6) LOCATORS - POPUP (DIALOG)
    // =========================================================
    private final By popupDialog = By.xpath("//div[@role='dialog']");
    private final By popupTitle  = By.xpath("//div[@role='dialog']//h2");
    private final By popupClose  = By.xpath("//div[@role='dialog']//button[normalize-space()='Đóng']");
    private final By popupCreateNew =
            By.xpath("//div[@role='dialog']//button[contains(normalize-space(.),'Tạo') and contains(normalize-space(.),'Mới')]");

    private final By popupFefoTitle =
            By.xpath("//div[@role='dialog']//*[contains(normalize-space(.),'Chi tiết phân bổ lô') or contains(normalize-space(.),'FEFO')]");
    private final By popupFefoRows = By.xpath("//div[@role='dialog']//table//tbody/tr");

    // toast/feedback: bắt tiêu đề dialog
    private final By anyToast = By.xpath("//div[@role='dialog']//h2");
    private final By btnCloseSuccess = By.xpath("//div[@role='dialog']//button[normalize-space()='Đóng']");

    // =========================================================
    // 7) NAV
    // =========================================================
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

    // =========================================================
    // 8) UI CHECK METHODS (dùng cho testcase UI)
    // =========================================================
    public boolean isPageTitleVisible() { return exists(pageTitle); }
    public boolean isGeneralInfoVisible() { return exists(generalInfoTitle); }
    public boolean isDetailTableVisible() { return exists(detailTableTitle); }
    public boolean isHeaderVisible(String headerText) { return exists(thHeader(headerText)); }
    public boolean isDepartmentRequiredStar() { return exists(departmentLabelHasStar); }
    public boolean isDrugHeaderRequiredStar() { return exists(drugHeaderHasStar); }
    public boolean isQtyHeaderRequiredStar() { return exists(qtyHeaderHasStar); }
    public boolean isAddRowVisible() { return exists(addRowButton); }
    public boolean isRefreshVisible() { return exists(refreshButton); }
    public boolean isFinishVisible() { return exists(finishButton); }

    // =========================================================
    // 9) THÔNG TIN CHUNG ACTIONS
    // =========================================================
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

    public WebElement getExportDateElement() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(exportDateInput));
    }

    public void setNotes(String notes) {
        log("Nhập Ghi chú: " + notes);
        WebElement area = wait.until(ExpectedConditions.visibilityOfElementLocated(notesArea));
        area.clear();
        area.sendKeys(notes);
    }

    public void blurToTriggerValidation() {
        try {
            WebElement notes = driver.findElement(notesArea);
            ((JavascriptExecutor) driver).executeScript("arguments[0].focus();", notes);
            notes.click();
        } catch (Exception ignore) {}
    }

    // =========================================================
    // 10) TABLE ACTIONS
    // =========================================================
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

    public void setRowDrugNameAndChooseSuggestion(int rowIndex, String name) {
        log("Dòng " + rowIndex + " - Nhập Tên thuốc & chọn gợi ý: " + name);

        WebElement ip = wait.until(ExpectedConditions.visibilityOfElementLocated(
                rowTextInput(rowIndex, "Nhập sản phẩm...")));

        ip.click();
        ip.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        ip.sendKeys(Keys.DELETE);

        String query = name.substring(0, Math.min(3, name.length()));
        String key = name.trim().split("\\s+")[0];

        ip.sendKeys(query);

        By itemByKey = By.xpath(
                "(//div[@role='option'] | //li[@role='option'] | " +
                        "//div[contains(@class,'cursor-pointer')] | //div[contains(@class,'border-b')])" +
                        "[contains(normalize-space(.),'" + key + "')][1]"
        );

        try {
            WebElement opt = new WebDriverWait(driver, Duration.ofSeconds(6))
                    .until(ExpectedConditions.elementToBeClickable(itemByKey));

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", opt);

            try {
                opt.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opt);
            }

            new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
                String v = ip.getAttribute("value");
                return v != null && !v.isBlank() && v.toLowerCase().contains(key.toLowerCase());
            });

            log("Đã chọn gợi ý (click): " + key);
            return;

        } catch (TimeoutException e) {
            log("Không click được option, fallback ARROW_DOWN + ENTER");
            ip.sendKeys(Keys.ARROW_DOWN);
            ip.sendKeys(Keys.ENTER);

            new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
                String v = ip.getAttribute("value");
                return v != null && !v.isBlank() && v.toLowerCase().contains(key.toLowerCase());
            });

            log("Đã chọn gợi ý (keyboard): " + key);
        }
    }

    public void waitRowAutoFilled(int rowIndex, int timeoutSeconds) {
        By priceCell = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//td[5]");
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
            String t = d.findElement(priceCell).getText().trim();
            return t != null && !t.isEmpty() && !t.equals("-");
        });
    }

    public void waitRowAutoFilledFull(int rowIndex, int timeoutSeconds) {
        By priceCell  = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//td[5]");
        By lotCell    = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//td[6]");
        By expiryCell = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//td[7]");

        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
            try {
                String p = d.findElement(priceCell).getText().trim();
                String l = d.findElement(lotCell).getText().trim();
                String e = d.findElement(expiryCell).getText().trim();
                return !p.isEmpty() && !p.equals("-")
                        && !l.isEmpty() && !l.equals("-")
                        && !e.isEmpty() && !e.equals("-");
            } catch (Exception ex) {
                return false;
            }
        });
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

    // =========================================================
    // 11) TOTAL / REFRESH / FINISH
    // =========================================================
    public String getTotalAmountText() {
        String text = wait.until(ExpectedConditions.visibilityOfElementLocated(totalAmountLabel))
                .getText().trim();
        log("Đọc Tổng thành tiền: " + text);
        return text;
    }

    public void clickRefresh() {
        WebElement btn = driver.findElement(By.xpath("//button[contains(text(),'Làm mới')]"));
        wait.until(ExpectedConditions.elementToBeClickable(btn));
        btn.click();
    }

    public void clickRefreshSafe() {
        log("Nhấn Làm mới");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(refreshButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public void clickFinish() {
        log("Nhấn Hoàn thành phiếu xuất");

        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(finishButton));

        if (!btn.isEnabled() || "true".equalsIgnoreCase(btn.getAttribute("disabled"))) {
            throw new IllegalStateException("Finish button is disabled (cannot click).");
        }

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public boolean isFinishEnabled() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(finishButton));
        boolean disabledAttr = "true".equalsIgnoreCase(btn.getAttribute("disabled"));
        return btn.isEnabled() && !disabledAttr;
    }

    public void waitUntilFinishEnabled(int timeoutSeconds) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
            try {
                WebElement b = d.findElement(finishButton);
                boolean disabledAttr = "true".equalsIgnoreCase(b.getAttribute("disabled"));
                return b.isEnabled() && !disabledAttr;
            } catch (Exception e) {
                return false;
            }
        });
    }

    // =========================================================
    // 12) WARNING BANNER
    // =========================================================
    public boolean isWarningBannerVisible() { return exists(warningBanner); }
    public String getWarningBannerText() { return safeText(warningBanner); }

    // =========================================================
    // 13) POPUP (DIALOG) METHODS
    // =========================================================
    public void waitPopupVisible(int timeoutSeconds) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.visibilityOfElementLocated(popupDialog));
    }

    public String getPopupTitleText() {
        return safeText(popupTitle);
    }

    public String getPopupFieldText(String textLabel) {
        By by = By.xpath("//div[@role='dialog']//*[normalize-space()='" + textLabel + "']/following-sibling::*[1]");
        if (exists(by)) return safeText(by);

        by = By.xpath("//div[@role='dialog']//*[contains(normalize-space(.),'" + textLabel + "')]/ancestor::*[1]//*[self::span or self::p or self::div][last()]");
        return safeText(by);
    }

    public boolean isFefoSectionVisible() { return exists(popupFefoTitle); }
    public int getFefoRowCount() { return driver.findElements(popupFefoRows).size(); }

    public void clickPopupClose() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(popupClose));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(popupDialog));
    }

    public void clickPopupCreateNew() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(popupCreateNew));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(popupDialog));
    }

    // =========================================================
    // 14) FEEDBACK / ALERT
    // =========================================================
    public String readFeedback() {
        try {
            Alert a = new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.alertIsPresent());
            String t = a.getText();
            log("Alert hiển thị: " + t);
            a.accept();
            return t == null ? "" : t.trim();
        } catch (TimeoutException | NoAlertPresentException ignored) {}

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            WebElement toastElement = wait.until(ExpectedConditions.visibilityOfElementLocated(anyToast));
            String t = toastElement.getText();
            log("Toast / feedback hiển thị: " + t);

            try {
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

    public String waitForAlertAndGetText(int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        String text = alert.getText();
        alert.accept();
        return text;
    }

    // =========================================================
    // 15) INPUT ACCESSORS / NATIVE VALIDATION
    // =========================================================
    public WebElement getQuantityInput(int rowIndex) {
        By nums = rowNumberInputs(rowIndex);
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(nums, 0));
        List<WebElement> list = driver.findElements(nums);
        return list.get(0);
    }

    public String getNativeValidationMessage(WebElement element) {
        try {
            String msg = element.getAttribute("validationMessage");
            return msg == null ? "" : msg.trim();
        } catch (Exception e) {
            return "";
        }
    }

    // =========================================================
    // 16) DROPDOWN HELPER
    // =========================================================
    private void selectFromDropdown(By buttonLocator, By spanLocator, String optionText) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(buttonLocator));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        btn.click();

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
            } catch (TimeoutException ignored) {}
        }

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
}
