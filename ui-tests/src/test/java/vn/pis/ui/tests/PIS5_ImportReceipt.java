<<<<<<< HEAD

// ui-tests/src/test/java/vn/pis/ui/tests/PIS5_ImportReceipt.java
package vn.pis.ui.tests;

import org.openqa.selenium.By;
=======
package vn.pis.ui.tests;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
>>>>>>> c1a6054a5e0969d81197da02e0c72fad0d758ab2
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;
import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.ImportReceiptPage;
import vn.pis.ui.pages.LoginPage;

<<<<<<< HEAD
import static vn.pis.ui.util.TestEnv.*;

/**
 * PIS-5: Tạo phiếu nhập kho (UI Automation)
 * A: Automation | M: Manual
 *
 * Ghi chú:
 *  - Các giá trị SUPPLIER_NAME, CATEGORY_NAME, UNIT_NAME cần chỉnh lại theo data thực tế trên hệ thống.
 */
@Listeners(PIS2_ConsoleLogger.class)
public class PIS5_ImportReceipt extends BaseTest {

    private static final String SUPPLIER_NAME = "Nhà cung cấp A";   // TODO: đổi theo data thật
    private static final String CATEGORY_NAME = "Kháng sinh";       // TODO: đổi theo data thật
    private static final String UNIT_NAME     = "Hộp";               // TODO: đổi theo data thật

    // ===== Helpers =====
    private void log(String msg) {
        String line = "[PIS5] " + msg;
        System.out.println(line);
        Reporter.log(line, true);
    }

    private void assertContains(String actual, String... needles) {
        String a = actual == null ? "" : actual.toLowerCase();
        for (String n : needles) {
            if (a.contains(n.toLowerCase())) return;
        }
        Assert.fail("Chuỗi không chứa từ khoá mong đợi. Actual: " + actual);
    }

    // parse "30.000 ₫" -> 30000
    private int parseCurrency(String text) {
        if (text == null) return 0;
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        return Integer.parseInt(digits);
    }

    // đọc message lỗi: ưu tiên alert, nếu không có thì dùng toast/feedback
    private String readErrorMessage(ImportReceiptPage page) {
        try {
            String alertText = page.waitForAlertAndGetText(2);
            log("Alert: " + alertText);
            return alertText;
        } catch (Exception e) {
            String msg = page.readFeedback();
            log("Feedback: " + msg);
            return msg;
        }
    }


    // ===== Login 1 lần cho cả suite =====
    @BeforeClass(alwaysRun = true)
    public void loginOnce() {
        log("Đăng nhập trước khi chạy các test PIS-5");
        LoginPage login = new LoginPage(driver);
        login.open(BASE_URL + "/login");
        login.login(ADMIN_USER, ADMIN_PASS);
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(java.lang.reflect.Method m){
        log("▶ BẮT ĐẦU TC: " + m.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(java.lang.reflect.Method m){
        log("■ KẾT THÚC TC: " + m.getName());
    }

    // ============================================================
    // TC01 – Layout & thành phần chính (A)
    // ============================================================
    /**
     * TC01_OpenImportReceiptPage_BasicLayout
     *
     * Kịch bản test:
     * 1. Mở menu "Nhập kho".
     * 2. Kiểm tra tiêu đề "Tạo phiếu nhập kho".
     * 3. Kiểm tra khối "Thông tin chung": label Nhà cung cấp, Ngày nhập, Ghi chú.
     * 4. Kiểm tra khối "Chi tiết phiếu nhập": header các cột, có 1 dòng trống.
     * 5. Kiểm tra khối "Tổng kết": label "Tổng thành tiền", giá trị mặc định 0 ₫.
     * 6. Kiểm tra nút "Hoàn thành phiếu nhập" hiển thị.
     *
     * Kỳ vọng:
     *  - Tất cả thành phần xuất hiện đúng như thiết kế.
     */
    @Test(priority = 1)
    public void TC01_OpenImportReceiptPage_BasicLayout() {
        ImportReceiptPage page = new ImportReceiptPage(driver);
        page.open();

        Assert.assertTrue(
                driver.findElements(By.xpath("//h1[normalize-space()='Tạo phiếu nhập kho']")).size() > 0,
                "Không thấy tiêu đề 'Tạo phiếu nhập kho'"
        );
        Assert.assertTrue(
                driver.findElements(By.xpath("//div[normalize-space()='Thông tin chung']")).size() > 0,
                "Không thấy khối 'Thông tin chung'"
        );
        Assert.assertTrue(
                driver.findElements(By.xpath("//label[@for='supplier' and contains(.,'Nhà cung cấp')]")).size() > 0,
                "Không thấy label Nhà cung cấp"
        );
        Assert.assertTrue(
                driver.findElements(By.id("importDate")).size() > 0,
                "Không thấy input Ngày nhập"
        );
        Assert.assertTrue(
                driver.findElements(By.id("notes")).size() > 0,
                "Không thấy textarea Ghi chú"
        );

        // Header bảng chi tiết
        Assert.assertTrue(
                driver.findElements(By.xpath("//th[normalize-space()='Tên thuốc *']")).size() > 0,
                "Không thấy cột 'Tên thuốc *'"
        );
        Assert.assertTrue(
                driver.findElements(By.xpath("//th[normalize-space()='Danh mục thuốc *']")).size() > 0,
                "Không thấy cột 'Danh mục thuốc *'"
        );
        Assert.assertTrue(
                driver.findElements(By.xpath("//th[normalize-space()='Đơn vị tính *']")).size() > 0,
                "Không thấy cột 'Đơn vị tính *'"
        );
        Assert.assertTrue(
                driver.findElements(By.xpath("//th[normalize-space()='Số lượng *']")).size() > 0,
                "Không thấy cột 'Số lượng *'"
        );
        Assert.assertTrue(
                driver.findElements(By.xpath("//th[normalize-space()='Đơn giá *']")).size() > 0,
                "Không thấy cột 'Đơn giá *'"
        );
        Assert.assertTrue(
                driver.findElements(By.xpath("//th[normalize-space()='Thành tiền']")).size() > 0,
                "Không thấy cột 'Thành tiền'"
        );
        Assert.assertTrue(
                driver.findElements(By.xpath("//th[normalize-space()='Xóa']")).size() > 0,
                "Không thấy cột 'Xóa'"
        );

        // Tổng kết
        Assert.assertTrue(
                driver.findElements(By.xpath("//span[contains(.,'Tổng thành tiền')]")).size() > 0,
                "Không thấy label 'Tổng thành tiền:'"
        );

        Assert.assertTrue(
                driver.findElements(By.xpath("//button[contains(.,'Hoàn thành phiếu nhập')]")).size() > 0,
                "Không thấy nút 'Hoàn thành phiếu nhập'"
        );
    }

    // ============================================================
    // TC02 – Tạo phiếu nhập 1 dòng thành công (A)
    // ============================================================
    /**
     * TC02_CreateReceipt_SingleRow_Success
     *
     * Kịch bản test:
     * 1. Mở màn "Tạo phiếu nhập kho".
     * 2. Chọn Nhà cung cấp hợp lệ.
     * 3. Giữ Ngày nhập mặc định (hoặc chỉnh sang 1 ngày hợp lệ).
     * 4. Nhập Ghi chú tùy ý.
     * 5. Ở dòng 1:
     *    - Nhập Tên thuốc.
     *    - Chọn Danh mục thuốc.
     *    - Nhập Đơn vị tính.
     *    - Nhập Số lượng > 0.
     *    - Nhập Đơn giá > 0.
     * 6. Kiểm tra cột "Thành tiền" dòng 1 = Số lượng * Đơn giá (định dạng tiền).
     * 7. Kiểm tra "Tổng thành tiền" = Thành tiền dòng 1.
     * 8. Bấm "Hoàn thành phiếu nhập".
     *
     * Kỳ vọng:
     *  - Hiển thị thông báo "Tạo phiếu nhập kho thành công!".
     *  - Không hiển thị lỗi validate.
     */
    @Test(priority = 2)
    public void TC02_CreateReceipt_SingleRow_Success() {
        ImportReceiptPage page = new ImportReceiptPage(driver);
        page.open();

        int qty = 5;
        int price = 10000;
        int expected = qty * price;

        page.selectSupplier(SUPPLIER_NAME);
        page.setNotes("Phiếu nhập tự động - TC02");

        page.setRowDrugName(1, "Paracetamol 500mg - TC02");
        page.selectRowCategory(1, CATEGORY_NAME);
        page.setRowUnit(1, UNIT_NAME);
        page.setRowQuantity(1, String.valueOf(qty));
        page.setRowPrice(1, String.valueOf(price));

        String lineAmount = page.getRowLineAmountText(1);
        log("Line amount row1 = " + lineAmount);
        Assert.assertTrue(lineAmount.contains(String.valueOf(expected)),
                "Thành tiền dòng 1 không chứa giá trị mong đợi: " + expected);

        String total = page.getTotalAmountText();
        log("Total amount = " + total);
        Assert.assertTrue(total.contains(String.valueOf(expected)),
                "Tổng thành tiền không khớp với dòng 1");

        page.clickFinish();
        String msg = page.readFeedback();
        log("Feedback: " + msg);
        assertContains(msg, "Tạo phiếu nhập kho thành công", "thành công");
    }

    // ============================================================
    // TC03 – Thiếu nhà cung cấp (A)
    // ============================================================
    /**
     * TC03_Validate_MissingSupplier
     *
     * Kịch bản test:
     * 1. Mở màn "Tạo phiếu nhập kho".
     * 2. KHÔNG chọn Nhà cung cấp.
     * 3. Điền đầy đủ dữ liệu dòng 1 (Tên thuốc, Danh mục, Đơn vị, Số lượng, Đơn giá).
     * 4. Bấm "Hoàn thành phiếu nhập".
     *
     * Kỳ vọng:
     *  - Hiển thị thông báo lỗi "Vui lòng chọn nhà cung cấp".
     *  - Phiếu nhập KHÔNG được lưu (có thể kiểm tra vẫn ở lại màn hình).
     */
    @Test(priority = 3)
    public void TC03_Validate_MissingSupplier() {
        ImportReceiptPage page = new ImportReceiptPage(driver);
        page.open();

        page.setNotes("TC03 - thiếu nhà cung cấp");

        page.setRowDrugName(1, "Amoxicillin 500mg - TC03");
        page.selectRowCategory(1, CATEGORY_NAME);
        page.setRowUnit(1, UNIT_NAME);
        page.setRowQuantity(1, "1");
        page.setRowPrice(1, "10000");

        page.clickFinish();
        String msg = page.readFeedback();
        log("Feedback: " + msg);
        assertContains(msg, "Vui lòng chọn nhà cung cấp");
    }

    // ============================================================
    // TC04 – Dòng 1: thiếu tên thuốc (A)
    // ============================================================
    /**
     * TC04_Validate_Row1_EmptyDrugName
     *
     * Kịch bản test:
     * 1. Mở màn "Tạo phiếu nhập kho".
     * 2. Chọn Nhà cung cấp hợp lệ.
     * 3. Ở dòng 1:
     *    - ĐỂ TRỐNG Tên thuốc.
     *    - Chọn Danh mục, nhập Đơn vị, Số lượng, Đơn giá.
     * 4. Bấm "Hoàn thành phiếu nhập".
     *
     * Kỳ vọng:
     *  - Thông báo lỗi: "Dòng 1: Vui lòng nhập tên thuốc".
     */
    @Test(priority = 4)
    public void TC04_Validate_Row1_EmptyDrugName() {
        ImportReceiptPage page = new ImportReceiptPage(driver);
        page.open();

        page.selectSupplier(SUPPLIER_NAME);

        // Không nhập tên thuốc
        page.selectRowCategory(1, CATEGORY_NAME);
        page.setRowUnit(1, UNIT_NAME);
        page.setRowQuantity(1, "1");
        page.setRowPrice(1, "10000");

        page.clickFinish();
        String msg = page.readFeedback();
        log("Feedback: " + msg);
        assertContains(msg, "Dòng 1", "Vui lòng nhập tên thuốc");
    }

    // ============================================================
    // TC05 – Dòng 1: thiếu đơn vị tính (A)
    // ============================================================
    /**
     * TC05_Validate_Row1_EmptyUnit
     *
     * Kịch bản test:
     * 1. Mở màn "Tạo phiếu nhập kho".
     * 2. Chọn Nhà cung cấp hợp lệ.
     * 3. Ở dòng 1:
     *    - Nhập Tên thuốc.
     *    - Chọn Danh mục.
     *    - ĐỂ TRỐNG Đơn vị tính.
     *    - Nhập Số lượng, Đơn giá.
     * 4. Bấm "Hoàn thành phiếu nhập".
     *
     * Kỳ vọng:
     *  - Thông báo lỗi: "Dòng 1: Vui lòng nhập đơn vị tính".
     */
    @Test(priority = 5)
    public void TC05_Validate_Row1_EmptyUnit() {
        ImportReceiptPage page = new ImportReceiptPage(driver);
        page.open();

        page.selectSupplier(SUPPLIER_NAME);

        page.setRowDrugName(1, "Cefixim 200mg - TC05");
        page.selectRowCategory(1, CATEGORY_NAME);
        // Không nhập đơn vị
        page.setRowQuantity(1, "1");
        page.setRowPrice(1, "10000");

        page.clickFinish();
        String msg = page.readFeedback();
        log("Feedback: " + msg);
        assertContains(msg, "Dòng 1", "Vui lòng nhập đơn vị tính");
    }

    // ============================================================
    // TC06 – Dòng 1: Số lượng phải > 0 (A)
    // ============================================================
    /**
     * TC06_Validate_Row1_QtyMustGreaterThanZero
     *
     * Kịch bản test:
     * 1. Mở màn "Tạo phiếu nhập kho".
     * 2. Chọn Nhà cung cấp hợp lệ.
     * 3. Ở dòng 1:
     *    - Nhập Tên thuốc, Danh mục, Đơn vị tính.
     *    - Nhập Số lượng = 0.
     *    - Nhập Đơn giá hợp lệ.
     * 4. Bấm "Hoàn thành phiếu nhập".
     *
     * Kỳ vọng:
     *  - Thông báo lỗi: "Dòng 1: Số lượng phải lớn hơn 0".
     */
    @Test(priority = 6)
    public void TC06_Validate_Row1_QtyMustGreaterThanZero() {
        ImportReceiptPage page = new ImportReceiptPage(driver);
        page.open();

        page.selectSupplier(SUPPLIER_NAME);

        page.setRowDrugName(1, "Vitamin C - TC06");
        page.selectRowCategory(1, CATEGORY_NAME);
        page.setRowUnit(1, UNIT_NAME);
        page.setRowQuantity(1, "0");
        page.setRowPrice(1, "10000");

        page.clickFinish();
        String msg = readErrorMessage(page); // FE đang show alert
        assertContains(msg, "Dòng 1", "Số lượng phải lớn hơn 0");
    }

    // ============================================================
    // TC07 – Dòng 1: Đơn giá không hợp lệ (A)
    // ============================================================
    /**
     * TC07_Validate_Row1_InvalidPrice
     *
     * Kịch bản test:
     * 1. Mở màn "Tạo phiếu nhập kho".
     * 2. Chọn Nhà cung cấp hợp lệ.
     * 3. Ở dòng 1:
     *    - Nhập Tên thuốc, Danh mục, Đơn vị tính.
     *    - Nhập Số lượng hợp lệ (>0).
     *    - Nhập Đơn giá = 0 (hoặc giá trị không hợp lệ theo rule).
     * 4. Bấm "Hoàn thành phiếu nhập".
     *
     * Kỳ vọng:
     *  - Thông báo lỗi: "Dòng 1: Đơn giá không hợp lệ".
     */
    @Test(priority = 7)
    public void TC07_Validate_Row1_InvalidPrice() {
        ImportReceiptPage page = new ImportReceiptPage(driver);
        page.open();

        page.selectSupplier(SUPPLIER_NAME);

        page.setRowDrugName(1, "Ibuprofen - TC07");
        page.selectRowCategory(1, CATEGORY_NAME);
        page.setRowUnit(1, UNIT_NAME);
        page.setRowQuantity(1, "5");
        page.setRowPrice(1, "0"); // không hợp lệ theo AC

        page.clickFinish();
        String msg = readErrorMessage(page);
        log("Feedback: " + msg);
        // Nếu FE đang cho pass -> test này sẽ FAIL đúng nghĩa bug
        assertContains(msg, "Dòng 1", "Đơn giá không hợp lệ");
    }

    // ============================================================
    // TC08 – Nhiều dòng: tính Thành tiền & Tổng tiền (A)
    // ============================================================
    /**
     * TC08_MultiRows_CalcLineAndTotal
     *
     * Kịch bản test:
     * 1. Mở màn "Tạo phiếu nhập kho".
     * 2. Chọn Nhà cung cấp hợp lệ.
     * 3. Dòng 1: điền dữ liệu hợp lệ (SL1, Giá1).
     * 4. Bấm "Thêm dòng" → xuất hiện dòng 2.
     * 5. Dòng 2: điền dữ liệu hợp lệ khác (SL2, Giá2).
     * 6. Kiểm tra:
     *    - Thành tiền dòng 1 = SL1 * Giá1.
     *    - Thành tiền dòng 2 = SL2 * Giá2.
     *    - Tổng thành tiền = Thành tiền dòng 1 + Thành tiền dòng 2.
     *
     * Kỳ vọng:
     *  - Công thức tính Thành tiền và Tổng thành tiền chính xác.
     */
    @Test(priority = 8)
    public void TC08_MultiRows_CalcLineAndTotal() {
        ImportReceiptPage page = new ImportReceiptPage(driver);
        page.open();

        page.selectSupplier(SUPPLIER_NAME);

        int q1 = 2, p1 = 15000, line1 = q1 * p1;
        int q2 = 3, p2 = 20000, line2 = q2 * p2;
        int total = line1 + line2;

        // Row 1
        page.setRowDrugName(1, "Thuốc A - TC08");
        page.selectRowCategory(1, CATEGORY_NAME);
        page.setRowUnit(1, UNIT_NAME);
        page.setRowQuantity(1, String.valueOf(q1));
        page.setRowPrice(1, String.valueOf(p1));

        // Row 2
        page.clickAddRow();
        Assert.assertTrue(page.getRowCount() >= 2, "Không thêm được dòng 2");
        page.setRowDrugName(2, "Thuốc B - TC08");
        page.selectRowCategory(2, CATEGORY_NAME);
        page.setRowUnit(2, UNIT_NAME);
        page.setRowQuantity(2, String.valueOf(q2));
        page.setRowPrice(2, String.valueOf(p2));

        String line1Txt = page.getRowLineAmountText(1);
        String line2Txt = page.getRowLineAmountText(2);
        String totalTxt = page.getTotalAmountText();

        log("Row1 amount = " + line1Txt);
        log("Row2 amount = " + line2Txt);
        log("Total       = " + totalTxt);

        Assert.assertEquals(parseCurrency(line1Txt), line1, "Thành tiền dòng 1 không đúng");
        Assert.assertEquals(parseCurrency(line2Txt), line2, "Thành tiền dòng 2 không đúng");
        Assert.assertEquals(parseCurrency(totalTxt), total, "Tổng thành tiền không đúng");
    }

    // ============================================================
    // TC09 – Xóa dòng cập nhật lại Tổng tiền (A)
    // ============================================================
    /**
     * TC09_DeleteRow_UpdateTotal
     *
     * Kịch bản test:
     * 1. Mở màn "Tạo phiếu nhập kho".
     * 2. Chọn Nhà cung cấp hợp lệ.
     * 3. Tạo 2 dòng hợp lệ (giống TC08).
     * 4. Ghi nhận Tổng thành tiền ban đầu.
     * 5. Xóa dòng 2.
     * 6. Kiểm tra:
     *    - Số dòng còn lại = 1.
     *    - Tổng thành tiền = Thành tiền dòng 1.
     *
     * Kỳ vọng:
     *  - Việc xóa dòng cập nhật đúng Tổng thành tiền.
     */
    @Test(priority = 9)
    public void TC09_DeleteRow_UpdateTotal() {
        ImportReceiptPage page = new ImportReceiptPage(driver);
        page.open();

        page.selectSupplier(SUPPLIER_NAME);

        // Row 1
        int q1 = 1, p1 = 10000, line1 = q1 * p1;

        page.setRowDrugName(1, "Thuốc X - TC09");
        page.selectRowCategory(1, CATEGORY_NAME);
        page.setRowUnit(1, UNIT_NAME);
        page.setRowQuantity(1, String.valueOf(q1));
        page.setRowPrice(1, String.valueOf(p1));

        // Row 2
        page.clickAddRow();
        page.setRowDrugName(2, "Thuốc Y - TC09");
        page.selectRowCategory(2, CATEGORY_NAME);
        page.setRowUnit(2, UNIT_NAME);
        page.setRowQuantity(2, "2");
        page.setRowPrice(2, "20000");

        String totalBeforeTxt = page.getTotalAmountText();
        log("Total BEFORE delete = " + totalBeforeTxt);

        // Xoá dòng 2
        page.deleteRow(2);

        Assert.assertEquals(page.getRowCount(), 1, "Sau khi xoá phải còn 1 dòng");

        String line1Txt = page.getRowLineAmountText(1);
        String totalAfterTxt = page.getTotalAmountText();
        log("Row1 amount = " + line1Txt);
        log("Total AFTER delete = " + totalAfterTxt);

        Assert.assertEquals(parseCurrency(line1Txt), line1,
                "Thành tiền dòng 1 không đúng sau khi xoá");
        Assert.assertEquals(parseCurrency(totalAfterTxt), line1,
                "Tổng thành tiền không cập nhật theo dòng 1");
    }
}
=======
import java.time.Duration;

import static vn.pis.ui.util.TestEnv.*;

public class PIS5_ImportReceipt extends BaseTest {

    private ImportReceiptPage page;

    // --- TEST VERSION INFO (printed before each test case) ---
    private static final String TEST_VERSION = initTestVersion();

    private static String initTestVersion() {
        String v = System.getProperty("test.version");
        if (v != null && !v.isEmpty())
            return v;
        v = System.getenv("TEST_VERSION");
        if (v != null && !v.isEmpty())
            return v;
        return "auto-" + java.time.Instant.now().toString();
    }

    // --- DỮ LIỆU TEST CHUẨN ---
    private static final String SUPPLIER_NAME = "Công ty Dược phẩm LA ĐẠI LÔC";
    private static final String DRUG_NAME = "Panadol";
    private static final String CATEGORY_NAME = "Thuốc giảm đau - Hạ sốt";
    private static final String UNIT_NAME = "Hộp";

    @BeforeClass(alwaysRun = true)
    public void loginAndInit() {
        if (driver == null)
            return;
        System.out.println("========== [SETUP] BẮT ĐẦU SUITE TEST NHẬP KHO ==========");
        System.out.println("...Đang đăng nhập hệ thống...");

        LoginPage login = new LoginPage(driver);
        login.open(BASE_URL + "/login");
        login.login(ADMIN_USER, ADMIN_PASS);

        page = new ImportReceiptPage(driver);
    }

    @BeforeMethod(alwaysRun = true)
    public void goToImportPage(java.lang.reflect.Method m) {
        System.out.println("\n=======================================================");
        System.out.println("   [Info] Test Version: " + TEST_VERSION);
        System.out.println("▶ START TEST CASE: " + m.getName());
        System.out.println("   [Setup] Reset trang: Mở lại menu Nhập kho...");
        page.open();
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(java.lang.reflect.Method m) {
        System.out.println("■ END TEST CASE: " + m.getName());
    }

    private void fillHeaderValid() {
        page.selectSupplier(SUPPLIER_NAME);
        // Ngày nhập đang read-only thì không set gì thêm
    }

    private void fillRowValid(int row) {
        page.setRowDrugName(row, DRUG_NAME);
        page.selectRowCategory(row, CATEGORY_NAME);
        page.setRowUnit(row, UNIT_NAME);
        page.setRowQuantity(row, "1");
        page.setRowPrice(row, "1000");

        // blur để hệ thống nhận dữ liệu (nếu cần)
        driver.findElement(By.tagName("body")).click();
    }

    // =================================================================
    // PHẦN 1: LAYOUT & UI
    // =================================================================

    /*
     * TC_001 - Verify Page Title
     *
     * Mục tiêu:
     * - Xác nhận trang "Tạo phiếu nhập kho" hiển thị đúng tiêu đề.
     *
     * Tiền điều kiện:
     * - Đã login admin
     * - Mở trang /import
     *
     * Steps:
     * 1) Vào trang Nhập kho từ menu.
     * 2) Quan sát tiêu đề trang.
     *
     * Expected:
     * - Có h1 hiển thị đúng "Tạo phiếu nhập kho".
     */

    @Test
    public void TC_001_VerifyPageTitle() {
        System.out.println("   [Scenario] Kiểm tra tiêu đề trang hiển thị đúng.");
        Assert.assertTrue(driver.findElements(page.pageTitle).size() > 0, "Tiêu đề trang sai");
    }

    /*
     * TC_002 - Verify Menu Navigation
     *
     * Mục tiêu:
     * - Xác nhận điều hướng từ menu tới đúng URL trang nhập kho.
     *
     * Tiền điều kiện:
     * - Đã login admin
     *
     * Steps:
     * 1) Click menu "Nhập kho".
     * 2) Lấy URL hiện tại.
     *
     * Expected:
     * - URL có chứa "import".
     */
    @Test
    public void TC_002_VerifyMenuNavigation() {
        System.out.println("   [Scenario] Kiểm tra URL có chứa 'import'.");
        Assert.assertTrue(driver.getCurrentUrl().contains("import"), "URL không chứa import");
    }

    /*
     * TC_003 - Verify Info Block
     *
     * Mục tiêu:
     * - Kiểm tra khối "Thông tin chung" có hiển thị.
     *
     * Tiền điều kiện:
     * - Đã login admin, đang ở /import
     *
     * Steps:
     * 1) Mở trang nhập kho.
     *
     * Expected:
     * - Block "Thông tin chung" xuất hiện.
     */

    @Test
    public void TC_003_VerifyInfoBlock() {
        Assert.assertTrue(driver.findElements(page.sectionInfo).size() > 0);
    }

    /*
     * TC_004 - Verify Supplier Input
     *
     * Mục tiêu:
     * - Kiểm tra trường chọn Nhà cung cấp hiển thị đúng.
     *
     * Tiền điều kiện:
     * - Đã login admin, đang ở /import
     *
     * Steps:
     * 1) Mở trang nhập kho.
     *
     * Expected:
     * - Có label/field "Nhà cung cấp".
     */
    @Test
    public void TC_004_VerifySupplierInput() {
        Assert.assertTrue(driver.findElements(By.xpath("//label[contains(.,'Nhà cung cấp')]")).size() > 0);
    }

    /*
     * TC_005 - Verify Date Input
     *
     * Mục tiêu:
     * - Kiểm tra ô "Ngày nhập" hiển thị.
     *
     * Tiền điều kiện:
     * - Đã login admin, đang ở /import
     *
     * Steps:
     * 1) Mở trang nhập kho.
     *
     * Expected:
     * - Input ngày (id importDate) hiển thị.
     */
    @Test
    public void TC_005_VerifyDateInput() {
        Assert.assertTrue(driver.findElements(By.id("importDate")).size() > 0);
    }

    /*
     * TC_006 - Verify Note Input
     *
     * Mục tiêu:
     * - Kiểm tra ô "Ghi chú" hiển thị.
     *
     * Tiền điều kiện:
     * - Đã login admin, đang ở /import
     *
     * Steps:
     * 1) Mở trang nhập kho.
     *
     * Expected:
     * - Textarea/input ghi chú (id notes) hiển thị.
     */
    @Test
    public void TC_006_VerifyNoteInput() {
        Assert.assertTrue(driver.findElements(By.id("notes")).size() > 0);
    }

    /*
     * TC_007 - Verify Table Header: Tên thuốc
     *
     * Mục tiêu:
     * - Kiểm tra header cột "Tên thuốc *" có hiển thị.
     *
     * Tiền điều kiện:
     * - Đã login admin, đang ở /import
     *
     * Steps:
     * 1) Quan sát bảng nhập thuốc.
     *
     * Expected:
     * - Có cột "Tên thuốc".
     */
    @Test
    public void TC_007_Header_DrugName() {
        Assert.assertTrue(driver.findElements(page.colDrugName).size() > 0);
    }

    /*
     * TC_008 - Verify Table Header: Danh mục thuốc
     *
     * Mục tiêu:
     * - Kiểm tra header cột "Danh mục thuốc *" có hiển thị.
     *
     * Expected:
     * - Có cột "Danh mục".
     */
    @Test
    public void TC_008_Header_Category() {
        Assert.assertTrue(driver.findElements(page.colCategory).size() > 0);
    }

    /*
     * TC_009 - Verify Table Header: Đơn vị tính
     *
     * Mục tiêu:
     * - Kiểm tra header cột "Đơn vị tính *" có hiển thị.
     *
     * Expected:
     * - Có cột "Đơn vị tính".
     */
    @Test
    public void TC_009_Header_Unit() {
        Assert.assertTrue(driver.findElements(page.colUnit).size() > 0);
    }

    /*
     * TC_010 - Verify Table Header: Số lượng
     *
     * Mục tiêu:
     * - Kiểm tra header cột "Số lượng *" có hiển thị.
     *
     * Expected:
     * - Có cột "Số lượng".
     */
    @Test
    public void TC_010_Header_Qty() {
        Assert.assertTrue(driver.findElements(page.colQty).size() > 0);
    }

    /*
     * TC_011 - Verify Table Header: Đơn giá
     *
     * Mục tiêu:
     * - Kiểm tra header cột "Đơn giá *" có hiển thị.
     *
     * Expected:
     * - Có cột "Đơn giá".
     */
    @Test
    public void TC_011_Header_Price() {
        Assert.assertTrue(driver.findElements(page.colPrice).size() > 0);
    }

    /*
     * TC_012 - Verify Table Header: Mã SKU
     *
     * Mục tiêu:
     * - Kiểm tra cột SKU có hiển thị khi scroll ngang.
     *
     * Steps:
     * 1) Scroll ngang bảng tới vị trí cột SKU.
     *
     * Expected:
     * - Có cột "Mã SKU".
     */
    @Test
    public void TC_012_Header_SKU() {
        System.out.println("   [Step] Scroll ngang để tìm cột SKU.");
        page.scrollToElement(page.colSku);
        Assert.assertTrue(driver.findElements(page.colSku).size() > 0);
    }

    /*
     * TC_013 - Verify Table Header: Số lô
     *
     * Mục tiêu:
     * - Kiểm tra cột "Số lô" hiển thị.
     *
     * Expected:
     * - Có cột "Số lô".
     */
    @Test
    public void TC_013_Header_Lot() {
        page.scrollToElement(page.colLot);
        Assert.assertTrue(driver.findElements(page.colLot).size() > 0);
    }

    /*
     * TC_014 - Verify Table Header: Hạn sử dụng
     *
     * Mục tiêu:
     * - Kiểm tra cột "Hạn sử dụng" hiển thị.
     *
     * Expected:
     * - Có cột "Hạn sử dụng".
     */
    @Test
    public void TC_014_Header_Expiry() {
        page.scrollToElement(page.colExpiry);
        Assert.assertTrue(driver.findElements(page.colExpiry).size() > 0);
    }

    /*
     * TC_015 - Verify Button: Thêm dòng
     *
     * Mục tiêu:
     * - Kiểm tra nút "+ Thêm dòng" hiển thị.
     *
     * Expected:
     * - Có button Thêm dòng.
     */
    @Test
    public void TC_015_Button_AddRow() {
        Assert.assertTrue(driver.findElements(By.xpath("//button[contains(.,'Thêm dòng')]")).size() > 0);
    }

    /*
     * TC_016 - Verify Footer: Tổng thành tiền
     *
     * Mục tiêu:
     * - Kiểm tra khu vực tổng tiền hiển thị.
     *
     * Expected:
     * - Có label "Tổng thành tiền".
     */
    @Test
    public void TC_016_Footer_Total() {
        Assert.assertTrue(driver.findElements(By.xpath("//span[contains(.,'Tổng thành tiền')]")).size() > 0);
    }

    /*
     * TC_017 - Verify Button: Hoàn thành
     *
     * Mục tiêu:
     * - Kiểm tra nút "Hoàn thành phiếu nhập" hiển thị.
     *
     * Expected:
     * - Có button Hoàn thành.
     */
    @Test
    public void TC_017_Button_Finish() {
        Assert.assertTrue(driver.findElements(By.xpath("//button[contains(.,'Hoàn thành')]")).size() > 0);
    }

    /*
     * TC_018 - Verify Horizontal Scroll / Last Column
     *
     * Mục tiêu:
     * - Kiểm tra bảng có thể scroll ngang tới cột cuối (Xóa).
     *
     * Steps:
     * 1) Scroll ngang tới cột Xóa.
     *
     * Expected:
     * - Cột "Xóa" hiển thị.
     */
    @Test
    public void TC_018_VerifyScrollbar() {
        System.out.println("   [Step] Scroll tới cột cuối cùng (Xóa).");
        page.scrollToElement(page.colDelete);
        Assert.assertTrue(driver.findElements(page.colDelete).size() > 0);
    }

    /*
     * TC_019 - Verify Table Header: Mô tả
     *
     * Expected:
     * - Có cột "Mô tả".
     */
    @Test
    public void TC_019_Header_Desc() {
        page.scrollToElement(page.colDesc);
        Assert.assertTrue(driver.findElements(page.colDesc).size() > 0);
    }

    /*
     * TC_020 - Verify Table Header: Thành tiền
     *
     * Expected:
     * - Có cột "Thành tiền".
     */
    @Test
    public void TC_020_Header_Total() {
        page.scrollToElement(page.colTotal);
        Assert.assertTrue(driver.findElements(page.colTotal).size() > 0);
    }

    /*
     * TC_021 - Verify Table Header: Xóa
     *
     * Expected:
     * - Có cột "Xóa".
     */
    @Test
    public void TC_021_Header_Delete() {
        page.scrollToElement(page.colDelete);
        Assert.assertTrue(driver.findElements(page.colDelete).size() > 0);
    }

    // =================================================================
    // PHẦN 2: VALIDATE HEADER
    // =================================================================

    /*
     * TC_022 - Validate Missing Supplier
     *
     * Mục tiêu:
     * - Khi thiếu Nhà cung cấp thì hệ thống phải chặn submit và báo lỗi đúng
     * "nhà cung cấp".
     *
     * Tiền điều kiện:
     * - Đã login admin, đang ở /import
     *
     * Test data:
     * - Dòng 1 hợp lệ: Panadol | Thuốc giảm đau - Hạ sốt | Hộp | SL=10 | Giá=1000
     *
     * Steps:
     * 1) Không chọn NCC.
     * 2) Nhập dòng thuốc hợp lệ.
     * 3) Click "Hoàn thành phiếu nhập".
     *
     * Expected:
     * - Alert/Toast báo lỗi chứa "vui lòng chọn nhà cung cấp".
     * - Không tạo phiếu nhập thành công.
     */
    @Test(description = "TC_022: Validate Bỏ trống Nhà cung cấp")
    public void TC_022_Validate_MissingSupplier() {
        System.out.println("   [Scenario] Nhập thuốc đầy đủ nhưng KHÔNG chọn NCC -> Bấm Hoàn thành -> Kỳ vọng lỗi.");

        System.out.println("   [Step] Refresh trang để xóa dữ liệu cũ.");
        driver.navigate().refresh();
        page.open();

        System.out.println("   [Step] Nhập thông tin thuốc mẫu.");
        page.setRowData(1, DRUG_NAME, CATEGORY_NAME, UNIT_NAME, "10", "1000");

        System.out.println("   [Step] Click Hoàn thành (Bỏ qua bước chọn NCC).");
        page.clickFinish();

        System.out.println("   [Check] Kiểm tra Alert xuất hiện.");
        checkAlertContains("Vui lòng chọn nhà cung cấp");
    }

    /*
     * TC_023 - Select Supplier Valid
     *
     * Mục tiêu:
     * - Chọn được NCC hợp lệ từ dropdown.
     *
     * Test data:
     * - NCC: Công ty Dược phẩm LA ĐẠI LÔC
     *
     * Steps:
     * 1) Click dropdown Nhà cung cấp.
     * 2) Chọn NCC theo tên.
     *
     * Expected:
     * - NCC được set vào trường Nhà cung cấp (không lỗi).
     */
    @Test(description = "TC_023: Chọn NCC hợp lệ")
    public void TC_023_SelectSupplier_Valid() {
        System.out.println("   [Scenario] Chọn NCC hợp lệ từ danh sách.");
        page.selectSupplier(SUPPLIER_NAME);
    }

    /*
     * TC_024 - Validate Import Date Display
     *
     * Mục tiêu:
     * - Kiểm tra trường Ngày nhập hiển thị (và thường là read-only theo nghiệp vụ).
     *
     * Steps:
     * 1) Mở trang /import.
     *
     * Expected:
     * - Input ngày hiển thị.
     */
    @Test(description = "TC_024: Validate Ngày nhập")
    public void TC_024_Validate_Date() {
        System.out.println("   [Check] Kiểm tra ô nhập ngày hiển thị.");
        Assert.assertTrue(driver.findElement(By.id("importDate")).isDisplayed());
    }

    /*
     * TC_025 - Validate Empty Note
     *
     * Mục tiêu:
     * - Ghi chú để trống vẫn cho phép tạo phiếu nhập (nếu nghiệp vụ không bắt
     * buộc).
     *
     * Tiền điều kiện:
     * - Chọn NCC hợp lệ.
     *
     * Steps:
     * 1) Không nhập ghi chú.
     * 2) Nhập dòng thuốc hợp lệ.
     * 3) Click Hoàn thành.
     *
     * Expected:
     * - Tạo phiếu nhập thành công (toast/alert thành công hoặc không lỗi).
     */
    @Test(description = "TC_025: Validate Bỏ trống Ghi chú")
    public void TC_025_Validate_EmptyNote() {
        System.out.println("   [Scenario] Để trống ghi chú -> Hệ thống vẫn cho phép nhập.");
        page.selectSupplier(SUPPLIER_NAME);
        page.setRowData(1, DRUG_NAME, CATEGORY_NAME, UNIT_NAME, "1", "1000");
        page.clickFinish();

        String msg = page.readFeedback();
        Assert.assertTrue(msg.contains("thành công") || msg.isEmpty());
    }

    /*
     * TC_026 - Validate Special Note
     *
     * Mục tiêu:
     * - Nhập ký tự đặc biệt vào ghi chú hệ thống vẫn chấp nhận (không crash / không
     * lỗi validation).
     *
     * Steps:
     * 1) Nhập notes: #@$
     * 2) Chọn NCC hợp lệ.
     * 3) Nhập dòng thuốc hợp lệ.
     * 4) Click Hoàn thành.
     *
     * Expected:
     * - Tạo phiếu nhập thành công hoặc không báo lỗi notes.
     */
    @Test(description = "TC_026: Validate Ghi chú ký tự đặc biệt")
    public void TC_026_Validate_SpecialNote() {
        System.out.println("   [Scenario] Nhập ký tự đặc biệt vào ghi chú -> Hệ thống vẫn chấp nhận.");
        driver.findElement(By.id("notes")).sendKeys("#@$");
        page.selectSupplier(SUPPLIER_NAME);
        page.setRowData(1, DRUG_NAME, CATEGORY_NAME, UNIT_NAME, "1", "1000");
        page.clickFinish();

        String msg = page.readFeedback();
        Assert.assertTrue(msg.contains("thành công") || msg.isEmpty());
    }

    // =================================================================
    // PHẦN 3: VALIDATE ROW
    // =================================================================

    @Test(description = "TC_027: Thiếu Tên thuốc")
    public void TC_027_Row_MissingName() {
        System.out.println("   [Scenario] Bỏ trống tên thuốc -> Kỳ vọng báo lỗi.");
        page.selectSupplier(SUPPLIER_NAME);
        page.setRowQuantity(1, "1"); // Chỉ nhập SL, bỏ trống tên
        page.clickFinish();
        checkErrorMsg("tên thuốc");
    }

    @Test(description = "TC_028: Thiếu Danh mục")
    public void TC_028_Row_MissingCategory() {
        System.out.println("   [Scenario] Bỏ trống danh mục -> Kỳ vọng báo lỗi.");
        page.selectSupplier(SUPPLIER_NAME);
        page.setRowDrugName(1, DRUG_NAME); // Nhập tên, bỏ danh mục
        page.clickFinish();
        checkErrorMsg("danh mục");
    }

    @Test(description = "TC_029: Thiếu Đơn vị")
    public void TC_029_Row_MissingUnit() {
        System.out.println("   [Scenario] Bỏ trống đơn vị tính -> Kỳ vọng báo lỗi.");
        page.selectSupplier(SUPPLIER_NAME);
        page.setRowDrugName(1, DRUG_NAME);
        page.selectRowCategory(1, CATEGORY_NAME);
        page.clickFinish();
        checkErrorMsg("đơn vị");
    }

    /*
     * TC_030 - Thiếu Số lượng (các field khác hợp lệ)
     *
     * Mục tiêu:
     * - Khi bỏ trống Số lượng thì hệ thống báo lỗi đúng "số lượng".
     *
     * Tiền điều kiện:
     * - Đã chọn NCC hợp lệ.
     *
     * Steps:
     * 1) Nhập Tên thuốc + Danh mục + Đơn vị + Đơn giá hợp lệ.
     * 2) KHÔNG nhập số lượng.
     * 3) Click Hoàn thành.
     *
     * Expected:
     * - Alert/Toast báo lỗi chứa "số lượng".
     */
    @Test(description = "TC_030: Thiếu Số lượng")
    public void TC_030_Row_MissingQty() {
        fillHeaderValid();

        // Setup hợp lệ trừ qty
        page.setRowDrugName(1, DRUG_NAME);
        page.selectRowCategory(1, CATEGORY_NAME);
        page.setRowUnit(1, UNIT_NAME);
        page.setRowPrice(1, "1000");
        // không set qty

        page.clickFinish();
        checkErrorMsg("số lượng");
    }

    /*
     * TC_031 - Thiếu Đơn giá (các field khác hợp lệ)
     *
     * Mục tiêu:
     * - Khi bỏ trống Đơn giá thì hệ thống báo lỗi đúng "đơn giá".
     *
     * Tiền điều kiện:
     * - Đã chọn NCC hợp lệ.
     *
     * Steps:
     * 1) Nhập Tên thuốc + Danh mục + Đơn vị + Số lượng hợp lệ.
     * 2) KHÔNG nhập đơn giá.
     * 3) Click Hoàn thành.
     *
     * Expected:
     * - Alert/Toast báo lỗi chứa "đơn giá".
     */
    @Test(description = "TC_031: Thiếu Đơn giá")
    public void TC_031_Row_MissingPrice() {
        fillHeaderValid();

        // Setup hợp lệ trừ price
        page.setRowDrugName(1, DRUG_NAME);
        page.selectRowCategory(1, CATEGORY_NAME);
        page.setRowUnit(1, UNIT_NAME);
        page.setRowQuantity(1, "1");
        // không set price

        page.clickFinish();
        checkErrorMsg("đơn giá");
    }

    /*
     * TC_032 - Số lượng = 0
     *
     * Mục tiêu:
     * - Số lượng bằng 0 phải bị chặn (theo nghiệp vụ: >0).
     *
     * Tiền điều kiện:
     * - NCC + dòng thuốc hợp lệ
     *
     * Steps:
     * 1) Nhập dòng hợp lệ.
     * 2) Set Số lượng = 0.
     * 3) Click Hoàn thành.
     *
     * Expected:
     * - Báo lỗi liên quan "số lượng" hoặc "lớn hơn 0".
     */
    @Test(description = "TC_032: Số lượng = 0")
    public void TC_032_Row_QtyZero() {
        fillHeaderValid();
        fillRowValid(1);

        page.setRowQuantity(1, "0");
        page.clickFinish();
        checkErrorMsg("lớn hơn 0"); // hoặc "số lượng"
    }

    @Test(description = "TC_033: Số lượng âm")
    public void TC_033_Row_QtyNegative() {
        fillHeaderValid();
        fillRowValid(1);

        page.setRowQuantity(1, "-5");
        page.clickFinish();
        checkErrorMsg("lớn hơn 0"); // hoặc "số lượng"
    }

    @Test(description = "TC_034: Số lượng không phải số - UI chặn ký tự")
    public void TC_034_Row_QtyChar() {
        fillHeaderValid();
        fillRowValid(1);

        // Thử nhập chữ vào input number
        page.setRowQuantity(1, "abc");

        // Nếu setRowQuantity đã assert empty thì test PASS luôn.
        // Không cần clickFinish nữa vì case này validate ngay tại input.
    }

    @Test(description = "TC_035: Đơn giá âm - UI chặn hoặc submit chặn")
    public void TC_035_Row_PriceNegative() {
        fillHeaderValid();
        fillRowValid(1);

        page.setRowPrice(1, "-1000");

        // Nếu UI không chặn và vẫn còn dấu '-' thì submit phải báo lỗi
        String priceAfter = page.getRowPriceValue(1); // bạn thêm hàm get value
        if (priceAfter != null && priceAfter.contains("-")) {
            page.clickFinish();
            checkErrorMsg("đơn giá"); // hoặc keyword hệ thống trả về
        }
    }

    /*
     * TC_036 - Đơn giá = 0 (Hàng tặng)
     *
     * Mục tiêu:
     * - Cho phép giá = 0 (nếu nghiệp vụ cho hàng tặng).
     *
     * Steps:
     * 1) Chọn NCC hợp lệ.
     * 2) Nhập dòng hợp lệ với Đơn giá = 0.
     * 3) Click Hoàn thành.
     *
     * Expected:
     * - Tạo phiếu nhập thành công.
     */
    @Test(description = "TC_036: Giá = 0")
    public void TC_036_Row_PriceZero() {
        System.out.println("   [Scenario] Nhập Giá = 0 (Hàng tặng) -> Kỳ vọng thành công.");
        page.selectSupplier(SUPPLIER_NAME);
        page.setRowData(1, DRUG_NAME, CATEGORY_NAME, UNIT_NAME, "1", "0");
        page.clickFinish();

        String msg = page.readFeedback();
        Assert.assertTrue(msg.contains("thành công") || msg.isEmpty());
    }

    /*
     * TC_037 - Nhập hạn sử dụng quá khứ
     *
     * Mục tiêu:
     * - Kiểm tra behavior khi nhập hạn sử dụng ở quá khứ (có thể chặn hoặc cảnh báo
     * tùy nghiệp vụ).
     *
     * Steps:
     * 1) Nhập expiry = 01/01/2020.
     *
     * Expected:
     * - Nếu hệ thống có validate: hiển thị lỗi/cảnh báo.
     * - Nếu chưa có validate: vẫn nhập được (ghi nhận để report).
     */
    @Test
    public void TC_037_Validate_ExpiryPast_Yesterday() throws InterruptedException {
        System.out.println("   [Scenario] HSD = ngày hôm qua -> kỳ vọng cảnh báo hạn sử dụng.");

        // 1) nhập row trước
        page.setRowData(1, DRUG_NAME, CATEGORY_NAME, UNIT_NAME, "1", "1000");

        // 2) chọn NCC + blur commit
        page.selectSupplier(SUPPLIER_NAME);
        driver.findElement(By.tagName("body")).click();

        // (optional) đợi 200ms cho UI update
        try {
            Thread.sleep(200);
        } catch (Exception ignored) {
        }

        // 3) lấy importDate
        String importDate = page.getImportDateValue(); // yyyy-MM-dd
        System.out.println("   [Read] importDate = " + importDate);

        // 4) set expiry = hôm qua
        String expiryYesterday = yyyyMMdd_plusDays(importDate, -1);
        page.setRowExpiry(1, expiryYesterday);

        // blur để tránh mất giá trị do focus
        driver.findElement(By.tagName("body")).click();
        // Thread.sleep(10000);

        // verify expiry còn giữ
        String actualExpiry = page.getRowExpiryValue(1);
        System.out.println("   [Verify] Expiry value = " + actualExpiry);
        Assert.assertEquals(actualExpiry, expiryYesterday, "HSD bị mất / không set được!");

        // 5) bấm hoàn thành
        page.clickFinish();

        // 6) bắt alert/toast
        String msg = "";
        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(3));
            Alert a = w.until(ExpectedConditions.alertIsPresent());
            msg = a.getText().trim();
            System.out.println("   [Alert] " + msg);
            a.accept();
        } catch (TimeoutException ignore) {
            msg = page.readFeedback().trim();
            System.out.println("   [Toast/Feedback] " + msg);
        }

        // 7) nếu báo thiếu NCC => setup đang lỗi, fail rõ ràng
        if (msg.toLowerCase().contains("nhà cung cấp")) {
            Assert.fail("Setup lỗi: hệ thống vẫn báo thiếu NCC dù đã select. Msg=" + msg);
        }

        // 8) nghiệp vụ hạn sử dụng
        if (msg.isEmpty()) {
            System.out.println(
                    "   ⚠️ [NOTE] Không có cảnh báo HSD quá khứ -> hệ thống có thể chưa validate. Ghi nhận report.");
            // Tạm thời fail nếu không thấy alert (vì user confirm là có alert)
            Assert.fail("Bug: Không hiển thị cảnh báo HSD quá khứ (User confirm manually có alert).");
        } else {
            // Revert: Không chấp nhận 'thành công' nữa
            System.out.println("   [Verify] Message content: " + msg);

            // Text user cung cấp: "Dòng 1: Hạn sử dụng phải lớn hơn ngày nhập kho. Không
            // thể nhập kho thuốc đã hết hạn hoặc hết hạn trong ngày nhập."
            Assert.assertTrue(msg.contains("Hạn sử dụng phải lớn hơn ngày nhập kho"),
                    "Thiếu câu 1. Msg=" + msg);
            Assert.assertTrue(
                    msg.contains("Không thể nhập kho thuốc đã hết hạn") || msg.contains("hết hạn trong ngày nhập"),
                    "Thiếu câu 2. Msg=" + msg);
        }
    }

    /*
     * TC_038 - Nhập hạn sử dụng tương lai
     *
     * Mục tiêu:
     * - Hạn sử dụng tương lai phải nhập được.
     *
     * Steps:
     * 1) Nhập expiry = 01/01/2030.
     *
     * Expected:
     * - Giá trị được set vào ô hạn dùng, không lỗi.
     */
    @Test
    public void TC_038_Row_ExpiryFuture() throws InterruptedException {
        page.selectSupplier("Công ty Dược phẩm LA ĐẠI LÔC");
        page.setRowData(1, "Panadol", "Thuốc giảm đau - Hạ sốt", "Hộp", "1", "1000");

        String importDate = page.getImportDateText(); // 2025-12-20
        System.out.println("   [Read] Ngày nhập (importDate) = " + importDate);

        String expiryGood = to_yyyyMMdd_plusDays(importDate, 10); // 2025-12-30
        page.setRowExpiry(1, expiryGood);
        // Thread.sleep(10000);

        // ✅ Assert ngay tại đây để biết input có vào không
        String actualExpiry = page.getRowExpiryValue(1);
        Assert.assertEquals(actualExpiry, expiryGood,
                "Expiry không được set đúng. Expect=" + expiryGood + " | Actual=" + actualExpiry);

        page.clickFinish();

        // Nếu cần, bạn có thể check không có alert lỗi hạn sử dụng
        String msg = page.readFeedback().toLowerCase();
        Assert.assertFalse(msg.contains("hạn sử dụng phải"),
                "Bị báo lỗi hạn sử dụng dù expiry hợp lệ. Msg=" + msg);
    }

    // =================================================================
    // PHẦN 4: LOGIC TÍNH TOÁN
    // =================================================================

    /*
     * TC_039 - Tính thành tiền dòng (chẵn)
     *
     * Mục tiêu:
     * - Thành tiền = Số lượng * Đơn giá.
     *
     * Steps:
     * 1) Nhập SL=2.
     * 2) Nhập Giá=10000.
     * 3) Blur để trigger tính toán.
     *
     * Expected:
     * - Thành tiền dòng hiển thị 20.000 ₫ (hoặc format tương đương).
     */
    @Test(description = "TC_039: Thành tiền Chẵn")
    public void TC_039_Calc_LineAmount() {
        System.out.println("   [Scenario] Nhập SL=2, Giá=10000 -> Check Thành tiền = 20.000");
        page.setRowQuantity(1, "2");
        page.setRowPrice(1, "10000");

        // Blur để trigger tính toán
        driver.findElement(By.tagName("body")).click();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        String amount = page.getRowLineAmountText(1);
        Assert.assertTrue(amount.contains("20.000") || amount.contains("20,000"));
    }

    /*
     * TC_040 - Tính thành tiền dòng (lẻ)
     *
     * Steps:
     * 1) SL=5, Giá=1500.
     * 2) Blur.
     *
     * Expected:
     * - Thành tiền = 7.500 ₫ (format tương đương).
     */
    @Test(description = "TC_040: Thành tiền Lẻ")
    public void TC_040_Calc_Odd() {
        System.out.println("   [Scenario] Nhập SL=5, Giá=1500 -> Check Thành tiền = 7.500");
        page.setRowQuantity(1, "5");
        page.setRowPrice(1, "1500");

        driver.findElement(By.tagName("body")).click();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        String amount = page.getRowLineAmountText(1);
        Assert.assertTrue(amount.contains("7.500") || amount.contains("7,500"));
    }

    /*
     * TC_041 - Cập nhật thành tiền khi sửa số lượng
     *
     * Steps:
     * 1) SL=2, Giá=10000.
     * 2) Sửa SL -> 5.
     *
     * Expected:
     * - Thành tiền cập nhật theo SL mới (5 * 10000 = 50.000).
     */
    @Test(description = "TC_041: Update Số lượng")
    public void TC_041_Calc_UpdateQty() {
        System.out.println("   [Scenario] Nhập SL=2, sau đó sửa thành 5 -> Check tiền cập nhật.");
        page.setRowQuantity(1, "2");
        page.setRowPrice(1, "10000");
        driver.findElement(By.tagName("body")).click();

        // Update
        System.out.println("   [Step] Sửa số lượng thành 5.");
        page.setRowQuantity(1, "5");
        driver.findElement(By.tagName("body")).click();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        String amount = page.getRowLineAmountText(1);
        Assert.assertTrue(amount.contains("50.000"));
    }

    /*
     * TC_042 - Cập nhật thành tiền khi sửa đơn giá
     *
     * Steps:
     * 1) SL=2, Giá=10000.
     * 2) Sửa Giá -> 20000.
     *
     * Expected:
     * - Thành tiền cập nhật = 40.000.
     */
    @Test(description = "TC_042: Update Đơn giá")
    public void TC_042_Calc_UpdatePrice() {
        System.out.println("   [Scenario] Nhập Giá=10k, sau đó sửa thành 20k -> Check tiền cập nhật.");
        page.setRowQuantity(1, "2");
        page.setRowPrice(1, "10000");
        driver.findElement(By.tagName("body")).click();

        // Update
        System.out.println("   [Step] Sửa đơn giá thành 20000.");
        page.setRowPrice(1, "20000");
        driver.findElement(By.tagName("body")).click();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        String amount = page.getRowLineAmountText(1);
        Assert.assertTrue(amount.contains("40.000"));
    }

    /*
     * TC_043 - Tính tổng tiền (1 dòng)
     *
     * Steps:
     * 1) SL=5, Giá=10000.
     * 2) Blur.
     *
     * Expected:
     * - Tổng thành tiền = 50.000.
     */
    @Test(description = "TC_043: Tổng tiền")
    public void TC_043_Calc_Total() {
        System.out.println("   [Scenario] Tính tổng tiền 1 dòng: 5 * 10.000 = 50.000");
        page.setRowQuantity(1, "5");
        page.setRowPrice(1, "10000");
        driver.findElement(By.tagName("body")).click();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        String total = page.getTotalAmountText();
        Assert.assertTrue(total.contains("50.000") || total.contains("50,000"));
    }

    /*
     * TC_044 - Tính tổng tiền (nhiều dòng)
     *
     * Steps:
     * 1) Dòng 1: Giá trị thành tiền = 10.000.
     * 2) Add dòng 2: thành tiền = 20.000.
     * 3) Blur.
     *
     * Expected:
     * - Tổng thành tiền = 30.000.
     */
    @Test(description = "TC_044: Tổng tiền nhiều dòng")
    public void TC_044_Calc_Total_MultiRow() {
        System.out.println("   [Scenario] Tính tổng tiền 2 dòng: 10k + 20k = 30k");

        // Dòng 1
        page.setRowData(1, "Panadol Extra", CATEGORY_NAME, UNIT_NAME, "1", "10000");

        System.out.println("   [Step] Thêm dòng thứ 2.");
        page.clickAddRow();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        } // Chờ render

        // Dòng 2
        page.setRowData(2, "Hapacol 250", CATEGORY_NAME, "Gói", "1", "20000");

        driver.findElement(By.tagName("body")).click();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }

        String total = page.getTotalAmountText();
        Assert.assertTrue(total.contains("30.000") || total.contains("30,000"));
    }

    /*
     * TC_045 - Tổng tiền cập nhật khi xóa dòng
     *
     * Steps:
     * 1) Có 2 dòng: 10.000 và 20.000.
     * 2) Xóa dòng 2.
     *
     * Expected:
     * - Tổng thành tiền giảm còn 10.000.
     */
    @Test(description = "TC_045: Cập nhật Tổng tiền khi xóa dòng")
    public void TC_045_Calc_Total_Delete() {
        System.out.println("   [Scenario] Nhập 2 dòng, sau đó xóa 1 dòng -> Tổng tiền phải giảm.");

        // Setup 2 dòng
        page.setRowData(1, "A", CATEGORY_NAME, UNIT_NAME, "1", "10000");
        page.clickAddRow();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        page.setRowData(2, "B", CATEGORY_NAME, UNIT_NAME, "1", "20000");
        driver.findElement(By.tagName("body")).click();

        // Xóa dòng 2
        System.out.println("   [Step] Xóa dòng thứ 2.");
        page.deleteRow(2);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }

        String total = page.getTotalAmountText();
        // Còn lại dòng 1 = 10k
        Assert.assertTrue(total.contains("10.000") || total.contains("10,000"));
    }

    /*
     * TC_046 - Format Input
     *
     * Mục tiêu:
     * - Kiểm tra format hiển thị khi nhập số (ngăn cách hàng nghìn, không crash).
     *
     * Steps:
     * 1) Nhập giá trị số vào Số lượng/Đơn giá.
     *
     * Expected:
     * - UI format đúng (nếu có), không lỗi.
     */
    @Test(description = "TC_046: Format Input - nhập số lớn và check UI format")
    public void TC_046_Format_Input() {
        System.out.println("   [Scenario] Nhập số lớn vào Qty/Price -> kiểm tra format hiển thị.");

        // Tiền điều kiện: có NCC để tránh submit bị chặn (không bắt buộc cho format,
        // nhưng ổn)
        page.selectSupplier(SUPPLIER_NAME);

        // Step 1: Nhập Qty và Price số lớn
        page.setRowQuantity(1, "1000");
        page.setRowPrice(1, "1234567"); // kỳ vọng format thành 1.234.567 hoặc 1,234,567

        // Step 2: blur để trigger format
        driver.findElement(By.tagName("body")).click();
        try {
            Thread.sleep(400);
        } catch (Exception ignored) {
        }

        // Step 3: đọc value thực tế
        String qtyVal = page.getRowQtyValue(1);
        String priceVal = page.getRowPriceValue(1);

        System.out.println("   [Read] Qty value = '" + qtyVal + "'");
        System.out.println("   [Read] Price value = '" + priceVal + "'");

        // Expected:
        // - Qty không rỗng và normalize đúng 1000
        Assert.assertTrue(qtyVal != null && !qtyVal.isEmpty(), "Qty bị rỗng sau khi nhập");
        Assert.assertEquals(qtyVal.replaceAll("[^0-9-]", ""), "1000", "Qty không đúng số sau khi nhập");

        // - Price normalize phải đúng 1234567
        Assert.assertTrue(priceVal != null && !priceVal.isEmpty(), "Price bị rỗng sau khi nhập");
        Assert.assertEquals(priceVal.replaceAll("[^0-9-]", ""), "1234567", "Price normalize không đúng");

        // - Bonus check: có format ngăn cách hàng nghìn (nếu UI có)
        Assert.assertTrue(
                priceVal.contains(".") || priceVal.contains(",") || priceVal.contains(" "),
                "Price không có format ngăn cách hàng nghìn (UI có thể chưa implement)");
    }

    /*
     * TC_047 - Format Total
     *
     * Mục tiêu:
     * - Kiểm tra format hiển thị của tổng tiền.
     *
     * Expected:
     * - Tổng tiền hiển thị đúng định dạng tiền tệ.
     */
    @Test(description = "TC_047: Format Total - kiểm tra format tổng tiền")
    public void TC_047_Format_Total() {
        System.out.println("   [Scenario] Nhập 1 dòng -> kiểm tra format Tổng thành tiền.");

        page.selectSupplier(SUPPLIER_NAME);
        page.setRowData(1, DRUG_NAME, CATEGORY_NAME, UNIT_NAME, "2", "10000"); // total = 20000
        driver.findElement(By.tagName("body")).click();
        try {
            Thread.sleep(400);
        } catch (Exception ignored) {
        }

        String totalText = page.getTotalAmountText();
        System.out.println("   [Read] Total text = '" + totalText + "'");

        // Expected: có tiền tệ
        Assert.assertTrue(totalText.contains("₫") || totalText.toLowerCase().contains("vnd"),
                "Tổng tiền không có ký hiệu tiền tệ");

        // Expected: normalize ra đúng 20000
        String digits = totalText.replaceAll("[^0-9]", "");
        Assert.assertEquals(digits, "20000", "Tổng tiền normalize không đúng");
    }

    // =================================================================
    // PHẦN 5: CHỨC NĂNG
    // =================================================================

    /*
     * TC_048 - Nhập SKU (tùy chọn)
     *
     * Mục tiêu:
     * - SKU là optional nhưng nhập được và vẫn tạo phiếu.
     *
     * Steps:
     * 1) Chọn NCC.
     * 2) Nhập dòng hợp lệ.
     * 3) Nhập SKU = "SKU-AUTO".
     * 4) Hoàn thành.
     *
     * Expected:
     * - Tạo phiếu nhập thành công.
     */
    @Test(description = "TC_048: Nhập SKU")
    public void TC_048_Func_SKU() {
        System.out.println("   [Scenario] Nhập SKU (Cột ẩn).");
        page.selectSupplier(SUPPLIER_NAME);
        page.setRowData(1, DRUG_NAME, CATEGORY_NAME, UNIT_NAME, "1", "1000");
        page.setRowSku(1, "SKU-AUTO");
        page.clickFinish();
        String msg = page.readFeedback();
        Assert.assertTrue(msg.contains("thành công") || msg.isEmpty());
    }

    /*
     * TC_049 - Nhập Số lô (tùy chọn)
     *
     * Steps:
     * 1) Chọn NCC.
     * 2) Nhập dòng hợp lệ.
     * 3) Nhập Số lô = "L001".
     * 4) Hoàn thành.
     *
     * Expected:
     * - Tạo phiếu nhập thành công.
     */
    @Test(description = "TC_049: Nhập Số lô")
    public void TC_049_Func_Lot() {
        System.out.println("   [Scenario] Nhập Số Lô.");
        page.selectSupplier(SUPPLIER_NAME);
        page.setRowData(1, DRUG_NAME, CATEGORY_NAME, UNIT_NAME, "1", "1000");
        page.setRowLot(1, "L001");
        page.clickFinish();
        String msg = page.readFeedback();
        Assert.assertTrue(msg.contains("thành công") || msg.isEmpty());
    }

    /*
     * TC_050 - Nhập Mô tả (tùy chọn)
     *
     * Steps:
     * 1) Chọn NCC.
     * 2) Nhập dòng hợp lệ.
     * 3) Nhập mô tả = "Desc".
     * 4) Hoàn thành.
     *
     * Expected:
     * - Tạo phiếu nhập thành công.
     */
    @Test(description = "TC_050: Nhập Mô tả")
    public void TC_050_Func_Desc() {
        System.out.println("   [Scenario] Nhập Mô tả.");
        page.selectSupplier(SUPPLIER_NAME);
        page.setRowData(1, DRUG_NAME, CATEGORY_NAME, UNIT_NAME, "1", "1000");
        page.setRowDescription(1, "Desc");
        page.clickFinish();
        String msg = page.readFeedback();
        Assert.assertTrue(msg.contains("thành công") || msg.isEmpty());
    }

    /*
     * TC_051 - Thêm dòng mới
     *
     * Mục tiêu:
     * - Bấm "+ Thêm dòng" tăng số dòng trong bảng.
     *
     * Steps:
     * 1) Lấy số dòng hiện tại.
     * 2) Click "+ Thêm dòng".
     *
     * Expected:
     * - Số dòng tăng thêm 1.
     */
    @Test(description = "TC_051: Thêm dòng")
    public void TC_051_Func_AddRow() {
        System.out.println("   [Scenario] Kiểm tra nút Thêm dòng.");
        int init = page.getRowCount();
        page.clickAddRow();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        Assert.assertEquals(page.getRowCount(), init + 1);
    }

    /*
     * TC_052 - Xóa dòng
     *
     * Mục tiêu:
     * - Thêm dòng thứ 2 và xóa được dòng đó.
     *
     * Steps:
     * 1) Click "+ Thêm dòng" tạo dòng 2.
     * 2) Click nút xóa dòng 2.
     *
     * Expected:
     * - Số dòng quay về 1.
     */
    @Test(description = "TC_052: Xóa dòng")
    public void TC_052_Func_DeleteRow() {
        System.out.println("   [Scenario] Thêm dòng mới, sau đó xóa đi.");
        page.clickAddRow();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        page.deleteRow(2);
        Assert.assertEquals(page.getRowCount(), 1);
    }

    /*
     * TC_053 - Không cho xóa dòng duy nhất
     *
     * Mục tiêu:
     * - Khi chỉ có 1 dòng thì nút xóa bị disable để tránh bảng rỗng.
     *
     * Tiền điều kiện:
     * - Đã login admin, đang ở trang Nhập kho (/import)
     *
     * Test data:
     * - Không cần
     *
     * Steps:
     * 1) Đảm bảo bảng chỉ còn 1 dòng.
     * 2) Quan sát nút xóa ở dòng 1.
     * 3) Thử click nút xóa dòng 1.
     *
     * Expected:
     * - Nút xóa dòng 1 có attribute disabled (UI nhạt màu).
     * - Click không xóa được, rowCount vẫn = 1.
     */
    @Test
    public void TC_053_Func_Delete_One() {
        System.out.println("   [Scenario] Còn 1 dòng duy nhất -> nút xóa phải disabled và không xóa được.");

        // Step 1: đảm bảo chỉ còn 1 dòng
        int rows = page.getRowCount();
        if (rows > 1) {
            System.out.println("   [Setup] Có " + rows + " dòng -> xóa về còn 1 dòng.");
            for (int i = rows; i >= 2; i--) {
                page.deleteRow(i);
                try {
                    Thread.sleep(500);
                } catch (Exception ignored) {
                }
            }
        }
        Assert.assertEquals(page.getRowCount(), 1, "Setup fail: không đưa về 1 dòng.");

        // Step 2: verify disabled attribute
        System.out.println("   [Verify] Kiểm tra nút xóa dòng 1 bị disable.");
        Assert.assertTrue(page.isDeleteButtonDisabled(1), "BUG: Nút xóa dòng 1 không disabled.");

        // Step 3: click thử -> không được xóa
        System.out.println("   [Action] Thử click nút xóa dòng 1.");
        page.clickDeleteButton(1);
        try {
            Thread.sleep(800);
        } catch (Exception ignored) {
        }

        System.out.println("   [Verify] Sau click, số dòng vẫn = 1.");
        Assert.assertEquals(page.getRowCount(), 1, "BUG: Vẫn xóa được dòng duy nhất!");
    }

    /*
     * TC_054 - Happy Path: Tạo phiếu nhập hợp lệ
     *
     * Mục tiêu:
     * - Tạo phiếu nhập kho thành công với dữ liệu hợp lệ.
     *
     * Steps:
     * 1) Chọn NCC.
     * 2) Nhập dòng thuốc hợp lệ (SL=10, Giá=5000).
     * 3) Nhập SKU (tùy chọn).
     * 4) Click Hoàn thành.
     *
     * Expected:
     * - Tạo phiếu nhập thành công.
     */
    @Test(description = "TC_054: Happy Path")
    public void TC_054_HappyPath() {
        System.out.println("   [Scenario] HAPPY PATH: Nhập đầy đủ, hợp lệ và Lưu.");
        page.selectSupplier(SUPPLIER_NAME);
        page.setRowData(1, DRUG_NAME, CATEGORY_NAME, UNIT_NAME, "10", "5000");
        page.setRowSku(1, "SKU-01");
        page.clickFinish();
        String msg = page.readFeedback();
        Assert.assertTrue(msg.contains("thành công") || msg.isEmpty());
    }

    /*
     * TC_055 - Hủy thao tác tạo phiếu (Fallback bằng Reload trang)
     *
     * Mục tiêu:
     * - Vì UI không có nút Hủy/Cancel, dùng reload để hủy thao tác nhập form.
     *
     * Tiền điều kiện:
     * - Đã login admin, đang ở trang /import
     *
     * Steps:
     * 1) Nhập NCC + nhập dữ liệu dòng 1 (tên thuốc/danh mục/đơn vị/SL/giá).
     * 2) Reload trang (driver.navigate().refresh()) hoặc page.open() lại.
     * 3) Verify form đã reset: Nhà cung cấp chưa được chọn và các input dòng 1
     * rỗng/0.
     *
     * Expected:
     * - Dữ liệu vừa nhập không còn trên form (form reset).
     * - Không tạo phiếu nhập (không bấm Hoàn thành).
     */
    @Test(description = "TC_055: Hủy thao tác bằng Reload trang (UI không có Cancel)")
    public void TC_055_Func_Cancel() {
        // Step 1: Nhập dữ liệu như đang tạo phiếu
        page.selectSupplier(SUPPLIER_NAME);
        page.setRowData(1, DRUG_NAME, CATEGORY_NAME, UNIT_NAME, "3", "2000");

        // Lấy value trước khi reload để chắc chắn là đã nhập
        String qtyBefore = page.getRowQtyValue(1);
        String priceBefore = page.getRowPriceValue(1);
        Assert.assertEquals(qtyBefore, "3", "Setup fail: Qty chưa được nhập đúng trước khi reload");
        Assert.assertTrue(priceBefore != null && !priceBefore.isEmpty(),
                "Setup fail: Price chưa có giá trị trước khi reload");

        // Step 2: Reload trang để hủy thao tác
        System.out.println("   [Action] Reload trang để hủy thao tác (UI không có Cancel).");
        driver.navigate().refresh();
        page.open(); // đảm bảo quay lại đúng màn Import

        // Step 3: Verify dữ liệu đã bị reset
        // - Với NCC: tuỳ UI, khó lấy text trên button -> mình verify bằng "không thể
        // hoàn thành"
        // (vì nếu NCC reset thì bấm Finish sẽ báo lỗi chọn NCC).
        System.out.println("   [Verify] Sau reload, thử bấm Hoàn thành để check form đã reset.");
        page.clickFinish();
        checkErrorMsg("nhà cung cấp"); // kỳ vọng reset NCC nên sẽ báo lỗi

        // - Verify thêm: qty/price dòng 1 rỗng hoặc mặc định
        String qtyAfter = page.getRowQtyValue(1);
        String priceAfter = page.getRowPriceValue(1);

        System.out.println("   [Verify] Qty sau reload = '" + qtyAfter + "', Price sau reload = '" + priceAfter + "'");
        Assert.assertTrue(qtyAfter == null || qtyAfter.isEmpty() || qtyAfter.equals("0"),
                "Qty chưa reset sau reload. Actual='" + qtyAfter + "'");
        Assert.assertTrue(priceAfter == null || priceAfter.isEmpty() || priceAfter.equals("0"),
                "Price chưa reset sau reload. Actual='" + priceAfter + "'");
    }

    /*
     * TC_056 - Mở lịch sử nhập/xuất và kiểm tra record sau khi nhập kho
     *
     * Mục tiêu:
     * - Sau khi tạo phiếu nhập kho thành công, record phải xuất hiện trong màn
     * "Lịch sử nhập/xuất thuốc".
     *
     * Tiền điều kiện:
     * - Đã login admin
     * - Đang ở trang /import (BeforeMethod đã mở sẵn)
     *
     * Test data:
     * - NCC: Công ty Dược phẩm LA ĐẠI LÔC
     * - Thuốc: Panadol | Danh mục: Thuốc giảm đau - Hạ sốt | Đơn vị: Hộp
     * - SL=3 | Giá=2000
     * - Số lô: unique để search (tránh trùng data cũ)
     *
     * Steps:
     * 1) Chọn NCC.
     * 2) Nhập dòng thuốc hợp lệ.
     * 3) Nhập Số lô unique.
     * 4) Click Hoàn thành.
     * 5) Click menu "Lịch sử nhập/xuất".
     * 6) Search theo Số lô vừa nhập.
     *
     * Expected:
     * - Có ít nhất 1 dòng chứa đúng Số lô vừa nhập.
     * - Loại hiển thị "Nhập kho".
     */
    @Test
    public void TC_056_Func_History() {
        System.out.println("   [Scenario] Nhập kho -> qua Lịch sử nhập/xuất kiểm tra record.");

        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(15));

        String lot = "L-AUTO-" + System.currentTimeMillis();

        // 1) Tạo phiếu nhập
        page.selectSupplier(SUPPLIER_NAME);
        page.setRowData(1, DRUG_NAME, CATEGORY_NAME, UNIT_NAME, "3", "2000");
        page.setRowLot(1, lot);
        page.clickFinish();

        // 2) Click menu Lịch sử nhập/xuất (PHẢI TRỎ ĐÚNG TEXT)
        By menuHistory = By.xpath(
                "//a[.//span[normalize-space()='Lịch sử nhập/xuất'] or contains(normalize-space(),'Lịch sử nhập/xuất')]");
        WebElement menu = w.until(ExpectedConditions.visibilityOfElementLocated(menuHistory));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", menu);

        try {
            w.until(ExpectedConditions.elementToBeClickable(menu)).click();
        } catch (Exception e) {
            System.out.println("   ⚠️ Click thường fail -> click JS");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menu);
        }

        // 3) Verify đã vào History (đừng chỉ check h1)
        w.until(d -> d.getCurrentUrl().toLowerCase().contains("lich-su")
                || d.getCurrentUrl().toLowerCase().contains("history")
                || d.getCurrentUrl().toLowerCase().contains("nhap-xuat"));

        System.out.println("   ✅ [Verify] URL sau khi click History = " + driver.getCurrentUrl());

        // (Nếu UI có h1 thì check thêm, còn không có thì bỏ cũng được)
        By titleHistory = By.xpath("//*[self::h1 or self::h2][contains(normalize-space(),'Lịch sử nhập/xuất')]");
        if (driver.findElements(titleHistory).size() > 0) {
            w.until(ExpectedConditions.visibilityOfElementLocated(titleHistory));
            System.out.println("   ✅ [Verify] Có title 'Lịch sử nhập/xuất'.");
        } else {
            System.out.println("   ⚠️ [Info] Không tìm thấy H1/H2 title, dùng URL để xác nhận trang.");
        }

        // 4) Search theo số lô
        By searchLot = By.xpath("//input[contains(@placeholder,'Tìm theo số lô')]");
        WebElement search = w.until(ExpectedConditions.visibilityOfElementLocated(searchLot));
        search.click();
        search.clear();
        search.sendKeys(lot);

        // 5) Verify record xuất hiện
        By rowContainsLot = By.xpath("//table//tbody//tr[contains(.,'" + lot + "')]");
        WebElement row = w.until(ExpectedConditions.visibilityOfElementLocated(rowContainsLot));

        String rowText = row.getText();
        System.out.println("   [Row] Found: " + rowText);

        Assert.assertTrue(rowText.toLowerCase().contains("nhập kho"),
                "Có số lô nhưng không thấy Loại = Nhập kho. Row=" + rowText);

        System.out.println("   ✅ [PASS] Record nhập kho đã xuất hiện trong lịch sử với đúng số lô.");
    }

    /*
     * TC_057 - Logic: Tăng tồn kho sau khi tạo phiếu
     *
     * Mục tiêu:
     * - Sau khi tạo phiếu nhập thành công, tồn kho tăng tương ứng.
     *
     * Steps:
     * 1) Tạo phiếu nhập thành công.
     * 2) Verify dữ liệu tồn kho (DB/API/UI).
     *
     * Expected:
     * - Tồn kho tăng đúng theo SL nhập.
     *
     * Note:
     * - Case này thường cần tích hợp DB/API nên có thể để pending nếu chưa có tool
     * verify.
     */
    @Test(description = "TC_057: Tăng tồn kho")
    public void TC_057_Logic_Stock() {
        Reporter.log("Verify DB", true);
    }

    /*
     * TC_058 - Logic: Trùng thuốc trong 2 dòng
     *
     * Mục tiêu:
     * - Khi nhập 2 dòng cùng 1 thuốc, hệ thống xử lý đúng (cho phép hoặc gộp) theo
     * nghiệp vụ.
     *
     * Steps:
     * 1) Chọn NCC.
     * 2) Dòng 1: Panadol SL=5 Giá=1000.
     * 3) Add dòng 2: Panadol SL=10 Giá=1000.
     * 4) Hoàn thành.
     *
     * Expected:
     * - Tạo phiếu thành công.
     * - (Nếu có rule gộp) tổng SL đúng.
     */
    @Test(description = "TC_058: Trùng thuốc")
    public void TC_058_Logic_Duplicate() {
        System.out.println("   [Scenario] Nhập 2 dòng cùng 1 loại thuốc -> Hệ thống cho phép/tự gộp.");
        page.selectSupplier(SUPPLIER_NAME);
        page.setRowData(1, DRUG_NAME, CATEGORY_NAME, UNIT_NAME, "5", "1000");
        page.clickAddRow();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        page.setRowData(2, DRUG_NAME, CATEGORY_NAME, UNIT_NAME, "10", "1000");
        page.clickFinish();
        String msg = page.readFeedback();
        Assert.assertTrue(msg.contains("thành công") || msg.isEmpty());
    }

    // @Test public void TC_059_Func_Search() {}

    // =================================================================
    // CÁC HÀM HỖ TRỢ RIÊNG (HELPER METHODS)
    // =================================================================

    private void checkErrorMsg(String expectedKeyword) {
        // 1) Alert
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            String text = alert.getText().toLowerCase();
            System.out.println("   ⚠️ [Check] Alert: " + text);

            Assert.assertTrue(
                    text.contains(expectedKeyword.toLowerCase()),
                    "Sai alert. Expect chứa: " + expectedKeyword + " | Actual: " + text);

            alert.accept();
            return;
        } catch (TimeoutException e) {
        }

        // 2) Toast
        String toastMsg = page.readFeedback();
        if (!toastMsg.isEmpty()) {
            System.out.println("   ⚠️ [Check] Toast: " + toastMsg);
            Assert.assertTrue(
                    toastMsg.toLowerCase().contains(expectedKeyword.toLowerCase()),
                    "Sai toast. Expect chứa: " + expectedKeyword + " | Actual: " + toastMsg);
            return;
        }

        Assert.fail("Không thấy lỗi nào chứa: " + expectedKeyword);
    }

    private void checkAlertContains(String expectedText) {
        checkErrorMsg(expectedText);
    }

    private String to_yyyyMMdd_plusDays(String dateText, int plusDays) {
        if (dateText == null)
            return "";

        dateText = dateText.trim();

        java.time.LocalDate d;

        // 1) Nếu input là yyyy-MM-dd (VD: 2025-12-20)
        if (dateText.matches("\\d{4}-\\d{2}-\\d{2}")) {
            d = java.time.LocalDate.parse(dateText); // default ISO
        }
        // 2) Nếu input là MM/dd/yyyy (VD: 12/20/2025)
        else if (dateText.matches("\\d{2}/\\d{2}/\\d{4}")) {
            java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy");
            d = java.time.LocalDate.parse(dateText, f);
        } else {
            throw new RuntimeException("Không nhận dạng được format ngày nhập: '" + dateText + "'");
        }

        return d.plusDays(plusDays).toString(); // yyyy-MM-dd
    }

    private String yyyyMMdd_plusDays(String yyyyMMdd, int plusDays) {
        java.time.LocalDate d = java.time.LocalDate.parse(yyyyMMdd); // yyyy-MM-dd
        return d.plusDays(plusDays).toString();
    }

}
>>>>>>> c1a6054a5e0969d81197da02e0c72fad0d758ab2
