package vn.pis.ui.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;

public class ImportReceiptPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // --- LOCATORS ---
    private final By menuImportLink = By.xpath("//a[contains(@href,'/import')]");
    public final By pageTitle = By.xpath("//h1[normalize-space()='Tạo phiếu nhập kho']");
    public final By menuImportActive = By.xpath("//a[contains(@href,'/import') and contains(@class,'active')]");
    public final By sectionInfo = By.xpath("//div[normalize-space()='Thông tin chung']");

    // Table Headers
    public final By colDrugName = By.xpath("//th[contains(.,'Tên thuốc')]");
    public final By colCategory = By.xpath("//th[contains(.,'Danh mục')]");
    public final By colUnit = By.xpath("//th[contains(.,'Đơn vị tính')]");
    public final By colQty = By.xpath("//th[contains(.,'Số lượng')]");
    public final By colPrice = By.xpath("//th[contains(.,'Đơn giá')]");
    public final By colSku = By.xpath("//th[contains(.,'Mã SKU')]");
    public final By colLot = By.xpath("//th[contains(.,'Số lô')]");
    public final By colExpiry = By.xpath("//th[contains(.,'Hạn sử dụng')]");
    public final By colDesc = By.xpath("//th[contains(.,'Mô tả')]");
    public final By colTotal = By.xpath("//th[contains(.,'Thành tiền')]");
    public final By colDelete = By.xpath("//th[contains(.,'Xóa')]");

    // Inputs & Buttons
    private final By supplierButton = By.xpath("//label[@for='supplier']/following-sibling::button");
    private final By importDateInput = By.id("importDate");
    private final By notesArea = By.id("notes");
    private final By addRowButton = By.xpath("//button[contains(.,'Thêm dòng')]");
    private final By totalAmountLabel = By.xpath("//span[contains(text(),'Tổng thành tiền')]/following-sibling::span");
    private final By finishButton = By.xpath("//button[contains(.,'Hoàn thành phiếu nhập')]");
    private final By anyToast = By.cssSelector(".swal2-toast, .toast-success, [role='alert']");

    private By expiryInputByRow(int row) {
        return By.xpath("(//table//tbody/tr)[" + row + "]//input[@type='date']");
    }

    // Row inputs (ổn định theo type/placeholder)
    private By qtyInput(int rowIndex) {
        return By.xpath("(//table//tbody/tr)[" + rowIndex + "]//input[@type='number' and @placeholder='0']");
    }

    private By priceInput(int rowIndex) {
        return By.xpath("(//table//tbody/tr)[" + rowIndex + "]//input[@type='text' and @placeholder='0']");
    }

    // Nút xóa theo dòng: <button ... disabled="">
    private By deleteBtn(int rowIndex) {
        return By.xpath("(//table//tbody/tr)[" + rowIndex + "]//button[contains(@class,'text-danger')]");
    }

    public ImportReceiptPage(WebDriver d) {
        this.driver = d;
        this.wait = new WebDriverWait(d, Duration.ofSeconds(10));
    }

    // --- ACTIONS ---

    public void open() {
        System.out.println("   [Page] Đang mở trang Nhập kho từ Menu...");
        try {
            WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(menuImportLink));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", menu);
            try {
                menu.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menu);
            }
            wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
            System.out.println("   [Page] -> Đã mở trang thành công.");
        } catch (Exception e) {
            System.out.println("   ⚠️ [Page] Click menu thất bại, thử mở URL trực tiếp.");
            driver.get("http://localhost:5173/import");
        }
    }

    public void open(String url) {
        System.out.println("   [Page] Mở URL: " + url);
        driver.get(url);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        } catch (Exception ignored) {
        }
    }

    public void scrollToElement(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'center'});", element);
            Thread.sleep(200);
        } catch (Exception ignored) {
        }
    }

    public boolean isElementDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void selectSupplier(String supplierName) {
        System.out.println("   [Input] Chọn nhà cung cấp: " + supplierName);
        scrollToElement(supplierButton);
        driver.findElement(supplierButton).click();
        selectOption(supplierName);
    }

    public void setNotes(String notes) {
        System.out.println("   [Input] Nhập ghi chú: " + notes);
        WebElement area = wait.until(ExpectedConditions.visibilityOfElementLocated(notesArea));
        area.clear();
        area.sendKeys(notes);
    }

    public int getRowCount() {
        int count = driver.findElements(By.xpath("//table//tbody/tr")).size();
        System.out.println("   [Check] Số dòng hiện tại: " + count);
        return count;
    }

    public void clickAddRow() {
        System.out.println("   [Action] Click nút '+ Thêm dòng'");
        scrollToElement(addRowButton);
        wait.until(ExpectedConditions.elementToBeClickable(addRowButton)).click();
    }

    public void setRowData(int rowIndex, String name, String category, String unit, String qty, String price) {
        System.out.println("   [Input] Nhập liệu dòng " + rowIndex + ": " + name + " | " + qty + " | " + price);
        setRowDrugName(rowIndex, name);
        selectRowCategory(rowIndex, category);
        setRowUnit(rowIndex, unit);
        setRowQuantity(rowIndex, qty);
        setRowPrice(rowIndex, price);
    }

    public void setRowDrugName(int rowIndex, String name) {
        System.out.println("      -> Nhập tên thuốc: " + name);
        By loc = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//input[@placeholder='Nhập tên thuốc']");
        smartSendKeys(loc, name);
    }

    public void selectRowCategory(int rowIndex, String categoryName) {
        System.out.println("      -> Chọn danh mục: " + categoryName);
        By btnLoc = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//button[contains(@class,'flex h-10')]");
        scrollToElement(btnLoc);
        driver.findElement(btnLoc).click();
        selectOption(categoryName);
    }

    public void setRowUnit(int rowIndex, String unit) {
        System.out.println("      -> Nhập đơn vị: " + unit);
        By loc = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//input[@placeholder='Viên, Hộp...']");
        smartSendKeys(loc, unit);
    }

    // =========================
    // VALIDATE NGAY SAU KHI NHẬP
    // =========================

    public void setRowQuantity(int rowIndex, String qty) {
        System.out.println("      -> Nhập số lượng: " + qty);

        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(qtyInput(rowIndex)));
        clearHard(el);
        el.sendKeys(qty);
        el.sendKeys(Keys.TAB);

        String actual = el.getAttribute("value");
        System.out.println("         [Verify] Qty value thực tế: " + actual);

        String type = el.getAttribute("type"); // number
        String min = el.getAttribute("min"); // 0 (theo HTML)

        // 1) Nhập số dương / 0 => phải match
        if (qty.matches("\\d+")) {
            Assert.assertEquals(normalizeNumber(actual), normalizeNumber(qty),
                    "Qty không đúng. Expect=" + qty + " | Actual=" + actual);
            return;
        }

        // 2) Nhập chữ hoặc âm => kỳ vọng UI/browser chặn (value rỗng hoặc auto-fix)
        if (actual == null || actual.isEmpty()) {
            System.out.println(
                    "         ✅ [Blocked] Qty bị chặn (type=" + type + ", min=" + min + "). input='" + qty + "'");
        } else {
            // Nếu bạn muốn strict hơn => Assert.fail ở đây
            System.out.println("         ⚠️ [Not blocked] Qty vẫn nhận: " + actual + " (input='" + qty + "')");
        }
    }

    public void setRowPrice(int rowIndex, String price) {
        System.out.println("      -> Nhập đơn giá: " + price);

        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(priceInput(rowIndex)));
        clearHard(el);
        el.sendKeys(price);
        el.sendKeys(Keys.TAB);

        String actual = el.getAttribute("value");
        System.out.println("         [Verify] Price value thực tế: " + actual);

        // 1) Nhập số thường => verify giá trị (UI có thể format 1.000)
        if (price.matches("\\d+")) {
            Assert.assertEquals(normalizeNumber(actual), normalizeNumber(price),
                    "Price không đúng. Expect=" + price + " | Actual=" + actual);
            return;
        }

        // 2) Nhập âm => UI có thể chặn hoặc giữ, TC sẽ xử lý ở test
        if (price.matches("-\\d+")) {
            if (actual == null || actual.isEmpty() || !actual.contains("-")) {
                System.out.println("         ✅ [Blocked] Price âm đã bị UI chặn/auto-fix. input='" + price
                        + "' => actual='" + actual + "'");
            } else {
                System.out.println("         ⚠️ [Not blocked] Price vẫn giữ âm: " + actual);
            }
            return;
        }

        // 3) Nhập chữ
        if (actual == null || actual.isEmpty()) {
            System.out.println("         ✅ [Blocked] Price chữ bị chặn. input='" + price + "'");
        } else {
            System.out.println("         ⚠️ [Not blocked] Price vẫn nhận: " + actual);
        }
    }

    // Getter để TC quyết định: UI chặn hay submit chặn
    public String getRowQtyValue(int rowIndex) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(qtyInput(rowIndex)));
        return el.getAttribute("value");
    }

    public String getRowPriceValue(int rowIndex) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(priceInput(rowIndex)));
        return el.getAttribute("value");
    }

    // =========================

    public void setRowSku(int rowIndex, String sku) {
        System.out.println("   [Input] Nhập SKU dòng " + rowIndex + ": " + sku);
        By loc = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//input[@placeholder='Mã SKU (tùy chọn)']");
        scrollToElement(loc);
        smartSendKeys(loc, sku);
    }

    public void setRowLot(int rowIndex, String lot) {
        System.out.println("   [Input] Nhập Lô dòng " + rowIndex + ": " + lot);
        By loc = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//input[@placeholder='Số lô']");
        scrollToElement(loc);
        smartSendKeys(loc, lot);
    }

    public String getImportDateText() {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("importDate")));
        // input này đang hiển thị kiểu MM/dd/yyyy (như hình)
        String v = el.getAttribute("value");
        System.out.println("   [Read] Ngày nhập (importDate) = " + v);
        return v;
    }

    // Set expiry đúng format cho input type=date: yyyy-MM-dd
    // public void setRowExpiry(int rowIndex, String date) {
    // System.out.println(" [Input] Nhập Hạn dùng dòng " + rowIndex + ": " + date);
    //
    // WebElement el =
    // wait.until(ExpectedConditions.visibilityOfElementLocated(expiryInput(rowIndex)));
    // scrollToElement(expiryInput(rowIndex));
    //
    // // 1) clear + sendKeys
    // clearHard(el);
    // el.sendKeys(date);
    //
    // // 2) nếu vẫn rỗng => set bằng JS (nhiều UI chặn typing)
    // String actual = el.getAttribute("value");
    // if (actual == null || actual.isEmpty()) {
    // ((JavascriptExecutor) driver).executeScript(
    // "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new
    // Event('input', {bubbles:true})); arguments[0].dispatchEvent(new
    // Event('change', {bubbles:true}));",
    // el, date
    // );
    // actual = el.getAttribute("value");
    // }
    //
    // System.out.println(" [Verify] Expiry value thực tế: " + actual);
    // }

    public void setRowDescription(int rowIndex, String desc) {
        System.out.println("   [Input] Nhập Mô tả dòng " + rowIndex + ": " + desc);
        By loc = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//input[@placeholder='Mô tả sản phẩm']");
        scrollToElement(loc);
        smartSendKeys(loc, desc);
    }

    public void clickFinish() {
        System.out.println("   [Action] Click nút 'Hoàn thành phiếu nhập'");
        scrollToElement(finishButton);
        wait.until(ExpectedConditions.elementToBeClickable(finishButton)).click();
    }

    public String readFeedback() {
        try {
            String text = wait.until(ExpectedConditions.visibilityOfElementLocated(anyToast)).getText();
            System.out.println("   [Check] Toast message: " + text);
            return text;
        } catch (Exception e) {
            try {
                Alert alert = driver.switchTo().alert();
                return alert.getText();
            } catch (Exception noAlert) {
                return "";
            }
        }
    }

    // --- PRIVATE HELPERS ---

    private void smartSendKeys(By locator, String val) {
        scrollToElement(locator);
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(val);
    }

    private void selectOption(String text) {
        try {
            By optionLoc = By.xpath("//*[@role='option' and contains(.,'" + text + "')]");
            wait.until(ExpectedConditions.elementToBeClickable(optionLoc)).click();
        } catch (Exception e) {
            System.out.println("   ⚠️ Không tìm thấy option chuẩn, thử cách dự phòng...");
            try {
                By itemLoc = By.xpath("//div[contains(@class,'item') and contains(.,'" + text + "')]");
                wait.until(ExpectedConditions.elementToBeClickable(itemLoc)).click();
            } catch (Exception ex) {
                System.out.println("   ⚠️ Fallback: Dùng phím mũi tên để chọn.");
                Actions actions = new Actions(driver);
                actions.pause(Duration.ofMillis(500));
                actions.sendKeys(Keys.ARROW_DOWN).sendKeys(Keys.ENTER).perform();
            }
        }
    }

    private void clearHard(WebElement el) {
        el.click();
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        el.sendKeys(Keys.BACK_SPACE);
    }

    private String normalizeNumber(String s) {
        if (s == null)
            return "";
        return s.replaceAll("[^0-9-]", "");
    }

    // =========================
    // READ UI: LINE AMOUNT / TOTAL
    // =========================

    public String getRowLineAmountText(int rowIndex) {
        // cột "Thành tiền" đang là td gần cuối (trước cột Xóa)
        By loc = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//td[last()-1]//div");
        scrollToElement(loc);

        // chờ chút để UI cập nhật tính toán
        try {
            Thread.sleep(300);
        } catch (Exception ignored) {
        }

        String text = driver.findElement(loc).getText().trim();
        System.out.println("   [Read] Thành tiền dòng " + rowIndex + ": " + text);
        return text;
    }

    public String getTotalAmountText() {
        scrollToElement(totalAmountLabel);
        String text = driver.findElement(totalAmountLabel).getText().trim();
        System.out.println("   [Read] Tổng tiền: " + text);
        return text;
    }

    // =========================
    // ACTION: DELETE ROW
    // =========================

    public void deleteRow(int rowIndex) {
        System.out.println("   [Action] Xóa dòng thứ " + rowIndex);

        By rowLoc = By.xpath("(//table//tbody/tr)[" + rowIndex + "]");
        By deleteBtnLoc = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//button[contains(@class,'text-danger')]");

        // đảm bảo dòng tồn tại
        wait.until(ExpectedConditions.presenceOfElementLocated(rowLoc));

        WebElement deleteBtn = wait.until(ExpectedConditions.presenceOfElementLocated(deleteBtnLoc));

        // nếu bị disable thì không click
        if (deleteBtn.getAttribute("disabled") != null) {
            System.out.println("   ⚠️ Nút xóa dòng " + rowIndex + " đang Disabled (không thể xóa).");
            return;
        }

        // click bằng JS để tránh bị che bởi scroll ngang
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
        System.out.println("   -> Đã click xóa dòng " + rowIndex);

        try {
            Thread.sleep(500);
        } catch (Exception ignored) {
        }
    }

    public boolean isDeleteButtonDisabled(int rowIndex) {
        WebElement btn = wait.until(
                ExpectedConditions.presenceOfElementLocated(deleteBtn(rowIndex)));

        boolean disabledAttr = btn.getAttribute("disabled") != null;
        boolean disabledByClass = btn.getAttribute("class").contains("disabled");

        System.out.println("         [Debug] delete.disabledAttr=" + disabledAttr
                + " | class=" + btn.getAttribute("class"));

        return disabledAttr || disabledByClass;
    }

    public void clickDeleteButton(int rowIndex) {
        System.out.println("   [Action] Click nút xóa dòng " + rowIndex);
        WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(deleteBtn(rowIndex)));
        scrollToElement(deleteBtn(rowIndex));
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public String getImportDateValue() {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("importDate")));
        return el.getAttribute("value");
    }

    public void setRowExpiry(int rowIndex, String yyyyMmDd) {
        System.out.println("   [Input] Nhập Hạn dùng dòng " + rowIndex + ": " + yyyyMmDd);

        By loc = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//input[@type='date']");

        // Retry logic: Try max 3 times if value doesn't stick
        for (int i = 0; i < 3; i++) {
            try {
                WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(loc));
                scrollToElement(loc);

                // 1. Try standard Clear + SendKeys
                try {
                    el.click();
                    el.clear();
                } catch (Exception ignored) {
                }

                el.sendKeys(yyyyMmDd);
                el.sendKeys(Keys.TAB); // Blur
                Thread.sleep(200);

                // 2. Validate
                String actual = el.getAttribute("value");
                if (actual != null && actual.equals(yyyyMmDd)) {
                    System.out.println("      [Verify] Expiry set thành công: " + actual);
                    return; // Success
                }

                // 3. Fallback: Javascript if standard failed
                System.out.println("   ⚠️ [Retry " + (i + 1) + "] SendKeys fail, thử JS Set Value (React-safe)...");
                ((JavascriptExecutor) driver).executeScript(
                        "let input = arguments[0];" +
                                "let lastValue = input.value;" +
                                "input.value = '" + yyyyMmDd + "';" +
                                "let event = new Event('input', { bubbles: true });" +
                                "let tracker = input._valueTracker;" +
                                "if (tracker) { tracker.setValue(lastValue); }" +
                                "input.dispatchEvent(event);" +
                                "input.dispatchEvent(new Event('change', { bubbles: true }));" +
                                "input.dispatchEvent(new Event('blur', { bubbles: true }));",
                        el);

                // Click body to force blur validation
                try {
                    driver.findElement(By.tagName("body")).click();
                } catch (Exception ignored) {
                }
                Thread.sleep(500);

                actual = el.getAttribute("value");
                if (actual != null && actual.equals(yyyyMmDd)) {
                    System.out.println("      [Verify] Expiry JS set thành công: " + actual);
                    return;
                }

            } catch (Exception e) {
                System.out.println("   ⚠️ [Error] setRowExpiry attempt " + (i + 1) + ": " + e.getMessage());
            }
        }
        System.out.println("      ❌ [Fail] Không thể set expiry sau 3 lần thử.");
    }

    public String getRowExpiryValue(int rowIndex) {
        By loc = By.xpath("(//table//tbody/tr)[" + rowIndex + "]//input[@type='date']");
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(loc));
        return el.getAttribute("value");
    }

}
