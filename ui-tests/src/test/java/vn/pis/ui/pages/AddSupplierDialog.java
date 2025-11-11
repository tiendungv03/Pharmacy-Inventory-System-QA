package vn.pis.ui.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;

/**
 * AddSupplierDialog – Page Object cho Popup "Thêm nhà cung cấp mới" (FE-UI2)
 * 7 Fields: name, contactName, phone, email, address, taxCode, status
 * Methods: Fill form, Validation, Submit, Close
 */
public class AddSupplierDialog {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // =========================
    // Locators - Dialog & Header
    // =========================
    private final By dialog = By.xpath("//div[@role='dialog'] | //div[contains(@class,'modal') and contains(@class,'show')]");
    private final By dialogTitle = By.xpath("//div[@role='dialog']//h2 | //div[@role='dialog']//h1 | //div[contains(@class,'modal')]//h2");
    
    // =========================
    // Locators - Form Fields (7 fields)
    // =========================
    private final By fieldName = By.id("name");
    private final By fieldContactName = By.id("contactName");
    private final By fieldPhone = By.id("phone");
    private final By fieldEmail = By.id("email");
    private final By fieldAddress = By.id("address");
    private final By fieldTaxCode = By.id("taxCode");
    private final By fieldStatus = By.id("status");

    // Alternative locators nếu ID không work
    private final By fieldNameAlt = By.xpath("//input[@name='name' or @placeholder*='Nhập tên nhà cung cấp']");
    private final By fieldPhoneAlt = By.xpath("//input[@name='phone' or @placeholder*='Nhập số điện thoại']");
    private final By fieldEmailAlt = By.xpath("//input[@name='email' or @placeholder*='Nhập email']");
    private final By fieldAddressAlt = By.xpath("//textarea[@name='address' or @placeholder*='Nhập địa chỉ']");

    // =========================
    // Locators - Error Messages
    // =========================
    private final By errorMessages = By.xpath("//span[contains(@class,'text-red') or contains(@class,'error')] | //*[@role='alert']");

    // =========================
    // Locators - Buttons
    // =========================
    private final By btnSubmit = By.xpath("//button[@type='submit' and contains(., 'Thêm')] | //button[@type='submit' and (contains(@class,'bg-primary') or contains(@class,'primary'))]");
    private final By overlayBackdrop = By.xpath("//div[@class='fixed inset-0'] | //div[contains(@class,'overlay') or contains(@class,'backdrop')]");

    // =========================
    // Locators - Toast/Alert
    // =========================
    private final By toastSuccess = By.xpath("//div[contains(@class,'toast') or contains(@class,'alert')]//span[contains(text(),'thành công') or contains(text(),'thêm')]");
    private final By toastError = By.xpath("//div[contains(@class,'toast') or contains(@class,'alert')]//span[contains(text(),'lỗi') or contains(text(),'error')]");

    // =========================
    // Constructor
    // =========================
    public AddSupplierDialog(WebDriver d) {
        this.driver = d;
        this.wait = new WebDriverWait(d, Duration.ofSeconds(10));
    }

    // =========================
    // Dialog Visibility
    // =========================
    public boolean isDialogOpen() {
        try {
            WebElement dlg = driver.findElement(dialog);
            return dlg.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void waitForDialogOpen() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialog));
    }

    public void waitForDialogClose() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(dialog));
    }

    public String getDialogTitle() {
        try {
            return driver.findElement(dialogTitle).getText().trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    // =========================
    // FE-UI2-A02: Form Fields (7 fields)
    // =========================
    public int getFormFieldCount() {
        try {
            // Kiểm tra by ID
            List<WebElement> fields = driver.findElements(By.xpath(
                "//div[@role='dialog']//*[@id='name' or @id='contactName' or @id='phone' or @id='email' or @id='address' or @id='taxCode' or @id='status']"
            ));
            return fields.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean hasAllRequiredFields() {
        return getFormFieldCount() >= 7;
    }

    // =========================
    // FE-UI2-A03: Placeholder Check
    // =========================
    public String getFieldPlaceholder(String fieldName) {
        try {
            By field = getFieldLocator(fieldName);
            WebElement elem = driver.findElement(field);
            return elem.getAttribute("placeholder");
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    // =========================
    // FE-UI2-A04: Default Status Value
    // =========================
    public String getStatusDefaultValue() {
        try {
            WebElement statusField = driver.findElement(fieldStatus);
            String value = statusField.getAttribute("value");
            if (value == null || value.isEmpty()) {
                value = statusField.getText();
            }
            return value;
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    // =========================
    // Fill Form Methods
    // =========================
    public void fillName(String value) {
        WebElement field = findFieldElement(fieldName, fieldNameAlt);
        if (field != null) {
            field.clear();
            field.sendKeys(value);
        }
    }

    public void fillContactName(String value) {
        WebElement field = findFieldElement(fieldContactName, null);
        if (field != null) {
            field.clear();
            field.sendKeys(value);
        }
    }

    public void fillPhone(String value) {
        WebElement field = findFieldElement(fieldPhone, fieldPhoneAlt);
        if (field != null) {
            field.clear();
            field.sendKeys(value);
        }
    }

    public void fillEmail(String value) {
        WebElement field = findFieldElement(fieldEmail, fieldEmailAlt);
        if (field != null) {
            field.clear();
            field.sendKeys(value);
        }
    }

    public void fillAddress(String value) {
        WebElement field = findFieldElement(fieldAddress, fieldAddressAlt);
        if (field != null) {
            field.clear();
            field.sendKeys(value);
        }
    }

    public void fillTaxCode(String value) {
        WebElement field = findFieldElement(fieldTaxCode, null);
        if (field != null) {
            field.clear();
            field.sendKeys(value);
        }
    }

    public void selectStatus(String value) {
        try {
            WebElement statusField = driver.findElement(fieldStatus);
            if (statusField.getTagName().equals("select")) {
                new Select(statusField).selectByValue(value);
            } else {
                // Combobox/other
                statusField.click();
                driver.findElement(By.xpath("//*[contains(text(),'" + value + "')]")).click();
            }
        } catch (Exception e) {
            System.err.println("Cannot select status: " + e.getMessage());
        }
    }

    public void fillFormValid(String name, String contact, String phone, String email, String address, String taxCode) {
        fillName(name);
        fillContactName(contact);
        fillPhone(phone);
        fillEmail(email);
        fillAddress(address);
        fillTaxCode(taxCode);
    }

    // =========================
    // FE-UI2-A05/A06/A07/A08: Validation & Error Messages
    // =========================
    public String getDialogText() {
        try {
            return driver.findElement(dialog).getText();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    public boolean hasErrorMessage(String keyword) {
        String dialogText = driver.findElement(dialog).getText().toLowerCase();
        return dialogText.contains(keyword.toLowerCase());
    }

    public String getErrorText() {
        try {
            List<WebElement> errors = driver.findElements(errorMessages);
            if (!errors.isEmpty()) {
                return errors.get(0).getText().trim();
            }
        } catch (Exception e) {
        }
        return "";
    }

    public boolean isDialogStillOpen() {
        return isDialogOpen();
    }

    // =========================
    // FE-UI2-A09/A10: Submit
    // =========================
    public void submitForm() {
        try {
            WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(btnSubmit));
            System.out.println("✓ Found submit button: " + submit.getText());
            
            // Scroll into view and wait for overlay to settle
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", submit);
            Thread.sleep(500);
            
            try {
                submit.click();
                System.out.println("✓ Clicked submit button (normal click)");
            } catch (ElementClickInterceptedException e) {
                // Fallback: use JavaScript click
                System.out.println("⚠ Normal click blocked, using JavaScript click");
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submit);
                System.out.println("✓ Clicked submit button (JS click)");
            }
            
            // Wait for validation to show errors
            Thread.sleep(800);
        } catch (Exception e) {
            throw new RuntimeException("Cannot click submit button: " + e.getMessage());
        }
    }

    public void submitByEnter() {
        // Simulate Enter key from last field
        WebElement lastField = findFieldElement(fieldAddress, fieldAddressAlt);
        if (lastField != null) {
            lastField.sendKeys(Keys.ENTER);
        }
    }

    public String waitForFeedback(long timeoutMs) {
        long endTime = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < endTime) {
            try {
                List<WebElement> toasts = driver.findElements(toastSuccess);
                if (!toasts.isEmpty()) {
                    return "success: " + toasts.get(0).getText();
                }
                toasts = driver.findElements(toastError);
                if (!toasts.isEmpty()) {
                    return "error: " + toasts.get(0).getText();
                }
            } catch (Exception e) {
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return "";
    }

    // =========================
    // FE-UI2-A11/A12: Close Dialog
    // =========================
    public void clickCancel() {
        try {
            // Try to find cancel button by text or class
            List<WebElement> buttons = driver.findElements(By.xpath("//div[@role='dialog']//button"));
            WebElement cancelBtn = null;
            
            for (WebElement btn : buttons) {
                String text = btn.getText().toLowerCase();
                if (text.contains("hủy") || text.contains("huỷ") || text.contains("cancel")) {
                    cancelBtn = btn;
                    break;
                }
            }
            
            if (cancelBtn == null && buttons.size() >= 2) {
                // If not found by text, try second-to-last button
                cancelBtn = buttons.get(buttons.size() - 2);
            }
            
            if (cancelBtn != null) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", cancelBtn);
                Thread.sleep(300);
                
                try {
                    cancelBtn.click();
                } catch (ElementClickInterceptedException e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cancelBtn);
                }
                System.out.println("✓ Clicked cancel button");
            } else {
                throw new RuntimeException("Cancel button not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot click cancel: " + e.getMessage());
        }
    }

    public void clickClose() {
        try {
            // Try to find close button (usually the first close-like button or top-right corner)
            List<WebElement> closeButtons = driver.findElements(By.xpath("//div[@role='dialog']//button[contains(@aria-label, 'close') or contains(@aria-label, 'Close') or contains(@aria-label, 'đóng') or contains(@aria-label, 'Đóng')]"));
            
            WebElement closeBtn = null;
            if (!closeButtons.isEmpty()) {
                closeBtn = closeButtons.get(0);
            } else {
                // Fallback: find X button or first button in dialog header
                List<WebElement> headerButtons = driver.findElements(By.xpath("//div[@role='dialog']//button[contains(@class, 'absolute') or contains(@class, 'top')]"));
                if (!headerButtons.isEmpty()) {
                    closeBtn = headerButtons.get(0);
                }
            }
            
            if (closeBtn != null) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", closeBtn);
                Thread.sleep(300);
                
                try {
                    closeBtn.click();
                } catch (ElementClickInterceptedException e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeBtn);
                }
                System.out.println("✓ Clicked close button");
            } else {
                throw new RuntimeException("Close button not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot click close: " + e.getMessage());
        }
    }

    public void clickOverlay() {
        try {
            List<WebElement> backdrops = driver.findElements(overlayBackdrop);
            if (!backdrops.isEmpty()) {
                backdrops.get(0).click();
            }
        } catch (Exception e) {
            System.err.println("Cannot click overlay: " + e.getMessage());
        }
    }

    public void dismissAlert() {
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            System.out.println("⚠ Alert detected: " + alert.getText());
            alert.dismiss();
            System.out.println("✓ Alert dismissed");
        } catch (TimeoutException e) {
            // No alert present - OK
        } catch (Exception e) {
            System.err.println("Cannot dismiss alert: " + e.getMessage());
        }
    }

    // =========================
    // FE-UI2-A15: Scroll Check
    // =========================
    public boolean canScroll() {
        try {
            WebElement dlg = driver.findElement(dialog);
            Long scrollHeight = (Long) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].scrollHeight", dlg);
            Long clientHeight = (Long) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].clientHeight", dlg);
            return scrollHeight > clientHeight;
        } catch (Exception e) {
            return false;
        }
    }

    // =========================
    // Helpers
    // =========================
    private WebElement findFieldElement(By primary, By fallback) {
        try {
            return driver.findElement(primary);
        } catch (NoSuchElementException e) {
            if (fallback != null) {
                try {
                    return driver.findElement(fallback);
                } catch (NoSuchElementException e2) {
                }
            }
        }
        return null;
    }

    private By getFieldLocator(String fieldName) {
        switch (fieldName.toLowerCase()) {
            case "name":
                return fieldName.equalsIgnoreCase("name") ? this.fieldName : this.fieldNameAlt;
            case "phone":
                return this.fieldPhone;
            case "email":
                return this.fieldEmail;
            case "address":
                return this.fieldAddress;
            default:
                return this.fieldName;
        }
    }

    public void assertDialogTitle(String expectedTitle) {
        Assert.assertTrue(
            getDialogTitle().contains(expectedTitle),
            "Dialog title không đúng. Expected: " + expectedTitle + ", Actual: " + getDialogTitle()
        );
    }

    public void assertFormFieldsCount(int expected) {
        Assert.assertTrue(
            getFormFieldCount() >= expected,
            "Phải có " + expected + " fields, thực tế: " + getFormFieldCount()
        );
    }

    public void assertStatusDefault(String expected) {
        String actual = getStatusDefaultValue();
        Assert.assertEquals(
            actual, expected,
            "Status default phải là '" + expected + "', thực tế: " + actual
        );
    }

    public void assertDialogClosed() {
        Assert.assertFalse(
            isDialogOpen(),
            "Dialog vẫn còn mở"
        );
    }

    public void assertDialogStillOpen() {
        Assert.assertTrue(
            isDialogOpen(),
            "Dialog đã đóng"
        );
    }
}
