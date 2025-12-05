
package vn.pis.ui.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ImportReceiptPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ---------- MENU / TITLE ----------
    private final By menuImportLink = By.xpath(
            "//a[contains(@href,'/import') and .//span[normalize-space()='Nhập kho']]"
    );
    private final By pageTitle = By.xpath("//h1[normalize-space()='Tạo phiếu nhập kho']");

    // ---------- THÔNG TIN CHUNG ----------
    private final By supplierButton = By.xpath("//label[@for='supplier']/following-sibling::button");
    private final By supplierSpan = By.xpath("//label[@for='supplier']/following-sibling::button//span");
    private final By importDateInput = By.id("importDate");
    private final By notesArea = By.id("notes");

    // ---------- CHI TIẾT PHIẾU NHẬP ----------
    private final By addRowButton = By.xpath("//button[.//span[normalize-space()='Thêm dòng'] or normalize-space()='Thêm dòng']");

    private By rowTextInput(int rowIndex, String placeholder) {
        return By.xpath("(//table//tbody/tr)[" + rowIndex + "]//input[@placeholder=\"" + placeholder + "\"]");
    }

    private By rowNumberInputs(int rowIndex) {
        return By.xpath("(//table//tbody/tr)[" + rowIndex + "]//input[@type='number' and @placeholder='0']");
    }

    private By rowDateInput(int rowIndex) {
        return By.xpath("(//table//tbody/tr)[" + rowIndex + "]//input[@type='date']");
    }

    private By rowCategoryButton(int rowIndex) {
        return By.xpath("(//table//tbody/tr)[" + rowIndex + "]//button[contains(@class,'flex h-10')]");
    }

    private By rowCategorySpan(int rowIndex) {
        return By.xpath("(//table//tbody/tr)[" + rowIndex + "]//button[contains(@class,'flex h-10')]//span");
    }

    private By rowLineAmount(int rowIndex) {
        return By.xpath("(//table//tbody/tr)[" + rowIndex + "]//td[last()-1]//div");
    }

    private By rowDeleteButton(int rowIndex) {
        return By.xpath("(//table//tbody/tr)[" + rowIndex + "]//button[.//svg[contains(@class,'trash')]]");
    }

    // ---------- TỔNG KẾT ----------
    private final By totalAmountLabel = By.xpath(
            "//span[contains(@class,'text-xl') and contains(@class,'font-bold') and contains(@class,'text-medical-blue')]"
    );

    // ---------- NÚT HOÀN THÀNH ----------
    private final By finishButton = By.xpath(
            "//button[.//span[normalize-space()='Hoàn thành phiếu nhập'] or normalize-space()='Hoàn thành phiếu nhập']"
    );

    // ---------- THÔNG BÁO ----------
    private final By anyToast = By.cssSelector(
            ".swal2-toast .swal2-title, .swal2-container .swal2-html-container," +
                    ".toast-success, .alert-success, .alert.alert-success, [role='alert']"
    );

    public ImportReceiptPage(WebDriver d) {
        this.driver = d;
        this.wait = new WebDriverWait(d, Duration.ofSeconds(10));
    }

    // ========= NAV =========
    public void open() {
        WebElement link = wait.until(ExpectedConditions.visibilityOfElementLocated(menuImportLink));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", link);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(menuImportLink)).click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
    }

    // ========= THÔNG TIN CHUNG =========
    public void selectSupplier(String supplierName) {
        selectFromDropdown(supplierButton, supplierSpan, supplierName);
    }

    public void setImportDate(String yyyyMMdd) {
        WebElement date = wait.until(ExpectedConditions.visibilityOfElementLocated(importDateInput));
        date.clear();
        date.sendKeys(yyyyMMdd);
    }

    public String getImportDate() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(importDateInput))
                .getAttribute("value");
    }

    public void setNotes(String notes) {
        WebElement area = wait.until(ExpectedConditions.visibilityOfElementLocated(notesArea));
        area.clear();
        area.sendKeys(notes);
    }

    // ========= DÒNG CHI TIẾT =========
    public void clickAddRow() {
        wait.until(ExpectedConditions.elementToBeClickable(addRowButton)).click();
    }

    public int getRowCount() {
        return driver.findElements(By.xpath("//table//tbody/tr")).size();
    }

    public void setRowDrugName(int rowIndex, String name) {
        WebElement ip = wait.until(ExpectedConditions.visibilityOfElementLocated(
                rowTextInput(rowIndex, "Nhập tên thuốc")));
        ip.clear();
        ip.sendKeys(name);
    }

    public void selectRowCategory(int rowIndex, String categoryName) {
        selectFromDropdown(rowCategoryButton(rowIndex), rowCategorySpan(rowIndex), categoryName);
    }

    public void setRowUnit(int rowIndex, String unit) {
        WebElement ip = wait.until(ExpectedConditions.visibilityOfElementLocated(
                rowTextInput(rowIndex, "Viên, Hộp...")));
        ip.clear();
        ip.sendKeys(unit);
    }

    public void setRowSku(int rowIndex, String sku) {
        WebElement ip = wait.until(ExpectedConditions.visibilityOfElementLocated(
                rowTextInput(rowIndex, "Mã SKU (tùy chọn)")));
        ip.clear();
        ip.sendKeys(sku);
    }

    public void setRowQuantity(int rowIndex, String qty) {
        By nums = rowNumberInputs(rowIndex);
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(nums, 0));
        List<WebElement> list = driver.findElements(nums);
        WebElement ip = list.get(0);
        ip.clear();
        ip.sendKeys(qty);
    }

    public void setRowPrice(int rowIndex, String price) {
        By nums = rowNumberInputs(rowIndex);
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(nums, 0));
        List<WebElement> list = driver.findElements(nums);
        WebElement ip = list.size() > 1 ? list.get(1) : list.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", ip);
        ip.clear();
        ip.sendKeys(price);
    }

    public void setRowLot(int rowIndex, String lot) {
        WebElement ip = wait.until(ExpectedConditions.visibilityOfElementLocated(
                rowTextInput(rowIndex, "Số lô")));
        ip.clear();
        ip.sendKeys(lot);
    }

    public void setRowExpiry(int rowIndex, String yyyyMMdd) {
        WebElement ip = wait.until(ExpectedConditions.visibilityOfElementLocated(rowDateInput(rowIndex)));
        ip.clear();
        ip.sendKeys(yyyyMMdd);
    }

    public void setRowDescription(int rowIndex, String desc) {
        WebElement ip = wait.until(ExpectedConditions.visibilityOfElementLocated(
                rowTextInput(rowIndex, "Mô tả sản phẩm")));
        ip.clear();
        ip.sendKeys(desc);
    }

    public String getRowLineAmountText(int rowIndex) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(rowLineAmount(rowIndex)))
                .getText().trim();
    }

    public void deleteRow(int rowIndex) {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(rowDeleteButton(rowIndex)));
        btn.click();
        wait.until(ExpectedConditions.numberOfElementsToBeLessThan(By.xpath("//table//tbody/tr"), rowIndex + 1));
    }

    // ========= TỔNG + HOÀN THÀNH =========
    public String getTotalAmountText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(totalAmountLabel))
                .getText().trim();
    }

    public void clickFinish() {
        wait.until(ExpectedConditions.elementToBeClickable(finishButton)).click();
    }

    // ========= THÔNG BÁO =========
    public String readFeedback() {
        try {
            Alert a = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.alertIsPresent());
            String t = a.getText();
            a.accept();
            return t == null ? "" : t.trim();
        } catch (TimeoutException | NoAlertPresentException ignored) {
        }

        try {
            String t = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(anyToast))
                    .getText();
            return t == null ? "" : t.trim();
        } catch (TimeoutException e) {
            return "";
        }
    }

    // ========= DROPDOWN HELPER =========
    private void selectFromDropdown(By buttonLocator, By spanLocator, String optionText) {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(buttonLocator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        btn.click();

        // 1) cố chọn đúng text nếu có
        if (optionText != null && !optionText.isBlank()) {
            By optionByText = By.xpath(
                    "//*[normalize-space()='" + optionText + "'][self::div or self::li or self::button]"
            );
            try {
                WebElement opt = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(optionByText));
                opt.click();
                wait.until(ExpectedConditions.textToBePresentInElementLocated(spanLocator, optionText));
                return;
            } catch (TimeoutException ignored) {
            }
        }

        // 2) fallback: chọn option đầu tiên (div/li/button)
        By firstOption = By.xpath(
                "(//div[@role='option'] | //div[@data-radix-select-item] | " +
                        "//div[contains(@class,'SelectItem')] | //li[@role='option'] | " +
                        "//button[@role='option'] | //button[contains(@class,'SelectItem')])[1]"
        );
        try {
            WebElement first = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(firstOption));
            String text = first.getText().trim();
            first.click();
            if (!text.isEmpty()) {
                wait.until(ExpectedConditions.textToBePresentInElementLocated(spanLocator, text));
            }
        } catch (TimeoutException e) {
            // 3) nếu vẫn không tìm được option → dùng phím mũi tên xuống + Enter
            btn.sendKeys(Keys.ARROW_DOWN);
            btn.sendKeys(Keys.ENTER);
        }
    }


    public String waitForAlertAndGetText(int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        String text = alert.getText();
        alert.accept(); // bấm OK
        return text;
    }
}
