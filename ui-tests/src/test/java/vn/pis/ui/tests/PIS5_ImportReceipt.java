// ui-tests/src/test/java/vn/pis/ui/tests/PIS5_ImportReceipt.java
package vn.pis.ui.tests;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;
import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.ImportReceiptPage;
import vn.pis.ui.pages.LoginPage;

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
