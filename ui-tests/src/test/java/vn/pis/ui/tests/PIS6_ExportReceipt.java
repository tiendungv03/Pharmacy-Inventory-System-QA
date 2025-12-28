package vn.pis.ui.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;
import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.ExportReceiptPage;
import vn.pis.ui.pages.LoginPage;
import java.time.ZoneId;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static vn.pis.ui.util.TestEnv.*;

/**
 * PIS-6: Tạo phiếu xuất kho
 * Bao gồm các TC từ TC-01 đến TC-18
 */
@Listeners(PIS2_ConsoleLogger.class)
public class PIS6_ExportReceipt extends BaseTest {

    private static final String DEPARTMENT_NAME   = "Khoa Nội";
    private static final String DRUG_NAME         = "Thuốc nhỏ mắt V.Rohto Vitamin hỗ trợ cải thiện tình trạng giảm thị lực, mắt mờ (13ml)"; // Đảm bảo thuốc này có trong kho
    private static final int    STOCK_SAFE_QTY    = 1;      // Số lượng nhỏ (<= tồn)
    private static final int    STOCK_OVER_QTY    = 999999; // Số lượng lớn (> tồn)
    private static final String MSG_SUCCESS       = "Tạo phiếu xuất kho thành công";

    // Helper: Parse tiền tệ (30.000 -> 30000)
    private long parseCurrency(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        return Long.parseLong(digits);
    }

    private void log(String msg) {
        String line = "[PIS6] " + msg;
        System.out.println(line);
        Reporter.log(line, true);
    }

    // Login 1 lần
    @BeforeClass(alwaysRun = true)
    public void loginOnce() {
        log("--- Đăng nhập hệ thống ---");
        LoginPage login = new LoginPage(driver);
        login.open(BASE_URL + "/login");
        login.login(ADMIN_USER, ADMIN_PASS);
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(java.lang.reflect.Method m){
        log("▶ BẮT ĐẦU TC: " + m.getName());
    }




    // =========================
    // HELPERS (add inside class)
    // =========================
    private void blurToTriggerValidation() {
        try {
            driver.findElement(By.id("notes")).click();
        } catch (Exception e) {
            try { driver.findElement(By.tagName("body")).click(); } catch (Exception ignored) {}
        }
    }

    private WebElement finishButtonEl() {
        return driver.findElement(By.xpath("//button[.//span[normalize-space()='Hoàn thành phiếu xuất'] or normalize-space()='Hoàn thành phiếu xuất']"));
    }

    private boolean isDisabledAttr(WebElement el) {
        String dis = el.getAttribute("disabled");
        return dis != null && (dis.equalsIgnoreCase("true") || dis.equalsIgnoreCase("disabled"));
    }

    private void waitDialogVisible(int seconds) {
        new WebDriverWait(driver, Duration.ofSeconds(seconds))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@role='dialog']")));
    }

    private String dialogTitle() {
        try {
            return driver.findElement(By.xpath("//div[@role='dialog']//h2")).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String dialogText() {
        try {
            return driver.findElement(By.xpath("//div[@role='dialog']")).getText();
        } catch (Exception e) {
            return "";
        }
    }

    private void clickDialogClose() {
        driver.findElement(By.xpath("//div[@role='dialog']//button[normalize-space()='Đóng']")).click();
    }

    private void clickDialogCreateNew() {
        driver.findElement(By.xpath(
                "//div[@role='dialog']//button[" +
                        "normalize-space()='Tạo Phiếu Mới' or normalize-space()='Tạo phiếu mới' or " +
                        "contains(normalize-space(),'Tạo')]" // fallback
        )).click();
    }

    private void createSuccess_OneRow() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();
        page.selectDepartment(DEPARTMENT_NAME);
        page.setNotes("Auto PIS6 - create success");
        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);

        // ✅ tăng timeout auto-fill cho ổn định
        page.waitRowAutoFilled(1, 20);

        page.setRowQuantity(1, String.valueOf(STOCK_SAFE_QTY));
        blurToTriggerValidation();

        // ✅ tăng timeout đợi enable
        page.waitUntilFinishEnabled(25);
        page.clickFinish();
        waitDialogVisible(15);
    }


    ////////////////UI///////

    @Test(priority = 1, description = "PIS-6-TC-01: Mở màn hình tạo phiếu xuất kho")
    public void TC01_OpenExportPage() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        String title = driver.findElement(By.xpath("//h1")).getText();
        Assert.assertTrue(title.contains("Tạo phiếu xuất kho"), "Tiêu đề trang không đúng!");
    }

    @Test(priority = 2, description = "PIS-6-TC-02: Chọn Khoa/Phòng nhận (Bắt buộc)")
    public void TC02_SelectDepartment() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        page.selectDepartment(DEPARTMENT_NAME);

        WebElement deptSpan = driver.findElement(By.xpath("//label[contains(.,'Khoa/Phòng nhận')]/following-sibling::button//span"));
        Assert.assertTrue(deptSpan.getText().contains(DEPARTMENT_NAME), "Dropdown không hiển thị tên khoa đã chọn");
    }

    @Test(priority = 3, description = "PIS-6-TC-03: Ngày xuất mặc định là hôm nay và KHÔNG cho phép chỉnh sửa")
    public void TC03_VerifyExportDate_DefaultAndReadOnly() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        String actualDate = page.getExportDate();

        ZoneId serverZone = ZoneId.of("GMT");
        String expectedDate = LocalDate.now(serverZone).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        log("Ngày trên web: " + actualDate + " | Ngày hệ thống (Expected): " + expectedDate);
        Assert.assertEquals(actualDate, expectedDate, "Ngày xuất mặc định không đúng ngày hiện tại!");

        WebElement dateInput = page.getExportDateElement();

        boolean isReadOnly = dateInput.getAttribute("readonly") != null;
        boolean isDisabled = dateInput.getAttribute("disabled") != null;

        log("Trạng thái Input -> Readonly: " + isReadOnly + " | Disabled: " + isDisabled);
        Assert.assertTrue(isReadOnly || isDisabled, "LỖI: Ô Ngày xuất vẫn cho phép chỉnh sửa (không có readonly/disabled)!");
    }

    @Test(priority = 4, description = "PIS-6-TC-04: Ghi chú tùy chọn")
    public void TC04_InputNotes() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        String noteContent = "Ghi chú Test Auto PIS6";
        page.setNotes(noteContent);

        WebElement noteArea = driver.findElement(By.id("notes"));
        Assert.assertEquals(noteArea.getAttribute("value"), noteContent, "Nội dung ghi chú không khớp!");
    }

    @Test(priority = 5, description = "PIS-6-TC-05, TC-06, TC-07: Thêm dòng, Gợi ý thuốc, Auto fill")
    public void TC05_TC06_TC07_RowInteraction_AutoFill() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();
        page.selectDepartment(DEPARTMENT_NAME);
        page.setNotes("Phiếu xuất tự động - PIS6");

        if (page.getRowCount() == 0) {
            page.clickAddRow();
        }
        Assert.assertTrue(page.getRowCount() > 0, "Không có dòng thuốc nào hiển thị!");

        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);

        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        String price = page.getRowPriceText(1);
        String expiry = page.getRowExpiryText(1);
        String lot = page.getRowLotText(1);

        log("Data auto-fill -> Price: " + price + ", Exp: " + expiry + ", Lot: " + lot);

        Assert.assertNotEquals(parseCurrency(price), 0, "Đơn giá chưa được tự động điền!");
        Assert.assertFalse(expiry.isEmpty(), "Hạn sử dụng chưa được tự động điền!");
    }

    @Test(priority = 6, description = "PIS-6-TC-08, TC-09: Tính thành tiền và Tổng tiền")
    public void TC08_TC09_Calculation() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();
        page.selectDepartment(DEPARTMENT_NAME);
        page.setNotes("Phiếu xuất tự động - PIS6");
        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);

        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        long price = parseCurrency(page.getRowPriceText(1));

        int quantity = 5;
        page.setRowQuantity(1, String.valueOf(quantity));

        try { Thread.sleep(500); } catch (InterruptedException e) {}

        long lineAmount = parseCurrency(page.getRowLineAmountText(1));
        long expectedLineAmount = price * quantity;
        Assert.assertEquals(lineAmount, expectedLineAmount, "Tính Thành tiền dòng bị sai!");

        long totalAmount = parseCurrency(page.getTotalAmountText());
        Assert.assertEquals(totalAmount, expectedLineAmount, "Tổng thành tiền phiếu xuất bị sai!");
    }

    @Test(priority = 7, description = "PIS-6-TC-10: Xóa dòng thuốc")
    public void TC10_DeleteRow() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();
        page.setNotes("Phiếu xuất tự động - PIS6");

        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);
        page.clickAddRow();

        try { Thread.sleep(500); } catch (InterruptedException e) {}

        int countBefore = page.getRowCount();
        log("Số dòng trước khi xóa: " + countBefore);
        Assert.assertTrue(countBefore >= 2, "Cần ít nhất 2 dòng để test xóa");

        page.deleteRow(2);

        int countAfter = page.getRowCount();
        log("Số dòng sau khi xóa: " + countAfter);
        Assert.assertEquals(countAfter, countBefore - 1, "Số lượng dòng không giảm sau khi xóa!");
    }

    @Test(priority = 8, description = "PIS-6-TC-20: Nút Hoàn thành DISABLED khi chưa chọn Khoa/Phòng")
    public void TC20_FinishDisabled_WhenMissingDepartment() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        page.setNotes("Auto PIS6 - missing dept");
        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);
        page.waitRowAutoFilled(1, 15);
        page.setRowQuantity(1, "1");
        blurToTriggerValidation();

        WebElement btn = finishButtonEl();
        Assert.assertTrue(!btn.isEnabled() || isDisabledAttr(btn),
                "FAIL: Nút Hoàn thành vẫn bật dù chưa chọn Khoa/Phòng!");
    }

    @Test(priority = 9, description = "PIS-6-TC-21: Nút Hoàn thành DISABLED khi chưa chọn thuốc (chỉ gõ text)")
    public void TC21_FinishDisabled_WhenDrugNotSelected() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        page.selectDepartment(DEPARTMENT_NAME);
        page.setNotes("Auto PIS6 - drug not selected");

        WebElement drugInput = driver.findElement(By.xpath("//input[@placeholder='Nhập sản phẩm...']"));
        drugInput.click();
        drugInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        drugInput.sendKeys(Keys.DELETE);
        drugInput.sendKeys("thuoc_khong_ton_tai");

        page.setRowQuantity(1, "1");
        blurToTriggerValidation();

        WebElement btn = finishButtonEl();
        Assert.assertTrue(!btn.isEnabled() || isDisabledAttr(btn),
                "FAIL: Nút Hoàn thành vẫn bật dù chưa chọn thuốc từ gợi ý!");
    }

    @Test(priority = 10, description = "PIS-6-TC-13: Bắt buộc nhập Số lượng")
    public void TC13_Validate_EmptyQuantity() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        page.selectDepartment(DEPARTMENT_NAME);
        page.setNotes("Phiếu xuất tự động - PIS6");

        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);

        WebElement qtyInput = page.getQuantityInput(1);
        qtyInput.click();
        qtyInput.sendKeys(Keys.CONTROL, "a");
        qtyInput.sendKeys(Keys.DELETE);

        if (!page.isFinishEnabled()) return;

        page.clickFinish();
        String msg = page.readFeedback();
        String title = driver.findElement(By.xpath("//h1")).getText();

        Assert.assertFalse(msg.toLowerCase().contains("thành công"),
                "Không được phép tạo phiếu khi thiếu Số lượng! Msg=" + msg);
        Assert.assertTrue(title.contains("Tạo phiếu xuất kho"),
                "Không được rời trang tạo phiếu khi thiếu Số lượng!");
    }

    @Test(priority = 11, description = "PIS-6-TC-23: Số lượng = 0 => DISABLED hoặc báo lỗi")
    public void TC23_QuantityZero_ShouldNotAllowFinish() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        page.selectDepartment(DEPARTMENT_NAME);
        page.setNotes("Auto PIS6 - qty 0");
        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);
        page.waitRowAutoFilled(1, 15);

        page.setRowQuantity(1, "0");
        blurToTriggerValidation();

        WebElement btn = finishButtonEl();
        if (btn.isEnabled() && !isDisabledAttr(btn)) {
            page.clickFinish();
            String msg = page.readFeedback();
            Assert.assertFalse(msg.toLowerCase().contains("thành công") || msg.toLowerCase().contains("success"),
                    "Không được tạo phiếu khi số lượng = 0. Msg=" + msg);
        } else {
            Assert.assertTrue(true);
        }
    }

    @Test(priority = 12, description = "PIS-6-TC-24: Số lượng âm => DISABLED hoặc báo lỗi")
    public void TC24_QuantityNegative_ShouldNotAllowFinish() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        page.selectDepartment(DEPARTMENT_NAME);
        page.setNotes("Auto PIS6 - qty negative");
        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);
        page.waitRowAutoFilled(1, 15);

        page.setRowQuantity(1, "-1");
        blurToTriggerValidation();

        WebElement btn = finishButtonEl();
        if (btn.isEnabled() && !isDisabledAttr(btn)) {
            page.clickFinish();
            String msg = page.readFeedback();
            Assert.assertFalse(msg.toLowerCase().contains("thành công") || msg.toLowerCase().contains("success"),
                    "Không được tạo phiếu khi số lượng âm. Msg=" + msg);
        } else {
            Assert.assertTrue(true);
        }
    }

    @Test(priority = 13, description = "PIS-6-TC-11: Không cho phép xuất quá tồn (Check Banner & Disabled Button)")
    public void TC11_Validate_OverStock() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        page.setNotes("Phiếu xuất tự động - PIS6 Overstock Check");
        page.selectDepartment(DEPARTMENT_NAME);
        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);

        page.setRowQuantity(1, String.valueOf(STOCK_OVER_QTY));
        driver.findElement(By.id("notes")).click();

        try { Thread.sleep(1500); } catch (InterruptedException e) {}

        WebElement alertBanner = null;
        try {
            alertBanner = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@role='alert']")));
        } catch (Exception e) {
            Assert.fail("FAIL: Không hiển thị banner cảnh báo (màu vàng) khi nhập quá tồn kho!");
        }

        String actualMsg = alertBanner.getText();
        log("Nội dung cảnh báo bắt được: " + actualMsg);

        boolean isMsgCorrect = actualMsg.toLowerCase().contains("vượt quá") ||
                actualMsg.toLowerCase().contains("tồn kho");
        Assert.assertTrue(isMsgCorrect, "Nội dung cảnh báo không đúng ngữ cảnh! Actual: " + actualMsg);

        WebElement btnFinish = driver.findElement(By.xpath("//button[contains(.,'Hoàn thành phiếu xuất')]"));
        boolean isEnabled = btnFinish.isEnabled();
        log("Trạng thái nút Hoàn thành (Enabled?): " + isEnabled);

        Assert.assertFalse(isEnabled, "LỖI: Nút Hoàn thành vẫn sáng (bấm được) dù đang xuất quá tồn kho!");
        log("PASS: Hệ thống đã hiện cảnh báo và khóa nút Hoàn thành đúng yêu cầu.");
    }

    @Test(priority = 14, description = "PIS-6-TC-19: Làm mới - Xóa sạch dữ liệu")
    public void TC19_Refresh_ClearData() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        page.selectDepartment(DEPARTMENT_NAME);
        page.setNotes("Dữ liệu này sẽ bị xóa sau khi nhấn nút Làm mới");
        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);
        page.setRowQuantity(1, "10");

        page.clickRefresh();

        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}

        WebElement noteArea = driver.findElement(By.id("notes"));
        Assert.assertEquals(noteArea.getAttribute("value"), "", "Ô Ghi chú chưa được xóa!");

        long totalAfter = parseCurrency(page.getTotalAmountText());
        Assert.assertEquals(totalAfter, 0, "Tổng tiền chưa reset về 0!");

        By drugInputLocator = By.xpath("//input[@placeholder='Nhập sản phẩm...']");
        java.util.List<WebElement> inputs = driver.findElements(drugInputLocator);

        if (inputs.size() == 0) {
            log("PASS: Bảng thuốc đã được xóa sạch (không còn dòng nào).");
        } else {
            String value = inputs.get(0).getAttribute("value");
            Assert.assertEquals(value, "", "Ô Tên thuốc vẫn còn dữ liệu!");
            log("PASS: Bảng thuốc đã reset về dòng trắng mặc định.");
        }
    }

    @Test(priority = 15, description = "PIS-6-TC-22: Ghi chú ký tự đặc biệt vẫn cho lưu thành công")
    public void TC22_Notes_SpecialChars_CreateSuccess() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        page.selectDepartment(DEPARTMENT_NAME);
        page.setNotes("@#$%^&*()_+{}[]<>?/\\|~` - PIS6 Special");
        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);
        page.waitRowAutoFilled(1, 20);
        page.setRowQuantity(1, String.valueOf(STOCK_SAFE_QTY));
        blurToTriggerValidation();

        page.waitUntilFinishEnabled(25);
        page.clickFinish();
        waitDialogVisible(15);

        String title = dialogTitle().toLowerCase();
        Assert.assertTrue(title.contains("thành công") || title.contains("success"),
                "Không thấy popup thành công. Title=" + dialogTitle());
    }

    @Test(priority = 16, description = "PIS-6-TC-25: Popup thành công có đủ thông tin cơ bản (Khoa/Phòng + Tổng tiền)")
    public void TC25_Popup_Content_BasicInfo() {
        createSuccess_OneRow();

        String txt = dialogText();
        Assert.assertTrue(txt.contains(DEPARTMENT_NAME) || txt.toLowerCase().contains("khoa"),
                "Popup không thấy Khoa/Phòng. Text=" + txt);

        boolean hasCurrency = txt.contains("₫") || txt.contains("đ") || txt.replaceAll("[^0-9]", "").length() > 0;
        Assert.assertTrue(hasCurrency, "Popup không thấy tổng tiền/đơn vị tiền tệ. Text=" + txt);
    }

    @Test(priority = 17, description = "PIS-6-TC-26: Nút Đóng popup => quay về màn Tạo phiếu xuất kho")
    public void TC26_Popup_Close_ReturnToForm() {
        createSuccess_OneRow();

        clickDialogClose();

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[@role='dialog']")));

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(By.id("notes")));

        String title = driver.findElement(By.xpath("//h1")).getText().trim();
        Assert.assertTrue(
                title.contains("Tạo phiếu xuất kho") || title.contains("Xuất kho"),
                "Đóng popup xong không quay lại trang Tạo phiếu xuất kho! Title=[" + title + "]"
        );
    }

    @Test(priority = 18, description = "PIS-6-TC-27: Nút Tạo Phiếu Mới => reset form (Ghi chú rỗng, tổng = 0)")
    public void TC27_Popup_CreateNew_ResetForm() {
        createSuccess_OneRow();

        clickDialogCreateNew();

        try { Thread.sleep(800); } catch (InterruptedException ignored) {}

        WebElement noteArea = driver.findElement(By.id("notes"));
        Assert.assertEquals(noteArea.getAttribute("value"), "", "Ô Ghi chú chưa reset sau Tạo Phiếu Mới!");

        ExportReceiptPage page = new ExportReceiptPage(driver);
        long total = parseCurrency(page.getTotalAmountText());
        Assert.assertEquals(total, 0, "Tổng tiền chưa reset về 0 sau Tạo Phiếu Mới!");
    }




}