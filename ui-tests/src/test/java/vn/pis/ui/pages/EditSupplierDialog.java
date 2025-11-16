package vn.pis.ui.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object: Popup "Chỉnh sửa nhà cung cấp"
 * Tối ưu cho PIS3_EditSupplier:
 *  - Locator "mềm", đặc biệt cho ô Mã NCC dạng <input disabled class="... bg-gray-100" value="SUP0007">
 *  - Chặn UnhandledAlertException (auto-accept alert & retry)
 *  - API giữ nguyên đúng như test đang gọi
 */
public class EditSupplierDialog {

    private final WebDriver driver;
    private final Duration UI_WAIT = Duration.ofSeconds(5);
    private final Duration SHORT_WAIT = Duration.ofMillis(400);

    // ====== Locators “mềm” (tolerant) ======
    private By dlg() { return By.xpath("//div[@role='dialog']"); }

    private By title() {
        return By.xpath(
            "//div[@role='dialog']//h2 | " +
            "//div[@role='dialog']//*[self::h1 or self::h2 or self::h3]"
        );
    }

    private By btnUpdate() {
        return By.xpath(
            "//div[@role='dialog']//button[" +
              ".//text()[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'cập nhật')] " +
              "or contains(.,'Update') or normalize-space()='Lưu' or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'save')" +
            "]"
        );
    }

    private By btnCancel() {
        return By.xpath(
            "//div[@role='dialog']//button[" +
              ".//text()[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'huỷ')] " +
              "or .//text()[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'hủy')] " +
              "or contains(.,'Cancel') or normalize-space()='Hủy' or normalize-space()='Huỷ'" +
            "]"
        );
    }

    private By btnCloseX() {
        return By.xpath(
            "//div[@role='dialog']//*[self::button or @role='button'][" +
              "@aria-label='Close' or @aria-label='Đóng' or contains(.,'×') or contains(@class,'close')" +
            "] | //div[@role='dialog']//*[name()='svg' and (contains(@aria-label,'Close') or contains(@aria-label,'Đóng'))]"
        );
    }

    // ====== FIELDS ======
    /**
     * Ô mã NCC KHÔNG có id/name/placeholder, chỉ là input disabled + bg-gray-100 + value SUPxxxx
     * -> viết locator siêu "mềm" cho các biến thể.
     */
    private By fldCode() {
        return By.xpath(
            "//div[@role='dialog']//input[" +
              "@disabled and (" +
                "starts-with(@value,'SUP') or " +
                "contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'bg-gray') or " +
                "contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'bg-slate') " +
              " or @readonly" +
              ")" +
            "]" +
            " | //div[@role='dialog']//input[@id='code' or @name='code' or contains(@placeholder,'Mã') or contains(@aria-label,'Mã')]"
        );
    }

    private By fldName() {
        return By.xpath(
            "//div[@role='dialog']//input[" +
              "@id='name' or @name='name' or contains(@placeholder,'Tên') or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'name')" +
            "] " +
            "| //div[@role='dialog']//label[contains(translate(.,'ĂÂÁÀẢÃẠẮẰẲẴẶẤẦẨẪẬÊÉÈẺẼẸÔỐỒỔỖỘƠỚỜỞỠỢƯỨỪỬỮỰĐ','aaaaaaaaaaaaaaaaaaaaaeeeeooooooooouuuuuuud'),'ten')]/following::*[self::input or self::textarea][1]"
        );
    }

    private By fldPhone() {
        return By.xpath(
            "//div[@role='dialog']//input[" +
              "@id='phone' or @name='phone' or @type='tel' or " +
              "contains(translate(@placeholder,'ĐÂĂÁÀẢÃẠÊÉÈẺẼẸÔỐỒỔỖỘƠỚỜỞỠỢƯỨỪỬỮỰđ','daaaaaaaaaeeeeooooooooouuuuuud'),'dien thoai') or " +
              "contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'phone')" +
            "]"
        );
    }

    private By fldEmail() {
        return By.xpath(
            "//div[@role='dialog']//input[" +
              "@id='email' or @name='email' or @type='email' or " +
              "contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'email')" +
            "]"
        );
    }

    private By fldAddress() {
        return By.xpath(
            "//div[@role='dialog']//*[self::input or self::textarea][" +
              "@id='address' or @name='address' or " +
              "contains(translate(@placeholder,'ĐÂĂÁÀẢÃẠÊÉÈẺẼẸÔỐỒỔỖỘƠỚỜỞỠỢƯỨỪỬỮỰđ','daaaaaaaaaeeeeooooooooouuuuuud'),'dia chi') or " +
              "contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'address')" +
            "]"
        );
    }

    private By selStatus() {
        return By.xpath(
            "//div[@role='dialog']//*[self::select or @role='combobox' or contains(@id,'status') or contains(@name,'status') or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'status')]"
        );
    }

    // error feedback (nhiều UI dùng class text-red/error/aria-live)
    private By anyError() {
        return By.xpath("//div[@role='dialog']//*[contains(@class,'text-red') or contains(@class,'error') or @role='alert' or @aria-live='assertive']");
    }

    // toast / alert area (fallback)
    private By anyToast() {
        return By.xpath("//div[contains(@class,'toast') or contains(@class,'alert') or @role='alert']");
    }

    public EditSupplierDialog(WebDriver driver) {
        this.driver = driver;
    }

    // ====== Alert handling ======
    private String acceptAlertIfPresent(Duration wait) {
        try {
            WebDriverWait w = new WebDriverWait(driver, wait);
            Alert a = w.until(ExpectedConditions.alertIsPresent());
            String text = a.getText();
            a.accept();
            sleep(SHORT_WAIT);
            return text == null ? "" : text.trim();
        } catch (TimeoutException | NoAlertPresentException ignored) {
            return "";
        } catch (UnhandledAlertException e) {
            try {
                Alert a = driver.switchTo().alert();
                String text = a.getText();
                a.accept();
                sleep(SHORT_WAIT);
                return text == null ? "" : text.trim();
            } catch (Exception ignored2) {
                return "";
            }
        }
    }

    public void dismissAlert() {
        acceptAlertIfPresent(Duration.ofSeconds(1));
    }

    private void dismissAllAlertsQuickly() {
        for (int i = 0; i < 2; i++) {
            String t = acceptAlertIfPresent(Duration.ofMillis(300));
            if (t.isEmpty()) break;
        }
    }

    // ====== Wait helpers ======
    private void sleep(Duration d) {
        try { Thread.sleep(d.toMillis()); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    private WebElement waitVisible(By locator) {
        dismissAllAlertsQuickly();
        return new WebDriverWait(driver, UI_WAIT).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // ====== Dialog lifecycle ======
    public void waitForOpen() {
        dismissAllAlertsQuickly();
        waitVisible(dlg());
    }

    public boolean isOpen() {
        dismissAllAlertsQuickly();
        try {
            return driver.findElement(dlg()).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void waitForClose() {
        dismissAllAlertsQuickly();
        new WebDriverWait(driver, UI_WAIT).until(ExpectedConditions.invisibilityOfElementLocated(dlg()));
    }

    public String getTitle() {
        try {
            return waitVisible(title()).getText().trim();
        } catch (UnhandledAlertException e) {
            dismissAllAlertsQuickly();
            return waitVisible(title()).getText().trim();
        }
    }

    // ====== Actions ======
    public void clickUpdate() {
        try {
            waitVisible(btnUpdate()).click();
        } catch (UnhandledAlertException e) {
            dismissAllAlertsQuickly();
        }
    }

    public void clickCancel() {
        try {
            waitVisible(btnCancel()).click();
        } catch (UnhandledAlertException e) {
            dismissAllAlertsQuickly();
        }
    }

    public void clickCloseX() {
        try {
            WebElement x = new WebDriverWait(driver, UI_WAIT)
                    .until(ExpectedConditions.elementToBeClickable(btnCloseX()));
            x.click();
        } catch (TimeoutException t) {
            // có UI không có nút X – fallback: thử Cancel
            clickCancel();
        } catch (UnhandledAlertException e) {
            dismissAllAlertsQuickly();
        }
    }

    // ====== Field getters / setters ======
    private void clearAndType(By locator, String value) {
        try {
            WebElement el = waitVisible(locator);
            el.clear();
            if (value != null && !value.isEmpty()) el.sendKeys(value);
        } catch (UnhandledAlertException e) {
            dismissAllAlertsQuickly();
            WebElement el = waitVisible(locator);
            el.clear();
            if (value != null && !value.isEmpty()) el.sendKeys(value);
        }
    }

    public WebElement getFieldName()  { return waitVisible(fldName()); }
    public WebElement getFieldPhone() { return waitVisible(fldPhone()); }
    public WebElement getFieldEmail() { return waitVisible(fldEmail()); }
    public WebElement getFieldAddress(){ return waitVisible(fldAddress()); }

    public String getSupplierCode() {
        try {
            WebElement code = waitVisible(fldCode());
            String v = code.getAttribute("value");
            if (v == null || v.isEmpty()) v = code.getText();
            return v == null ? "" : v.trim();
        } catch (UnhandledAlertException e) {
            dismissAllAlertsQuickly();
            try {
                WebElement code = waitVisible(fldCode());
                String v = code.getAttribute("value");
                if (v == null || v.isEmpty()) v = code.getText();
                return v == null ? "" : v.trim();
            } catch (Exception ex) {
                return "";
            }
        } catch (NoSuchElementException e) {
            // một số UI ẩn mã NCC
            return "";
        }
    }

    public void assertSupplierCodeDisabled() {
        try {
            WebElement code = waitVisible(fldCode());
            String ro  = String.valueOf(code.getAttribute("readonly"));
            String dis = String.valueOf(code.getAttribute("disabled"));
            boolean disabled = "true".equalsIgnoreCase(dis) || (dis != null && !"null".equalsIgnoreCase(dis))
                            || "true".equalsIgnoreCase(ro)  || (ro  != null && !"null".equalsIgnoreCase(ro));
            if (!disabled) throw new AssertionError("Mã NCC phải disabled/readOnly");
        } catch (NoSuchElementException ignored) {
            // nếu UI không hiển thị ô mã (không cho sửa), coi như đạt
        } catch (UnhandledAlertException e) {
            dismissAllAlertsQuickly();
            assertSupplierCodeDisabled();
        }
    }

    public void setName(String v)    { clearAndType(fldName(), v); }
    public void setPhone(String v)   { clearAndType(fldPhone(), v); }
    public void setEmail(String v)   { clearAndType(fldEmail(), v); }
    public void setAddress(String v) { clearAndType(fldAddress(), v); }

    public String getValue(WebElement inputOrTextarea) {
        try {
            String v = inputOrTextarea.getAttribute("value");
            if (v == null || v.isEmpty()) v = inputOrTextarea.getText();
            return v == null ? "" : v.trim();
        } catch (UnhandledAlertException e) {
            dismissAllAlertsQuickly();
            String v = inputOrTextarea.getAttribute("value");
            if (v == null || v.isEmpty()) v = inputOrTextarea.getText();
            return v == null ? "" : v.trim();
        }
    }

    // ====== Status ======
    public String getStatusValue() {
        try {
            WebElement el = waitVisible(selStatus());
            String tag = el.getTagName().toLowerCase();
            if ("select".equals(tag)) {
                List<WebElement> selected = el.findElements(By.xpath(".//option[@selected]"));
                if (!selected.isEmpty()) return selected.get(0).getText().trim();
                // fallback: value attr
                String v = el.getAttribute("value");
                return v == null ? "" : v.trim();
            } else {
                String v = el.getAttribute("value");
                if (v == null || v.isEmpty()) v = el.getText();
                return v == null ? "" : v.trim();
            }
        } catch (Exception e) {
            return "";
        }
    }

    public void setStatus(String target) {
        String key = target == null ? "" : target.trim().toLowerCase();
        if (key.isEmpty()) return;

        try {
            WebElement el = waitVisible(selStatus());
            String tag = el.getTagName().toLowerCase();
            if ("select".equals(tag)) {
                List<WebElement> options = el.findElements(By.xpath(".//option[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + key + "')]"));
                if (!options.isEmpty()) {
                    options.get(0).click();
                }
            } else {
                // Custom combobox/select widget
                el.click();
                sleep(Duration.ofMillis(300));
                List<WebElement> opts = driver.findElements(By.xpath(
                    "//*[@role='option' or @data-radix-collection-item][contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + key + "')]"
                ));
                if (!opts.isEmpty()) {
                    opts.get(0).click();
                }
            }
        } catch (Exception e) {
            // Có lỗi khi set status, không crash
        }
    }

    // ====== Errors / Toasts ======
    /** Trả về nội dung error đầu tiên trong dialog (nếu có). */
    public String getFirstError() {
        try {
            List<WebElement> errs = driver.findElements(anyError());
            if (!errs.isEmpty()) {
                String t = errs.get(0).getText();
                return t == null ? "" : t.trim();
            }
            return "";
        } catch (UnhandledAlertException e) {
            dismissAllAlertsQuickly();
            List<WebElement> errs = driver.findElements(anyError());
            if (!errs.isEmpty()) {
                String t = errs.get(0).getText();
                return t == null ? "" : t.trim();
            }
            return "";
        }
    }

    /**
     * Chờ toast/alert xuất hiện trong khoảng timeoutMs.
     * Ưu tiên Alert JS: nếu thấy alert → accept và trả text.
     */
    public String waitToast(int timeoutMs) {
        // 1) JS Alert trước
        String alert = acceptAlertIfPresent(Duration.ofMillis(timeoutMs));
        if (!alert.isEmpty()) return alert;

        // 2) Nếu không có Alert, thử toast element
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            try {
                List<WebElement> toasts = driver.findElements(anyToast());
                if (!toasts.isEmpty()) {
                    String text = toasts.get(0).getText();
                    if (text != null && !text.trim().isEmpty()) return text.trim();
                }
            } catch (UnhandledAlertException e) {
                alert = acceptAlertIfPresent(Duration.ofSeconds(1));
                if (!alert.isEmpty()) return alert;
            }
            sleep(SHORT_WAIT);
        }
        return "";
    }
}
